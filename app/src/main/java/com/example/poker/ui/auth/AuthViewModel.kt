package com.example.poker.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.shared.dto.LoginRequest
import com.example.poker.shared.dto.RegisterRequest
import com.example.poker.data.repository.AuthRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import com.example.poker.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val username: String = "",
    val isUsernameError: Boolean = false,
    val email: String = "",
    val isEmailError: Boolean = false,
    val password: String = "",
    val isPasswordError: Boolean = false,
    val isLoading: Boolean = false,
    val generalError: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private fun validateUsername(username: String): Boolean {
        // Ник от 2 до 20 символов: англ/рус буквы, цифры, ! и $
        val usernameRegex = Regex("^[a-zA-Zа-яА-Я0-9!$]{2,20}$")
        return username.matches(usernameRegex)
    }

    private fun validateEmail(email: String): Boolean {
        // Минимум 6 символов и должен содержать @
        return email.length >= 6 && "@" in email && "." in email
    }

    private fun validatePassword(password: String): Boolean {
        // Пароль от 4 до 100 символов: англ/рус буквы, цифры, ! и $
        val passwordRegex = Regex("^[a-zA-Zа-яА-Я0-9!$]{4,100}$")
        return password.matches(passwordRegex)
    }

    fun onUsernameChange(newUsername: String) {
        _uiState.update {
            it.copy(
                username = newUsername,
                isUsernameError = if (newUsername.isNotEmpty()) !validateUsername(newUsername) else false
            )
        }
    }
    fun onEmailChange(newEmail: String) {
        _uiState.update {
            it.copy(
                email = newEmail,
                isEmailError = if (newEmail.isNotEmpty()) !validateEmail(newEmail) else false
            )
        }
    }
    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                isPasswordError = if (newPassword.isNotEmpty()) !validatePassword(newPassword) else false
            )
        }
    }

    fun onRegisterClick() {
        val currentState = _uiState.value
        val isUsernameValid = validateUsername(currentState.username)
        val isEmailValid = validateEmail(currentState.email)
        val isPasswordValid = validatePassword(currentState.password)

        // Обновляем состояние ошибок для всех полей, чтобы показать их, если юзер нажал кнопку
        _uiState.update {
            it.copy(
                isUsernameError = !isUsernameValid,
                isEmailError = !isEmailValid,
                isPasswordError = !isPasswordValid
            )
        }
        if (!isUsernameValid || !isEmailValid || !isPasswordValid) {
            _uiState.update { it.copy(generalError = "Пожалуйста, исправьте ошибки в полях") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepository.register(
                RegisterRequest(currentState.username, currentState.email, currentState.password)
            )
            when(result) {
                is Result.Success -> {
                    appSettings.saveAccessToken(result.data.accessToken)
                    appSettings.saveRefreshToken(result.data.refreshToken)
                    _navigationEvent.emit(Screen.Lobby.route)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(generalError = result.message) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onLoginClick() {
        val currentState = _uiState.value
        val isEmailValid = validateEmail(currentState.email)
        val isPasswordValid = validatePassword(currentState.password)
        _uiState.update {
            it.copy(
                isEmailError = !isEmailValid,
                isPasswordError = !isPasswordValid
            )
        }
        if (!isEmailValid || !isPasswordValid) {
            _uiState.update { it.copy(generalError = "Пожалуйста, исправьте ошибки в полях") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }
            val result = authRepository.login(LoginRequest(currentState.email, currentState.password))
            Log.d("testLogin", result.toString())
            when(result) {
                is Result.Success -> {
                    appSettings.saveAccessToken(result.data.accessToken)
                    appSettings.saveRefreshToken(result.data.refreshToken)
                    _navigationEvent.emit(Screen.Lobby.route)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(generalError = result.message) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}