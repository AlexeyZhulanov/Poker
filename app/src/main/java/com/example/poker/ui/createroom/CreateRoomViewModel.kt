package com.example.poker.ui.createroom

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.remote.dto.CreateRoomRequest
import com.example.poker.data.remote.dto.GameMode
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
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

    private val _initialStack = MutableStateFlow("1000")
    val initialStack = _initialStack.asStateFlow()

    // ... можно добавить состояния и для других полей (блайнды, длительность)

    private val _navigateBackEvent = MutableSharedFlow<Unit>()
    val navigateBackEvent = _navigateBackEvent.asSharedFlow()

    fun onRoomNameChange(name: String) { _roomName.value = name }
    fun onGameModeChange(mode: GameMode) { _gameMode.value = mode }
    fun onStackChange(stack: String) { _initialStack.value = stack }

    fun onCreateClick() {
        viewModelScope.launch {
            val request = CreateRoomRequest(
                name = roomName.value,
                gameMode = gameMode.value,
                maxPlayers = 9,
                initialStack = initialStack.value.toLongOrNull() ?: 1000L,
                smallBlind = 10, // TODO: брать из полей UI
                bigBlind = 20,
                levelDurationMinutes = 10
            )
            val result = gameRepository.createRoom(request)
            when(result) {
                is Result.Success -> {
                    _navigateBackEvent.emit(Unit)
                }
                is Result.Error -> {
                    // todo
                    Log.d("testError", result.message)
                }
            }

        }
    }
}