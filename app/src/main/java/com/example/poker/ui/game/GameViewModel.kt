package com.example.poker.ui.game

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.GameRoomCache
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.AppJson
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import com.example.poker.di.AuthEvent
import com.example.poker.di.AuthEventBus
import com.example.poker.domain.model.OfflineHostManager
import com.example.poker.shared.dto.GameMode
import com.example.poker.shared.dto.IncomingMessage
import com.example.poker.shared.dto.OutgoingMessage
import com.example.poker.shared.dto.OutsInfo
import com.example.poker.shared.dto.PlayerStatus
import com.example.poker.shared.model.Card
import com.example.poker.ui.game.RunItUiState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.util.Base64
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

sealed interface RunItUiState {
    data object Hidden : RunItUiState
    data class AwaitingUnderdogChoice(val expiresAt: Long) : RunItUiState
    data class AwaitingFavoriteConfirmation(val underdogId: String, val times: Int, val expiresAt: Long) : RunItUiState
}

data class AllInEquity(
    val equities: ImmutableMap<String, Double>,
    val outs: ImmutableMap<String, OutsInfo> = persistentMapOf(),
    val runIndex: Int
)

data class TournamentInfo(val sb: Long, val bb: Long, val ante: Long, val level: Int, val levelTime: Long)

enum class StackDisplayMode {
    CHIPS, BIG_BLINDS
}

