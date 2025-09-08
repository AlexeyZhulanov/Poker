package com.example.poker.ui.createroom

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.GameRoomCache
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import com.example.poker.domain.model.OfflineHostManager
import com.example.poker.shared.dto.BlindStructureType
import com.example.poker.shared.dto.CreateRoomRequest
import com.example.poker.shared.dto.GameMode
import com.example.poker.util.serverSocketUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gameRepository: GameRepository,
    private val offlineHostManager: OfflineHostManager,
    private val appSettings: AppSettings
) : ViewModel() {

    // Получаем флаг режима из аргументов навигации
    private val isOfflineMode: Boolean = savedStateHandle.get<Boolean>("isOffline") ?: false

    private val _roomName = MutableStateFlow("New Room")
    val roomName = _roomName.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.CASH)
    val gameMode = _gameMode.asStateFlow()

    //private val _initialStack = MutableStateFlow("1000")
    //val initialStack = _initialStack.asStateFlow()

    private val _blindStructureType = MutableStateFlow(BlindStructureType.STANDARD)
    val blindStructureType = _blindStructureType.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Pair<String?, Boolean>>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onRoomNameChange(name: String) { _roomName.value = name }
    fun onGameModeChange(mode: GameMode) { _gameMode.value = mode }
    //fun onStackChange(stack: String) { _initialStack.value = stack }
    fun onBlindStructureChange(type: BlindStructureType) { _blindStructureType.value = type }

    fun onCreateClick() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true // Блокируем UI
            val request = CreateRoomRequest(
                name = roomName.value,
                gameMode = _gameMode.value,
                maxPlayers = 9,
                smallBlind = 10, // TODO: брать из полей UI
                bigBlind = 20,
                blindStructureType = if (gameMode.value == GameMode.TOURNAMENT) blindStructureType.value else null
            )
            if(isOfflineMode) {
                createOfflineRoom(request)
            } else {
                createOnlineRoom(request)
            }
        }
    }

    private suspend fun createOnlineRoom(request: CreateRoomRequest) {
        val result = gameRepository.createRoom(request)
        when(result) {
            is Result.Success -> {
                GameRoomCache.currentRoom = result.data
                val url = "$serverSocketUrl/play/${result.data.roomId}"
                _navigationEvent.emit(URLEncoder.encode(url, "UTF-8") to false)
            }
            is Result.Error -> {
                Log.d("testError", result.message)
                _isLoading.value = false
                _navigationEvent.emit(null to false) // Сигнал, что можно просто вернуться назад
            }
        }
    }

    private fun createOfflineRoom(request: CreateRoomRequest) {
        val userId = appSettings.getUserId()
        val username = appSettings.getUsername()
        viewModelScope.launch {
            offlineHostManager.startHost(request, userId, username)
            _navigationEvent.emit(encodeUrlWithCredentialsOffline() to true)
        }
    }

    private fun encodeUrlWithCredentialsOffline(): String {
        val baseUrl = "ws://localhost:8080/play"
        val userId = appSettings.getUserId()
        val username = URLEncoder.encode(appSettings.getUsername(), "UTF-8")
        val fullUrl = "$baseUrl?userId=$userId&username=$username"
        return URLEncoder.encode(fullUrl, "UTF-8") // Кодируем весь URL для навигации
    }
}