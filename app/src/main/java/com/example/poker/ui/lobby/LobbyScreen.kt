package com.example.poker.ui.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poker.data.remote.dto.GameRoom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreateRoom: () -> Unit
) {
    val rooms by viewModel.rooms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Poker Rooms") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateRoom) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Room")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            items(rooms) { room ->
                RoomItem(room = room, onJoinClick = { /* TODO */ })
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
    }
}

@Composable
fun RoomItem(room: GameRoom, onJoinClick: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(room.name, style = MaterialTheme.typography.titleMedium)
            Text("${room.players.size} / 9 players", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { onJoinClick(room.roomId) }) {
            Text("Join")
        }
    }
}