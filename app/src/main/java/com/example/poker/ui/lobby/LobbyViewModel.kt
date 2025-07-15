package com.example.poker.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.GameRoom
import com.example.poker.data.remote.dto.OutgoingMessage
import com.example.poker.data.storage.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val apiClient: KtorApiClient,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _rooms = MutableStateFlow<List<GameRoom>>(emptyList())
    val rooms: StateFlow<List<GameRoom>> = _rooms.asStateFlow()

    init {
        connectToLobbySocket()
    }

    private fun connectToLobbySocket() {
        viewModelScope.launch {
            val token = appSettings.getAccessToken() ?: return@launch
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
}