@HiltViewModel
class GameViewModel @Inject constructor(
    private val apiClient: KtorApiClient,
    private val appSettings: AppSettings,
    private val gameRepository: GameRepository,
    private val authEventBus: AuthEventBus,
    private val offlineHostManager: OfflineHostManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val encodedUrl: String = savedStateHandle.get<String>("gameUrl") ?: ""
    private val gameUrl: String = URLDecoder.decode(encodedUrl, "UTF-8")
    private val isOffline: Boolean = savedStateHandle.get<Boolean>("isOffline") ?: false
    private val isHost: Boolean = isOffline && gameUrl.contains("localhost")
    private val roomId: String = if(isOffline) "offline_room" else gameUrl.substringAfterLast('/')

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _myUserId = MutableStateFlow<String?>(null)
    val myUserId: StateFlow<String?> = _myUserId.asStateFlow()

    private val _roomInfo = MutableStateFlow<GameRoom?>(null)
    val roomInfo: StateFlow<GameRoom?> = _roomInfo.asStateFlow()

    private val _runItUiState = MutableStateFlow<RunItUiState>(Hidden)
    val runItUiState: StateFlow<RunItUiState> = _runItUiState.asStateFlow()

    private val _isActionPanelLocked = MutableStateFlow(false)
    val isActionPanelLocked: StateFlow<Boolean> = _isActionPanelLocked.asStateFlow()
    private var lockJob: Job? = null

    private val _allInEquity = MutableStateFlow<AllInEquity?>(null)
    val allInEquity: StateFlow<AllInEquity?> = _allInEquity.asStateFlow()

    private val _staticCommunityCards = MutableStateFlow<ImmutableList<Card>>(persistentListOf())
    val staticCommunityCards: StateFlow<ImmutableList<Card>> = _staticCommunityCards.asStateFlow()

    private val _boardRunouts = MutableStateFlow<ImmutableList<ImmutableList<Card>>>(persistentListOf())
    val boardRunouts: StateFlow<ImmutableList<ImmutableList<Card>>> = _boardRunouts.asStateFlow()

    private val _runsCount = MutableStateFlow(0)
    val runsCount: StateFlow<Int> = _runsCount.asStateFlow()

    private val _gameMode = MutableStateFlow<GameMode?>(null)
    val gameMode: StateFlow<GameMode?> = _gameMode.asStateFlow()

    private val _stackDisplayMode = MutableStateFlow(StackDisplayMode.CHIPS)
    val stackDisplayMode: StateFlow<StackDisplayMode> = _stackDisplayMode.asStateFlow()

    private val _boardResult = MutableStateFlow<ImmutableList<Pair<String, Long>>?>(null)
    val boardResult: StateFlow<ImmutableList<Pair<String, Long>>?> = _boardResult.asStateFlow()

    private val _tournamentInfo = MutableStateFlow<TournamentInfo?>(null)
    val tournamentInfo: StateFlow<TournamentInfo?> = _tournamentInfo.asStateFlow()

    private val _tournamentWinner = MutableStateFlow<String?>(null)
    val tournamentWinner: StateFlow<String?> = _tournamentWinner.asStateFlow()

    private val _scaleMultiplier = MutableStateFlow(1.0f)
    val scaleMultiplier: StateFlow<Float> = _scaleMultiplier.asStateFlow()

    private val _specsCount = MutableStateFlow(0)
    val specsCount: StateFlow<Int> = _specsCount.asStateFlow()

    private val _isReconnecting = MutableStateFlow(false)
    val isReconnecting: StateFlow<Boolean> = _isReconnecting.asStateFlow()

    private val _isPerformanceMode = MutableStateFlow(false)
    val isPerformanceMode: StateFlow<Boolean> = _isPerformanceMode.asStateFlow()

    private val _isClassicCardsEnabled = MutableStateFlow(false)
    val isClassicCardsEnabled: StateFlow<Boolean> = _isClassicCardsEnabled.asStateFlow()

    private val _isFourColorMode = MutableStateFlow(true)
    val isFourColorMode: StateFlow<Boolean> = _isFourColorMode.asStateFlow()

    private var winnerDisplayJob: Job? = null

    private var session: DefaultClientWebSocketSession? = null
    private var connectionJob: Job? = null

    init {
        _myUserId.value = decodeJwtAndGetUserId(appSettings.getAccessToken())
        _scaleMultiplier.value = appSettings.getScaleMultiplier()
        _isPerformanceMode.value = appSettings.getPerformanceMode()
        _isClassicCardsEnabled.value = appSettings.getClassicCardsEnabled()
        _isFourColorMode.value = appSettings.getFourColorMode()
    }

    private suspend fun loadInitialState() {
        val initialRoom = GameRoomCache.currentRoom
        if (initialRoom != null) {
            _roomInfo.value = GameRoom.fromUserInput(initialRoom)
            _specsCount.value = initialRoom.players.filter { it.status == PlayerStatus.SPECTATING }.size
            GameRoomCache.currentRoom = null // Очищаем кэш
            _gameMode.value = initialRoom.gameMode
        }
        coroutineScope {
            val gameStateJob = async { gameRepository.getGameState(roomId) }
            val roomDetailsJob = async { gameRepository.getRoomDetails(roomId) }
            val gameStateResult = gameStateJob.await()
            val roomResult = roomDetailsJob.await()

            if (gameStateResult is Result.Success) {
                _gameState.value = GameState.fromUserInput(gameStateResult.data)
            }
            if(roomResult is Result.Success) {
                if(roomResult.data != initialRoom) {
                    _roomInfo.value = GameRoom.fromUserInput(roomResult.data)
                    _specsCount.value = roomResult.data.players.filter { it.status == PlayerStatus.SPECTATING }.size
                    _gameMode.value = roomResult.data.gameMode
                }
            }
        }
    }

    fun connect() {
        if (connectionJob?.isActive == true) return

        connectionJob = viewModelScope.launch {
            while (true) {
                var wasKicked = false // Флаг, чтобы определить причину разрыва
                try {
                    // 1. Сначала загружаем актуальное состояние комнаты через REST если это онлайн-режим
                    if(!isOffline) {
                        loadInitialState()
                    }
                    Log.d("testGameWS", "Connecting to room $roomId...")
                    // 2. Потом запускаем WebSocket
                    apiClient.client.webSocket(
                        urlString = gameUrl,
                        request = {
                            if(!isOffline) {
                            val token = appSettings.getAccessToken()
                            if(token != null) header(HttpHeaders.Authorization, "Bearer $token")
                            }
                        }
                    ) {
                        _isReconnecting.value = false
                        Log.d("testGameWS", "Connected.")
                        session = this
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val messageJson = frame.readText()
                                when (val message = AppJson.decodeFromString<OutgoingMessage>(messageJson)) {
                                    is OutgoingMessage.GameStateUpdate -> {
                                        Log.d("testNewState", message.state.toString())
                                        _gameState.value = message.state?.let { GameState.fromUserInput(it) }

                                        _isActionPanelLocked.value = false
                                        lockJob?.cancel()
                                        if(message.state == null) {
                                            _roomInfo.update { currentRoom ->
                                                currentRoom?.copy(players = currentRoom.players.map { it.copy(isReady = false) }.toImmutableList())
                                            }
                                        }
                                        // Если идет Run It Multiple times, обновляем нужную доску
                                        val currentState = message.state
                                        val runIndex = currentState?.runIndex
                                        if (runIndex != null && _boardRunouts.value.isNotEmpty()) {
                                            _boardRunouts.update { currentRunouts ->
                                                currentRunouts.toMutableList().also { mutableList ->
                                                    val runoutCards = currentState.communityCards.drop(_staticCommunityCards.value.size)
                                                    mutableList[runIndex - 1] = runoutCards.toImmutableList()
                                                }.toImmutableList()
                                            }
                                        } else {
                                            _boardRunouts.value = persistentListOf()
                                            if(message.state?.runIndex == null) _allInEquity.value = null
                                        }
                                    }
                                    is OutgoingMessage.AllInEquityUpdate -> {
                                        Log.d("testEquity", message.equities.toString())
                                        _allInEquity.value = AllInEquity(
                                            equities = message.equities.toImmutableMap(),
                                            outs = message.outs.toImmutableMap(),
                                            runIndex = message.runIndex
                                        )
                                    }
                                    is OutgoingMessage.StartBoardRun -> {
                                        Log.d("testStartRun", "Started, total runs: ${message.totalRuns}")
                                        if(message.totalRuns > 1) {
                                            if (message.runIndex == 1) {
                                                // Первая крутка: запоминаем статичные карты и создаем первую пустую доску
                                                _staticCommunityCards.value = _gameState.value?.communityCards?.toImmutableList() ?: persistentListOf()
                                                Log.d("testStaticCards", _staticCommunityCards.value.toString())
                                                _runsCount.value = message.totalRuns
                                                _boardRunouts.value = persistentListOf(persistentListOf())
                                            } else {
                                                // Последующие крутки: добавляем еще одну пустую доску
                                                _boardRunouts.value = (_boardRunouts.value + persistentListOf(persistentListOf())).toImmutableList()
                                            }
                                        }
                                    }
                                    is OutgoingMessage.BlindsUp -> {
                                        Log.d("testBlindsUp", message.toString())
                                        _tournamentInfo.value = TournamentInfo(
                                            message.smallBlind, message.bigBlind, message.ante,
                                            message.level, message.levelTime
                                        )
                                    }
                                    is OutgoingMessage.ErrorMessage -> println("123") // todo
                                    is OutgoingMessage.LobbyUpdate -> {} // скипаем, это для другой страницы
                                    is OutgoingMessage.OfferRunItMultipleTimes -> {
                                        _runItUiState.value = AwaitingFavoriteConfirmation(
                                            underdogId = message.underdogId,
                                            times = message.times,
                                            expiresAt = message.expiresAt
                                        )
                                    }
                                    is OutgoingMessage.OfferRunItForUnderdog -> {
                                        _runItUiState.value = AwaitingUnderdogChoice(
                                            expiresAt = message.expiresAt
                                        )
                                    }
                                    is OutgoingMessage.PlayerJoined -> {
                                        Log.d("testPlayerJoined", message.player.toString())
                                        _roomInfo.update { currentRoom ->
                                            currentRoom?.copy(players = (currentRoom.players + message.player).toImmutableList())
                                        }
                                        _specsCount.value = _roomInfo.value?.players?.filter { it.status == PlayerStatus.SPECTATING }?.size ?: 0
                                    }
                                    is OutgoingMessage.PlayerLeft -> {
                                        Log.d("testPlayerLeft", message.userId)
                                        _roomInfo.update { currentRoom ->
                                            currentRoom?.copy(players = currentRoom.players.filterNot { it.userId == message.userId }.toImmutableList())
                                        }
                                        _specsCount.value = _roomInfo.value?.players?.filter { it.status == PlayerStatus.SPECTATING }?.size ?: 0
                                    }
                                    is OutgoingMessage.BoardResult -> {
                                        Log.d("testBoardResult", message.payments.toString())
                                        _boardResult.value = message.payments.toImmutableList()
                                        // Отменяем предыдущий таймер, если он был
                                        winnerDisplayJob?.cancel()
                                        // Запускаем новый таймер на 3 секунды, чтобы скрыть подсветку
                                        winnerDisplayJob = viewModelScope.launch {
                                            delay(3000L)
                                            _boardResult.value = null
                                        }
                                    }
                                    is OutgoingMessage.SocialActionBroadcast -> println("123") // todo
                                    is OutgoingMessage.TournamentWinner -> {
                                        Log.d("testTournamentWinner", message.toString())
                                        _tournamentWinner.value = message.winnerUserId
                                    }
                                    is OutgoingMessage.PlayerReadyUpdate -> {
                                        Log.d("testPlayerReady", "id: ${message.userId}, isReady: ${message.isReady}")
                                        _roomInfo.update { currentRoom ->
                                            currentRoom?.copy(
                                                players = currentRoom.players.map { player ->
                                                    if (player.userId == message.userId) {
                                                        player.copy(isReady = message.isReady)
                                                    } else {
                                                        player
                                                    }
                                                }.toImmutableList()
                                            )
                                        }
                                    }
                                    is OutgoingMessage.PlayerStatusUpdate -> {
                                        Log.d("testUpdateStatus", message.status.toString())
                                        _roomInfo.update { currentRoom ->
                                            currentRoom?.copy(
                                                players = currentRoom.players.map { player ->
                                                    if (player.userId == message.userId) {
                                                        player.copy(status = message.status)
                                                    } else {
                                                        player
                                                    }
                                                }.toImmutableList()
                                            )
                                        }
                                        _specsCount.value = _roomInfo.value?.players?.filter { it.status == PlayerStatus.SPECTATING }?.size ?: 0
                                    }
                                    is OutgoingMessage.ConnectionStatusUpdate -> {
                                        _roomInfo.update { currentRoom ->
                                            currentRoom?.copy(
                                                players = currentRoom.players.map {
                                                    if (it.userId == message.userId) it.copy(isConnected = message.isConnected) else it
                                                }.toImmutableList()
                                            )
                                        }
                                        _gameState.update { currentState ->
                                            currentState?.copy(
                                                playerStates = currentState.playerStates.map {
                                                    if (it.player.userId == message.userId) {
                                                        it.copy(player = it.player.copy(isConnected = message.isConnected))
                                                    } else {
                                                        it
                                                    }
                                                }.toImmutableList()
                                            )
                                        }
                                    }
                                    is OutgoingMessage.GameRoomUpdateOffline -> {
                                        val room = message.room
                                        if (room != null) {
                                            _roomInfo.value = GameRoom.fromUserInput(room)
                                            _specsCount.value = room.players.filter { it.status == PlayerStatus.SPECTATING }.size
                                            _gameMode.value = room.gameMode
                                        }
                                    }
                                    is OutgoingMessage.GameStateUpdateOffline -> {
                                        val state = message.state
                                        state?.let { _gameState.value = GameState.fromUserInput(it) }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) Log.d("testGameWS", "Error: ${e.message}")
                } finally {
                    // 1. Получаем причину закрытия от сервера
                    val reason = session?.closeReason?.await()

                    // 2. Проверяем, был ли это "кик"
                    if (reason?.code == CloseReason.Codes.VIOLATED_POLICY.code) {
                        Log.d("testGameWS", "Kicked from room by server.")
                        wasKicked = true // Ставим флаг, что нас выгнали
                        authEventBus.postEvent(AuthEvent.KickedFromRoom)
                    } else {
                        Log.d("testGameWS", "Disconnected. Reason: $reason")
                    }
                }
                // Если нас выгнали, больше не пытаемся переподключиться
                if (wasKicked) {
                    break // Выходим из цикла while(true)
                }
                _isReconnecting.value = true
                Log.d("testGameWS", "Disconnected. Reconnecting in 5 seconds...")
                delay(5000L) // Пауза перед попыткой переподключения
            }
        }
    }

    fun disconnect() {
        Log.d("testGameWS", "Disconnecting...")
        connectionJob?.cancel()
        connectionJob = null
    }

    // --- Методы для отправки действий на сервер ---
    fun onFold() {
        if (_isActionPanelLocked.value) return
        lockActionPanel()
        sendAction(IncomingMessage.Fold())
    }
    fun onCheck() {
        if (_isActionPanelLocked.value) return
        lockActionPanel()
        sendAction(IncomingMessage.Check())
    }
    fun onCall() {
        if (_isActionPanelLocked.value) return
        lockActionPanel()
        sendAction(IncomingMessage.Call())
    }
    fun onBet(amount: Long) {
        if (_isActionPanelLocked.value) return
        lockActionPanel()
        sendAction(IncomingMessage.Bet(amount))
    }
    fun onReadyClick(isReady: Boolean) = sendAction(IncomingMessage.SetReady(isReady))
    fun onRunItChoice(times: Int) {
        sendAction(IncomingMessage.SelectRunCount(times))
        _runItUiState.value = Hidden
    }
    fun onRunItConfirmation(accepted: Boolean) {
        sendAction(IncomingMessage.AgreeRunCount(accepted))
        _runItUiState.value = Hidden
    }
    fun hideRunItState() {
        _runItUiState.value = Hidden
    }
    fun onSitAtTableClick() {
        sendAction(IncomingMessage.SitAtTable)
    }

    private fun lockActionPanel() {
        _isActionPanelLocked.value = true
        lockJob = viewModelScope.launch {
            delay(5000L)
            _isActionPanelLocked.value = false
        }
    }

    private fun sendAction(action: IncomingMessage) {
        viewModelScope.launch {
            session?.sendSerialized(action)
        }
    }

    private fun decodeJwtAndGetUserId(token: String?): String? {
        try {
            val parts = token?.split(".") ?: return null
            if (parts.size < 2) return null
            val payload = String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
            // Simple regex to find the userId claim
            val userIdRegex = """"userId":"([^"]+)"""".toRegex()
            return userIdRegex.find(payload)?.groupValues?.get(1)
        } catch (_: Exception) {
            return null
        }
    }

    fun toggleStackDisplayMode() {
        _stackDisplayMode.update {
            if (it == StackDisplayMode.CHIPS) StackDisplayMode.BIG_BLINDS else StackDisplayMode.CHIPS
        }
    }

    fun changeScale(change: Float) {
        val newValue = (_scaleMultiplier.value + change).coerceIn(0.5f, 1.5f) // Ограничиваем 50%-150%
        _scaleMultiplier.value = newValue
        appSettings.saveScaleMultiplier(newValue)
    }

    fun toggleClassicCardsEnabled() {
        val bool = !isClassicCardsEnabled.value
        _isClassicCardsEnabled.value = bool
        appSettings.saveClassicCardsEnabled(bool)
    }

    fun toggleFourColorMode() {
        val bool = !isFourColorMode.value
        _isFourColorMode.value = bool
        appSettings.saveFourColorMode(bool)
    }

    override fun onCleared() {
        super.onCleared()
        if (isHost) {
            offlineHostManager.stopAll() // останавливаем локальный сервер
        }
    }
}