package com.example.poker.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.GameRoomCache
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.GameRoom
import com.example.poker.data.remote.dto.OutgoingMessage
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import com.example.poker.di.AuthEvent
import com.example.poker.di.AuthEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val apiClient: KtorApiClient,
    private val appSettings: AppSettings,
    private val gameRepository: GameRepository,
    private val authEventBus: AuthEventBus
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<GameRoom>>(emptyList())
    val rooms: StateFlow<List<GameRoom>> = _rooms.asStateFlow()

    private val _navigateToGameEvent = MutableSharedFlow<String>()
    val navigateToGameEvent = _navigateToGameEvent.asSharedFlow()

    init {
        connectToLobbySocket()
    }

    private fun connectToLobbySocket() {
        viewModelScope.launch {
            val token = appSettings.getAccessToken()

            if(token == null) {
                delay(500)
                authEventBus.postEvent(AuthEvent.SessionExpired)
                return@launch
            }
            try {
                apiClient.client.webSocket(
                    method = HttpMethod.Get,
                    host = "amessenger.ru", port = 8080, path = "/lobby",
                    request = { header(HttpHeaders.Authorization, "Bearer $token") }
                ) {
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val message = Json.decodeFromString<OutgoingMessage>(frame.readText())
                            if (message is OutgoingMessage.LobbyUpdate) {
                                _rooms.value = message.rooms
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // TODO: Handle error
                println("Lobby WS Error: ${e.message}")
            }
        }
    }

    fun onJoinClick(roomId: String) {
        viewModelScope.launch {
            when (val result = gameRepository.joinRoom(roomId)) {
                is Result.Success -> {
                    GameRoomCache.currentRoom = result.data
                    _navigateToGameEvent.emit(roomId)
                }
                is Result.Error -> {
                    // TODO: Показать ошибку пользователю
                    println("Join error: ${result.message}")
                }
            }
        }
    }
}