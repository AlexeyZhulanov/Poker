package com.example.poker.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.AppJson
import com.example.poker.data.remote.dto.AuthResponse
import com.example.poker.data.remote.dto.GameRoom
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.IncomingMessage
import com.example.poker.data.remote.dto.LoginRequest
import com.example.poker.data.remote.dto.OutgoingMessage
import com.example.poker.data.remote.dto.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val apiClient: KtorApiClient
) : ViewModel() {

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private var session: DefaultClientWebSocketSession? = null
    private val _myUserId = MutableStateFlow<String?>(null)
    val myUserId: StateFlow<String?> = _myUserId.asStateFlow()

    private fun addLog(message: String) {
        _logs.value = _logs.value + message
        Log.d("testLog", "${_logs.value + message}")
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    private fun decodeJwtAndGetUserId(token: String): String? {
        try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
            // Simple regex to find the userId claim
            val userIdRegex = """"userId":"([^"]+)"""".toRegex()
            return userIdRegex.find(payload)?.groupValues?.get(1)
        } catch (e: Exception) {
            return null
        }
    }

    fun register() {
        viewModelScope.launch {
            try {
                addLog("Registering...")
                val response = apiClient.client.post("http://194.169.160.6:8080/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(RegisterRequest("testuser1", "test1@test.com", "123456"))
                }
                addLog("Register response: ${response.status}")
            } catch (e: Exception) {
                addLog("Error: ${e.message}")
            }
        }
    }

    fun login() {
        viewModelScope.launch {
            try {
                addLog("Logging in...")
                val response = apiClient.client.post("http://194.169.160.6:8080/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest("testuser1", "123456"))
                }

                // 2. Парсим ответ и сохраняем токен
                if (response.status == HttpStatusCode.OK) {
                    val authResponse = response.body<AuthResponse>()
                    _accessToken.value = authResponse.accessToken
                    _myUserId.value = decodeJwtAndGetUserId(authResponse.accessToken)
                    addLog("Login SUCCESS. My User ID: ${_myUserId.value}")
                } else {
                    addLog("Login FAILED: ${response.status}")
                }
            } catch (e: Exception) {
                addLog("Error: ${e.message}")
            }
        }
    }

    fun createRoom() {
        viewModelScope.launch {
            try {
                val token = _accessToken.value ?: return@launch
                addLog("Creating room...")
                val room = apiClient.client.post("http://194.169.160.6:8080/rooms") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }.body<GameRoom>()
                addLog("Room created with ID: ${room.roomId}")
                //connectWebSocket(room.roomId)
            } catch (e: Exception) { addLog("Error creating room: ${e.message}") }
        }
    }

    fun joinRoom(roomId: String) {
        viewModelScope.launch {
            val token = _accessToken.value ?: return@launch
            addLog("Joining room $roomId...")
            try {
                val response = apiClient.client.post("http://194.169.160.6:8080/rooms/$roomId/join") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                if (response.status == HttpStatusCode.OK) {
                    addLog("Successfully joined room. Connecting to WebSocket...")
                    connectWebSocket(roomId)
                } else {
                    addLog("Failed to join room: ${response.status}")
                }
            } catch (e: Exception) {
                addLog("Error joining room: ${e.message}")
            }
        }
    }

    fun connectWebSocket(roomId: String) {
        viewModelScope.launch {
            val token = _accessToken.value
            if (token == null) {
                addLog("Cannot connect: Not logged in (no token).")
                return@launch
            }

            addLog("Connecting to WebSocket with token...")
            try {
                apiClient.client.webSocket(
                    method = HttpMethod.Get,
                    host = "amessenger.ru",
                    port = 8080,
                    path = "/play/$roomId",
                    request = {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                ) {
                    session = this
                    addLog("WebSocket Connected!")

                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val messageJson = frame.readText()
                            addLog("Received: $messageJson") // Логируем сырой JSON

                            // Десериализуем и обрабатываем по типам
                            try {
                                when (val message = Json.decodeFromString<OutgoingMessage>(messageJson)) {
                                    is OutgoingMessage.GameStateUpdate -> {
                                        _gameState.value = message.state
                                        addLog("GameState Updated: Stage ${message.state.stage}, Pot ${message.state.pot}")
                                    }
                                    is OutgoingMessage.PlayerJoined -> {
                                        addLog("Player Joined: ${message.username}")
                                    }
                                    is OutgoingMessage.PlayerLeft -> {
                                        addLog("Player Left: ${message.username}")
                                    }
                                    is OutgoingMessage.ErrorMessage -> {
                                        addLog("ERROR: ${message.message}")
                                    }
                                    is OutgoingMessage.AllInEquityUpdate -> {
                                        addLog("Equity Update Received: ${message.equities.entries.joinToString()}")
                                    }
                                    is OutgoingMessage.RunItMultipleTimesResult -> {
                                        addLog("Multi-Run Result Received: ${message.results.size} boards.")
                                    }
                                    is OutgoingMessage.BlindsUp -> {
                                        addLog("Blinds Up! Lvl ${message.level}: ${message.smallBlind}/${message.bigBlind}")
                                    }
                                    is OutgoingMessage.TournamentWinner -> {
                                        addLog("Tournament Winner: ${message.winnerUsername}!")
                                    }
                                    is OutgoingMessage.OfferRunItMultipleTimes -> {
                                        addLog("Offer to Run it: ${message.options.joinToString("/")} times. Waiting for choice.")
                                    }
                                    is OutgoingMessage.SocialActionBroadcast -> {
                                        addLog("action ${message.action} from ${message.fromPlayerId}")
                                    }
                                }
                            } catch (e: Exception) {
                                addLog("Deserialization error: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                addLog("WS Connection Error: ${e.message}")
            } finally {
                session = null
                addLog("WebSocket Disconnected.")
            }
        }
    }
    fun sendFold() = sendAction(IncomingMessage.Fold())
    fun sendCheck() = sendAction(IncomingMessage.Check())
    fun sendBet(amount: Long) = sendAction(IncomingMessage.Bet(amount))

    private fun sendAction(action: IncomingMessage) {
        viewModelScope.launch {
            if (session?.isActive == true) {
                session?.sendSerialized(action)
                addLog("Sent action: ${action::class.simpleName}")
            } else {
                addLog("Cannot send action: WebSocket not connected.")
            }
        }
    }
}