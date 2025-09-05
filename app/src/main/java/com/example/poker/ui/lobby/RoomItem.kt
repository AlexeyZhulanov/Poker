package com.example.poker.ui.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoomItem(roomId: String, roomName: String, roomSize: Int, onJoinClick: (String) -> Unit, isEnabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(roomName, style = MaterialTheme.typography.titleMedium)
            Text("$roomSize / 9 players", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { onJoinClick(roomId) }, enabled = isEnabled) {
            Text("Join")
        }
    }
}