package com.example.poker.ui.createroom

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.GameRoomCache
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
import com.example.poker.shared.dto.BlindStructureType
import com.example.poker.shared.dto.CreateRoomRequest
import com.example.poker.shared.dto.GameMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _roomName = MutableStateFlow("New Room")
    val roomName = _roomName.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.CASH)
    val gameMode = _gameMode.asStateFlow()

    //private val _initialStack = MutableStateFlow("1000")
    //val initialStack = _initialStack.asStateFlow()

    private val _blindStructureType = MutableStateFlow(BlindStructureType.STANDARD)
    val blindStructureType = _blindStructureType.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String?>()
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
            val result = gameRepository.createRoom(request)
            when(result) {
                is Result.Success -> {
                    GameRoomCache.currentRoom = result.data
                    _navigationEvent.emit(result.data.roomId)
                }
                is Result.Error -> {
                    Log.d("testError", result.message)
                    _isLoading.value = false
                    _navigationEvent.emit(null) // Сигнал, что можно просто вернуться назад
                }
            }
        }
    }
}