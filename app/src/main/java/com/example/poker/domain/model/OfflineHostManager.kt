package com.example.poker.domain.model

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.poker.data.remote.dto.DiscoveredGame
import com.example.poker.shared.dto.CreateRoomRequest
import com.example.poker.shared.dto.IncomingMessage
import com.example.poker.shared.dto.LeaveRequest
import com.example.poker.shared.dto.OutgoingMessage
import com.example.poker.shared.model.GameRoomService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.Executor
import kotlin.time.Duration.Companion.seconds

class OfflineHostManager(
    context: Context,
    private val gameRoomService: GameRoomService
) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverJob: Job? = null
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private val mainThreadExecutor: Executor = ContextCompat.getMainExecutor(context)
    private val _discoveredGames = MutableStateFlow<List<DiscoveredGame>>(emptyList())
    val discoveredGames = _discoveredGames.asStateFlow()

    // Карта для хранения активных подписок на сервисы
    private val serviceInfoCallbacks = mutableMapOf<String, NsdManager.ServiceInfoCallback>()

    companion object {
        const val SERVICE_TYPE = "_poker._tcp."
        const val SERVICE_PORT = 8080
        const val ROOM_ID = "offline_room"
    }

    // --- Логика Хоста ---

    suspend fun startHost(request: CreateRoomRequest, userId: String, username: String) {
        gameRoomService.createRoom(request, userId, username, roomId = ROOM_ID)
        startKtorServer()
        registerService(request.name)
    }

    private suspend fun startKtorServer() {
        if (serverJob?.isActive == true) return

        // Для того, чтобы viewModel дожидалась завершения запуска сервера
        val serverReady = CompletableDeferred<Unit>()

        serverJob = coroutineScope.launch {
            embeddedServer(Netty, port = SERVICE_PORT) {
                install(WebSockets) {
                    pingPeriod = 15.seconds
                    timeout = 15.seconds
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }
                install(ContentNegotiation) {
                    json()
                }
                // Когда сервер готов, отправляем сигнал
                monitor.subscribe(ApplicationStarted) {
                    serverReady.complete(Unit)
                }
                routing {
                    post("/leave") {
                        try {
                            val request = call.receive<LeaveRequest>()
                            Log.d("testOfflineHostManager", "Received leave request from ${request.userId}")
                            gameRoomService.onLobbyJoinInOffline(request.userId)
                            call.respond(HttpStatusCode.OK)
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid request")
                        }
                    }
                    webSocket("/play") {
                        // Читаем параметры из URL
                        val userId = call.request.queryParameters["userId"]
                        val username = call.request.queryParameters["username"]

                        // Если клиент не представился - разрываем соединение
                        if (userId.isNullOrBlank() || username.isNullOrBlank()) {
                            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "User ID and username are required."))
                            return@webSocket
                        }

                        // Логика из post("/{roomId}/join")
                        val (updatedRoom, player) = gameRoomService.joinRoom(ROOM_ID, userId, username)
                        if (updatedRoom == null) {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Room is full or not found."))
                            return@webSocket
                        }

                        // Новый игрок подключился
                        Log.d("testOfflineHostManager", "Player connected: $username ($userId)")

                        // Оповещаем всех о новом игроке
                        player?.let { gameRoomService.broadcast(ROOM_ID, OutgoingMessage.PlayerJoined(it)) }

                        val engine = gameRoomService.getEngine(ROOM_ID)
                        val gameState = engine?.getPersonalizedGameStateFor(userId)

                        try {
                            // Отправляем состояние вместо REST по сокетам
                            gameState?.let {
                                val jsonString1 = Json.encodeToString(OutgoingMessage.serializer(), OutgoingMessage.GameStateUpdateOffline(it))
                                this.send(Frame.Text(jsonString1))
                            }
                            val jsonString2 = Json.encodeToString(OutgoingMessage.serializer(), OutgoingMessage.GameRoomUpdateOffline(updatedRoom))
                            this.send(Frame.Text(jsonString2))

                            gameRoomService.onJoin(ROOM_ID, userId, this)
                            for (frame in incoming) {
                                frame as? Frame.Text ?: continue
                                val engine = gameRoomService.getEngine(ROOM_ID)
                                val currentGameState = engine?.getCurrentGameState()
                                val activePlayerId = currentGameState?.playerStates?.getOrNull(currentGameState.activePlayerPosition)?.player?.userId

                                val isActiveFlag = activePlayerId == userId

                                val incomingMessage = Json.decodeFromString<IncomingMessage>(frame.readText())
                                when (incomingMessage) {
                                    is IncomingMessage.Fold -> if(isActiveFlag) engine.processFold(userId)
                                    is IncomingMessage.Bet -> if(isActiveFlag) engine.processBet(userId, incomingMessage.amount)
                                    is IncomingMessage.Check -> if(isActiveFlag) engine.processCheck(userId)
                                    is IncomingMessage.Call -> if(isActiveFlag) engine.processCall(userId)
                                    is IncomingMessage.SelectRunCount -> engine?.processUnderdogRunChoice(userId, incomingMessage.times)
                                    is IncomingMessage.AgreeRunCount -> engine?.processFavoriteRunConfirmation(userId, incomingMessage.isAgree)
                                    is IncomingMessage.PerformSocialAction -> engine?.processSocialAction(userId, incomingMessage.action)
                                    is IncomingMessage.SetReady -> gameRoomService.setPlayerReady(ROOM_ID, userId, incomingMessage.isReady)
                                    is IncomingMessage.SitAtTable -> gameRoomService.handleSitAtTable(ROOM_ID, userId)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("testOfflineHostManager", e.stackTraceToString())
                        } finally {
                            // Игрок отключился
                            gameRoomService.onPlayerDisconnected(ROOM_ID, userId)
                        }
                    }
                }
            }.start(wait = true)
        }
        Log.d("testOfflineHostManager", "Ktor server job started")
        serverReady.await() // Ждем сигнала о том, что сервер запустился
    }

    private suspend fun registerService(gameName: String) {
        // Точно так же ждем выполнения из viewModel
        val registrationComplete = CompletableDeferred<Unit>()

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.d("testOfflineHostManager", "Service registered: $gameName")
                registrationComplete.complete(Unit)
            }
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.d("testOfflineHostManager", "Service registration failed, errorCode:$errorCode")
                registrationComplete.completeExceptionally(
                    RuntimeException("NSD registration failed with error code: $errorCode")
                )
            }
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.d("testOfflineHostManager", "Service unregistered: $gameName")
                registrationComplete.completeExceptionally(
                    RuntimeException("Service unregistered: $gameName")
                )
            }
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.d("testOfflineHostManager", "Service unregistration failed, errorCode:$errorCode")
                registrationComplete.completeExceptionally(
                    RuntimeException("Service unregistration failed, errorCode:$errorCode")
                )
            }
        }
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = gameName
            this.serviceType = SERVICE_TYPE
            this.port = SERVICE_PORT
        }
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        try {
            registrationComplete.await()
        } catch (e: Exception) {
            Log.e("testOfflineHostManager", "Could not await service registration", e)
        }
    }

    // --- Логика Клиента ---

    fun discoverGames() {
        // Очищаем старые колбэки перед новым поиском
        stopDiscovery()

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
            override fun onDiscoveryStarted(serviceType: String) {
                Log.d("testOfflineHostManager", "Discovery started")
            }
            override fun onDiscoveryStopped(serviceType: String) {
                Log.d("testOfflineHostManager", "Discovery stopped")
            }
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d("testOfflineHostManager", "Service found: $serviceInfo")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    registerServiceInfoCallback(serviceInfo)
                } else {
                    resolveService(serviceInfo)
                }
            }
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d("testOfflineHostManager", "Service lost: $serviceInfo")
                _discoveredGames.value = _discoveredGames.value.filter { it.serviceName != serviceInfo.serviceName }
                // Если мы теряем сервис, нужно отписаться от его колбэка
                serviceInfoCallbacks.remove(serviceInfo.serviceName)?.also { callback ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        nsdManager.unregisterServiceInfoCallback(callback)
                    }
                }
            }
        }
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun registerServiceInfoCallback(serviceInfo: NsdServiceInfo) {
        val callback = object : NsdManager.ServiceInfoCallback {
            override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                Log.e("testOfflineHostManager", "Callback registration failed: $errorCode")
            }

            override fun onServiceInfoCallbackUnregistered() {
                Log.d("testOfflineHostManager", "Callback unregistered")
            }

            override fun onServiceUpdated(service: NsdServiceInfo) {
                Log.d("testOfflineHostManager", "Service updated via callback: ${service.serviceName}")
                val hostAddress = service.hostAddresses.firstOrNull()?.hostAddress
                if (hostAddress.isNullOrEmpty()) return

                val newGame = DiscoveredGame(
                    serviceName = service.serviceName,
                    hostAddress = hostAddress,
                    port = service.port
                )

                // Обновляем или добавляем игру в список
                val currentGames = _discoveredGames.value.filter { it.serviceName != newGame.serviceName }
                _discoveredGames.value = currentGames + newGame
            }

            override fun onServiceLost() {
                Log.d("testOfflineHostManager", "Service lost via callback: ${serviceInfo.serviceName}")
                _discoveredGames.value = _discoveredGames.value.filter { it.serviceName != serviceInfo.serviceName }
            }
        }
        // Сохраняем колбэк, чтобы потом отписаться
        serviceInfoCallbacks[serviceInfo.serviceName] = callback
        nsdManager.registerServiceInfoCallback(serviceInfo, mainThreadExecutor, callback)
    }


    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e("testOfflineHostManager", "Resolve failed: $errorCode")
            }
            override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                val hostAddress = if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 7) {
                    resolvedInfo.hostAddresses.firstOrNull()?.hostAddress
                } else {
                    @Suppress("DEPRECATION")
                    resolvedInfo.host?.hostAddress
                }
                if (hostAddress.isNullOrEmpty()) {
                    Log.e("testOfflineHostManager", "Could not get host address")
                    return
                }
                Log.d("testOfflineHostManager", "Service resolved: $hostAddress:${resolvedInfo.port}")
                val newGame = DiscoveredGame(
                    serviceName = resolvedInfo.serviceName,
                    hostAddress = hostAddress,
                    port = resolvedInfo.port
                )
                if (newGame.hostAddress.isNotEmpty() && !_discoveredGames.value.any { it.serviceName == newGame.serviceName }) {
                    _discoveredGames.value += newGame
                }
            }
        }
        @Suppress("DEPRECATION")
        if (SdkExtensions.getExtensionVersion(Build.VERSION_CODES.TIRAMISU) >= 3) {
            nsdManager.resolveService(serviceInfo, mainThreadExecutor, resolveListener)
        } else {
            nsdManager.resolveService(serviceInfo, resolveListener)
        }
    }

    fun stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener)
            discoveryListener = null
        }
        // Отписываемся от всех активных колбэков
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            serviceInfoCallbacks.values.forEach { nsdManager.unregisterServiceInfoCallback(it) }
        }
        serviceInfoCallbacks.clear()
        _discoveredGames.value = emptyList()
    }

    fun stopAll() {
        stopDiscovery()
        if (registrationListener != null) {
            nsdManager.unregisterService(registrationListener)
            registrationListener = null
        }
        serverJob?.cancel()
        serverJob = null
    }
}