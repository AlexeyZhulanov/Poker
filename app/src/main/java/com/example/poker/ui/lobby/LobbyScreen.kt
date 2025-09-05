package com.example.poker.ui.lobby

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.poker.data.remote.dto.DiscoveredGame
import com.example.poker.ui.game.ReconnectingText
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    onlineViewModel: LobbyViewModel,
    offlineViewModel: OfflineLobbyViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateRoom: (isOffline: Boolean) -> Unit,
    onNavigateToOnlineGame: (roomId: String) -> Unit,
    onNavigateToOfflineGame: (gameUrl: String) -> Unit
) {
    val isReconnecting by onlineViewModel.isReconnecting.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Online", "Offline")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { if(isReconnecting) ReconnectingText() else Text("Poker Rooms") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToCreateRoom(selectedTab == 1) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Room")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> OnlineLobbyContent(
                    viewModel = onlineViewModel,
                    onNavigateToGame = { roomId ->
                        onNavigateToOnlineGame(roomId)
                    }
                )
                1 -> OfflineLobbyContent(
                    viewModel = offlineViewModel,
                    onCreateGame = { onNavigateToCreateRoom(true) },
                    onJoinGame = { game ->
                        // Для оффлайн игры мы формируем URL из IP
                        val encodedUrl = URLEncoder.encode("ws://${game.hostAddress}:${game.port}/play", "UTF-8")
                        onNavigateToOfflineGame(encodedUrl)
                    }
                )
            }
        }
    }
}

@Composable
fun OnlineLobbyContent(
    viewModel: LobbyViewModel,
    onNavigateToGame: (roomId: String) -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.navigateToGameEvent.collect { roomId ->
            onNavigateToGame(roomId)
        }
    }

    // Этот эффект следит за жизненным циклом
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.connect()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Эта часть вызовется, когда экран будет уничтожен
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.disconnect()
        }
    }
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(rooms) { room ->
            RoomItem(roomId = room.roomId, roomName = room.name, roomSize = room.players.size,
                onJoinClick = { viewModel.onJoinClick(room.roomId) }, isEnabled = !isLoading)
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}

@Composable
fun OfflineLobbyContent(
    viewModel: OfflineLobbyViewModel,
    onCreateGame: () -> Unit,
    onJoinGame: (discoveredGame: DiscoveredGame) -> Unit
) {
    val discoveredGames by viewModel.discoveredGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Запускаем/останавливаем поиск игр при входе/выходе с экрана
    DisposableEffect(Unit) {
        viewModel.startDiscovery()
        onDispose {
            viewModel.stopDiscovery()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Для игры с друзьями, один должен создать точку доступа Wi-Fi (Hotspot), а остальные - подключиться к ней.", textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }) {
            Text("Открыть настройки Wi-Fi")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCreateGame, modifier = Modifier.fillMaxWidth()) {
            Text("Создать игру (быть Хостом)")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text("Найденные игры:", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(discoveredGames) { game ->
                RoomItem("offline_room", game.serviceName, 1,
                    { viewModel.joinGame(game, onJoinGame) }, !isLoading)
            }
        }
    }
}