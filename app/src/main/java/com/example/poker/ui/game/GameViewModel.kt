package com.example.poker.ui.game

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.GameRoomCache
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.AppJson
import com.example.poker.data.remote.dto.GameRoom
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.IncomingMessage
import com.example.poker.data.remote.dto.OutgoingMessage
import com.example.poker.data.remote.dto.Player
import com.example.poker.data.remote.dto.PlayerState
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val apiClient: KtorApiClient,
    private val appSettings: AppSettings,
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = savedStateHandle.get<String>("roomId") ?: ""

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _myUserId = MutableStateFlow<String?>(null)
    val myUserId: StateFlow<String?> = _myUserId.asStateFlow()

    private val _roomInfo = MutableStateFlow<GameRoom?>(null)
    val roomInfo: StateFlow<GameRoom?> = _roomInfo.asStateFlow()

    val playersOnTable: StateFlow<List<PlayerState>> = combine(_roomInfo, _gameState) { room, state ->
        state?.playerStates ?: (room?.players?.map { PlayerState(player = it) } ?: emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var session: DefaultClientWebSocketSession? = null

    init {
        _myUserId.value = decodeJwtAndGetUserId(appSettings.getAccessToken())
        val initialRoom = GameRoomCache.currentRoom
        Log.d("testPlayers1", initialRoom?.players.toString())
        if (initialRoom != null) {
            _roomInfo.value = initialRoom
            GameRoomCache.currentRoom = null // Очищаем кэш
        }
        connectToGame()
        viewModelScope.launch {
            val updatedRoom = loadRoomDetails()
            Log.d("testPlayers2", updatedRoom?.players.toString())
            if(updatedRoom != initialRoom && updatedRoom != null) _roomInfo.value = updatedRoom
        }
    }

    private suspend fun loadRoomDetails(): GameRoom? {
        return when (val result = gameRepository.getRoomDetails(roomId)) {
            is Result.Success -> result.data
            is Result.Error -> null
        }
    }

    private fun connectToGame() {
        viewModelScope.launch {
            val token = appSettings.getAccessToken() ?: return@launch
            try {
                apiClient.client.webSocket(
                    method = HttpMethod.Get,
                    host = "amessenger.ru", port = 8080, path = "/play/$roomId",
                    request = { header(HttpHeaders.Authorization, "Bearer $token") }
                ) {
                    session = this
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val messageJson = frame.readText()
                            when (val message = AppJson.decodeFromString<OutgoingMessage>(messageJson)) {
                                is OutgoingMessage.GameStateUpdate -> {
                                    Log.d("testNewState", message.state.toString())
                                    _gameState.value = message.state
                                }
                                is OutgoingMessage.AllInEquityUpdate -> println("123") // todo
                                is OutgoingMessage.BlindsUp -> println("123") // todo
                                is OutgoingMessage.ErrorMessage -> println("123") // todo
                                is OutgoingMessage.LobbyUpdate -> println("lobby update") // todo
                                is OutgoingMessage.OfferRunItMultipleTimes -> println("123") // todo
                                is OutgoingMessage.PlayerJoined -> {
                                    Log.d("testPlayerJoined", message.username)
                                    _roomInfo.value = _roomInfo.value?.copy(
                                        players = _roomInfo.value!!.players + Player(userId = message.username, username = message.username, stack = 0) //todo
                                    )
                                }
                                is OutgoingMessage.PlayerLeft -> {
                                    Log.d("testPlayerLeft", message.username)
                                    _roomInfo.value = _roomInfo.value?.copy(
                                        players = _roomInfo.value!!.players.filterNot { it.username == message.username }
                                    )
                                }
                                is OutgoingMessage.RunItMultipleTimesResult -> println("123") // todo
                                is OutgoingMessage.SocialActionBroadcast -> println("123") // todo
                                is OutgoingMessage.TournamentWinner -> println("123") // todo
                                is OutgoingMessage.PlayerReadyUpdate -> println("123") // todo
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Game WS Error: ${e.message}")
            }
        }
    }

    // --- Методы для отправки действий на сервер ---
    fun onFold() = sendAction(IncomingMessage.Fold())
    fun onCheck() = sendAction(IncomingMessage.Check())
    fun onCall() = sendAction(IncomingMessage.Call())
    fun onBet(amount: Long) = sendAction(IncomingMessage.Bet(amount))
    fun onReadyClick(isReady: Boolean) = sendAction(IncomingMessage.SetReady(isReady))

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
        } catch (e: Exception) {
            return null
        }
    }
}