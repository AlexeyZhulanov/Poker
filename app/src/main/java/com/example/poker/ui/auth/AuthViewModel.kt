package com.example.poker.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.remote.dto.LoginRequest
import com.example.poker.data.remote.dto.RegisterRequest
import com.example.poker.data.repository.AuthRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import com.example.poker.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appSettings: AppSettings
) : ViewModel() {

    // Состояние для полей ввода
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onUsernameChange(newUsername: String) { _username.value = newUsername }
    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }
    fun clearError() { _error.value = null }

    fun onRegisterClick() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.register(
                RegisterRequest(
                    username.value,
                    email.value,
                    password.value
                )
            )
            when(result) {
                is Result.Success -> {
                    appSettings.saveAccessToken(result.data.accessToken)
                    appSettings.saveRefreshToken(result.data.refreshToken)
                    _navigationEvent.emit(Screen.Lobby.route)
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun onLoginClick() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.login(LoginRequest(username.value, password.value))
            when(result) {
                is Result.Success -> {
                    Log.d("testLogin", result.toString())
                    appSettings.saveAccessToken(result.data.accessToken)
                    appSettings.saveRefreshToken(result.data.refreshToken)
                    _navigationEvent.emit(Screen.Lobby.route)
                }
                is Result.Error -> {
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }
}