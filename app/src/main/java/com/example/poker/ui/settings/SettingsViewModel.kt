package com.example.poker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.repository.AuthRepository
import com.example.poker.data.storage.AppSettings
import com.example.poker.data.repository.Result
import com.example.poker.shared.dto.UpdateUsernameRequest
import com.example.poker.shared.dto.UserResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsUiEvent {
    data class ShowSnackBar(val message: String) : SettingsUiEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isPerformanceMode = MutableStateFlow(false)
    val isPerformanceMode: StateFlow<Boolean> = _isPerformanceMode.asStateFlow()

    private val _userResponse = MutableStateFlow<UserResponse?>(null)
    val userResponse: StateFlow<UserResponse?> = _userResponse.asStateFlow()

    private val _uiEvents = MutableSharedFlow<SettingsUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        _isPerformanceMode.value = appSettings.getPerformanceMode()
        viewModelScope.launch {
            val res = authRepository.meInfo()
            val user = when(res) {
                is Result.Success -> {
                    appSettings.saveUsername(res.data.username)
                    res.data
                }
                is Result.Error -> {
                    UserResponse("123", appSettings.getUsername(), "example@gmail.com", 1000.0)
                }
            }
            _userResponse.value = user
        }
    }

    fun setPerformanceMode(bool: Boolean) {
        appSettings.savePerformanceMode(bool)
        _isPerformanceMode.value = bool
    }

    fun onLogoutClick() {
        // Просто удаляем токены
        appSettings.saveAccessToken(null)
        appSettings.saveRefreshToken(null)
    }

    fun changeUsername(newUsername: String) {
        viewModelScope.launch {
            // Оптимистичное обновление UI
            _userResponse.value = _userResponse.value?.copy(username = newUsername)

            // 1. Сохраняем локально немедленно
            appSettings.saveUsername(newUsername)

            // 2. Отправляем запрос на сервер
            val result = authRepository.updateUsername(UpdateUsernameRequest(newUsername))

            if (result is Result.Error) {
                _uiEvents.emit(SettingsUiEvent.ShowSnackBar("Не удалось сменить имя: ${result.message}, но оно было сохранено локально"))
            }
        }
    }
}