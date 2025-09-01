package com.example.poker.ui.settings

import androidx.lifecycle.ViewModel
import com.example.poker.data.storage.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings
) : ViewModel() {

    private val _isPerformanceMode = MutableStateFlow(false)
    val isPerformanceMode: StateFlow<Boolean> = _isPerformanceMode.asStateFlow()

    init {
        _isPerformanceMode.value = appSettings.getPerformanceMode()
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
}