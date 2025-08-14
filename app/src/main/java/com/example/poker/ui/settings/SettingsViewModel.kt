package com.example.poker.ui.settings

import androidx.lifecycle.ViewModel
import com.example.poker.data.storage.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettings
) : ViewModel() {

    fun onLogoutClick() {
        // Просто удаляем токены
        appSettings.saveAccessToken(null)
        appSettings.saveRefreshToken(null)
    }
}