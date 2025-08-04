package com.example.poker.ui.game

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poker.data.GameRoomCache
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.AppJson
import com.example.poker.data.remote.dto.Card
import com.example.poker.data.remote.dto.GameRoom
import com.example.poker.data.remote.dto.GameState
import com.example.poker.data.remote.dto.IncomingMessage
import com.example.poker.data.remote.dto.OutgoingMessage
import com.example.poker.data.remote.dto.OutsInfo
import com.example.poker.data.remote.dto.Player
import com.example.poker.data.remote.dto.PlayerState
import com.example.poker.data.repository.GameRepository
import com.example.poker.data.repository.Result
import com.example.poker.data.storage.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Base64
import javax.inject.Inject

sealed interface RunItUiState {
    data object Hidden : RunItUiState
    data object AwaitingUnderdogChoice : RunItUiState
    data class AwaitingFavoriteConfirmation(val underdogId: String, val times: Int) : RunItUiState
}

data class AllInEquity(
    val equities: Map<String, Double>,
    val outs: Map<String, OutsInfo> = emptyMap(),
    val runIndex: Int
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val apiClient: KtorApiClient,
    private val appSettings: AppSettings,
    private val gameRepository: GameRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = savedStateHandle.get<String>("roomId") ?: ""

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _myUserId = MutableStateFlow<String?>(null)
    val myUserId: StateFlow<String?> = _myUserId.asStateFlow()

    private val _roomInfo = MutableStateFlow<GameRoom?>(null)
    val roomInfo: StateFlow<GameRoom?> = _roomInfo.asStateFlow()

    private val _runItUiState = MutableStateFlow<RunItUiState>(RunItUiState.Hidden)
    val runItUiState: StateFlow<RunItUiState> = _runItUiState.asStateFlow()

    private val _isActionPanelLocked = MutableStateFlow(false)
    val isActionPanelLocked: StateFlow<Boolean> = _isActionPanelLocked.asStateFlow()
    private var lockJob: Job? = null

    private val _allInEquity = MutableStateFlow<AllInEquity?>(null)
    val allInEquity: StateFlow<AllInEquity?> = _allInEquity.asStateFlow()

    private val _staticCommunityCards = MutableStateFlow<List<Card>>(emptyList())
    val staticCommunityCards: StateFlow<List<Card>> = _staticCommunityCards.asStateFlow()

    private val _boardRunouts = MutableStateFlow<List<List<Card>>>(emptyList())
    val boardRunouts: StateFlow<List<List<Card>>> = _boardRunouts.asStateFlow()

    private val _runsCount = MutableStateFlow(0)
    val runsCount: StateFlow<Int> = _runsCount.asStateFlow()

    val playersOnTable: StateFlow<List<PlayerState>> = combine(_roomInfo, _gameState) { room, state ->
        state?.playerStates ?: (room?.players?.map { PlayerState(player = it) } ?: emptyList())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var session: DefaultClientWebSocketSession? = null

    init {
        _myUserId.value = decodeJwtAndGetUserId(appSettings.getAccessToken())
        val initialRoom = GameRoomCache.currentRoom
        Log.d("testPlayers1", initialRoom?.players.toString())
        if (initialRoom != null) {
            _roomInfo.value = initialRoom
            GameRoomCache.currentRoom = null // Очищаем кэш
        }
        connectToGame()
        viewModelScope.launch {
            val updatedRoom = loadRoomDetails()
            Log.d("testPlayers2", updatedRoom?.players.toString())
            if(updatedRoom != initialRoom && updatedRoom != null) _roomInfo.value = updatedRoom
        }
    }

    private suspend fun loadRoomDetails(): GameRoom? {
        return when (val result = gameRepository.getRoomDetails(roomId)) {
            is Result.Success -> result.data
            is Result.Error -> null
        }
    }

    private fun connectToGame() {
        viewModelScope.launch {
            val token = appSettings.getAccessToken() ?: return@launch
            try {
                apiClient.client.webSocket(
                    method = HttpMethod.Get,
                    host = "amessenger.ru", port = 8080, path = "/play/$roomId",
                    request = { header(HttpHeaders.Authorization, "Bearer $token") }
                ) {
                    session = this
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val messageJson = frame.readText()
                            when (val message = AppJson.decodeFromString<OutgoingMessage>(messageJson)) {
                                is OutgoingMessage.GameStateUpdate -> {
                                    Log.d("testNewState", message.state.toString())
                                    _gameState.value = message.state
                                    if(message.state == null) {
                                        _roomInfo.update { currentRoom ->
                                            currentRoom?.copy(players = currentRoom.players.map { it.copy(isReady = false) })
                                        }
                                    }
                                    _isActionPanelLocked.value = false
                                    lockJob?.cancel()
                                    // Если идет Run It Multiple times, обновляем нужную доску
                                    if (message.state?.runIndex != null && _boardRunouts.value.isNotEmpty()) {
                                        _boardRunouts.update { currentRunouts ->
                                            currentRunouts.toMutableList().also { mutableList ->
                                                val runoutCards = message.state.communityCards.drop(_staticCommunityCards.value.size)
                                                mutableList[message.state.runIndex - 1] = runoutCards
                                            }
                                        }
                                    } else {
                                        _boardRunouts.value = emptyList()
                                        if(message.state?.runIndex == null) _allInEquity.value = null
                                    }
                                }
                                is OutgoingMessage.AllInEquityUpdate -> {
                                    Log.d("testEquity", message.equities.toString())
                                    _allInEquity.value = AllInEquity(
                                        equities = message.equities,
                                        outs = message.outs,
                                        runIndex = message.runIndex
                                    )
                                }
                                is OutgoingMessage.StartBoardRun -> {
                                    Log.d("testStartRun", "Started, total runs: ${message.totalRuns}")
                                    if(message.totalRuns > 1) {
                                        if (message.runIndex == 1) {
                                            // Первая крутка: запоминаем статичные карты и создаем первую пустую доску
                                            _staticCommunityCards.value = _gameState.value?.communityCards ?: emptyList()
                                            Log.d("testStaticCards", _staticCommunityCards.value.toString())
                                            _boardRunouts.value = listOf(emptyList())
                                            _runsCount.value = message.totalRuns
                                        } else {
                                            // Последующие крутки: добавляем еще одну пустую доску
                                            _boardRunouts.value = _boardRunouts.value + listOf(emptyList())
                                        }
                                    }
                                }
                                is OutgoingMessage.BlindsUp -> println("123") // todo
                                is OutgoingMessage.ErrorMessage -> println("123") // todo
                                is OutgoingMessage.LobbyUpdate -> {} // скипаем, это для другой страницы
                                is OutgoingMessage.OfferRunItMultipleTimes -> {
                                    _runItUiState.value = RunItUiState.AwaitingFavoriteConfirmation(
                                        underdogId = message.underdogId,
                                        times = message.times
                                    )
                                }
                                is OutgoingMessage.OfferRunItForUnderdog -> {
                                    _runItUiState.value = RunItUiState.AwaitingUnderdogChoice
                                }
                                is OutgoingMessage.PlayerJoined -> {
                                    Log.d("testPlayerJoined", message.player.toString())
                                    _roomInfo.update { currentRoom ->
                                        currentRoom?.copy(players = currentRoom.players + message.player)
                                    }
                                }
                                is OutgoingMessage.PlayerLeft -> {
                                    Log.d("testPlayerLeft", message.userId)
                                    _roomInfo.update { currentRoom ->
                                        currentRoom?.copy(players = currentRoom.players.filterNot { it.userId == message.userId })
                                    }
                                }
                                is OutgoingMessage.RunItMultipleTimesResult -> {
                                    Log.d("testRunMulRes", message.results.toString())
                                } // todo
                                is OutgoingMessage.SocialActionBroadcast -> println("123") // todo
                                is OutgoingMessage.TournamentWinner -> println("123") // todo
                                is OutgoingMessage.PlayerReadyUpdate -> {
                                    Log.d("testPlayerReady", "id: ${message.userId}, isReady: ${message.isReady}")
                                    _roomInfo.update { currentRoom ->
                                        currentRoom?.copy(
                                            players = currentRoom.players.map { player ->
                                                if (player.userId == message.userId) {
                                                    player.copy(isReady = message.isReady)
                                                } else {
                                                    player
                                                }
                                            }
                                        )
                                    }
                                }
                                is OutgoingMessage.PlayerStatusUpdate -> {
                                    Log.d("testUpdateStatus", message.status.toString())
                                    _roomInfo.update { currentRoom ->
                                        currentRoom?.copy(
                                            players = currentRoom.players.map { player ->
                                                if (player.userId == message.userId) {
                                                    player.copy(status = message.status)
                                                } else {
                                                    player
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Game WS Error: ${e.message}")
            }
        }
    }

    // --- Методы для отправки действий на сервер ---
    fun onFold() {
        lockActionPanel()
        sendAction(IncomingMessage.Fold())
    }
    fun onCheck() {
        lockActionPanel()
        sendAction(IncomingMessage.Check())
    }
    fun onCall() {
        lockActionPanel()
        sendAction(IncomingMessage.Call())
    }
    fun onBet(amount: Long) {
        lockActionPanel()
        sendAction(IncomingMessage.Bet(amount))
    }
    fun onReadyClick(isReady: Boolean) = sendAction(IncomingMessage.SetReady(isReady))
    fun onRunItChoice(times: Int) {
        sendAction(IncomingMessage.SelectRunCount(times))
        _runItUiState.value = RunItUiState.Hidden
    }
    fun onRunItConfirmation(accepted: Boolean) {
        sendAction(IncomingMessage.AgreeRunCount(accepted))
        _runItUiState.value = RunItUiState.Hidden
    }
    fun onSitAtTableClick(buyIn: Long) {
        sendAction(IncomingMessage.SitAtTable(buyIn))
    }

    private fun lockActionPanel() {
        if (_isActionPanelLocked.value) return
        _isActionPanelLocked.value = true
        lockJob = viewModelScope.launch {
            delay(3000L)
            _isActionPanelLocked.value = false
        }
    }

    private fun sendAction(action: IncomingMessage) {
        viewModelScope.launch {
            session?.sendSerialized(action)
        }
    }

    private fun decodeJwtAndGetUserId(token: String?): String? {
        try {
            val parts = token?.split(".") ?: return null
            if (parts.size < 2) return null
            val payload = String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
            // Simple regex to find the userId claim
            val userIdRegex = """"userId":"([^"]+)"""".toRegex()
            return userIdRegex.find(payload)?.groupValues?.get(1)
        } catch (e: Exception) {
            return null
        }
    }
}