package com.example.poker.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.remote.dto.DiscoveredGame
import com.example.poker.data.storage.AppSettings
import com.example.poker.domain.model.OfflineHostManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OfflineLobbyViewModel @Inject constructor(
    private val offlineHostManager: OfflineHostManager,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val discoveredGames = offlineHostManager.discoveredGames

    fun startDiscovery() {
        val userId = appSettings.getUserId()
        offlineHostManager.lobbyJoin(userId)
        offlineHostManager.discoverGames()
    }

    fun stopDiscovery() {
        offlineHostManager.stopDiscovery()
    }

    fun joinGame(game: DiscoveredGame, navigate: (DiscoveredGame) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                navigate(game)
            } finally {
                delay(3000)
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        offlineHostManager.stopDiscovery()
    }
}