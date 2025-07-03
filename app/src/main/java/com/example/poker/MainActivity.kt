package com.example.poker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.poker.ui.TestViewModel
import com.example.poker.ui.theme.PokerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokerTheme {
                TestScreen(viewModel)
            }
        }
    }
}

@Composable
fun TestScreen(viewModel: TestViewModel) {
    val logs by viewModel.logs.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    var betAmount by remember { mutableStateOf("") }
    val myUserId by viewModel.myUserId.collectAsState()
    var roomIdInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Верхняя панель с кнопками
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.register() }) { Text("Register") }
            Button(onClick = { viewModel.login() }) { Text("Login") }
            Button(onClick = { viewModel.createRoom() }) { Text("Create Room") }
        }
        Button(onClick = {viewModel.clearLogs()}) { Text("Clear logs") }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = roomIdInput,
                onValueChange = { roomIdInput = it },
                label = { Text("Room ID") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { viewModel.joinRoom(roomIdInput) }) { Text("Join") }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        // Игровая информация
        gameState?.let { state ->
            Text("Stage: ${state.stage}", style = MaterialTheme.typography.titleMedium)
            Text("Pot: ${state.pot}")
            Text("Community Cards: ${state.communityCards.joinToString { it.rank + it.suit }}")
            Spacer(modifier = Modifier.height(8.dp))

            // Информация об игроках
            state.playerStates.forEachIndexed { index, playerState ->
                val indicator = if (index == state.activePlayerPosition) "->" else ""
                Text("$indicator ${playerState.player.username} (${playerState.player.stack}) Bet: ${playerState.currentBet} Cards: ${playerState.cards.joinToString { it.rank + it.suit }}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            val activePlayerId = state.playerStates.getOrNull(state.activePlayerPosition)?.player?.userId
            if (activePlayerId == myUserId) { // The magic condition!
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your Turn!", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top=8.dp)) {
                    Button(onClick = { viewModel.sendFold() }) { Text("Fold") }
                    Button(onClick = { viewModel.sendCheck() }) { Text("Check") }
                    Button(onClick = { viewModel.sendBet(betAmount.toLongOrNull() ?: 0) }) { Text("Bet/Raise") }
                    OutlinedTextField(
                        value = betAmount,
                        onValueChange = { betAmount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.width(100.dp)
                    )
                }
            }
            // Кнопки действий
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                Button(onClick = { viewModel.sendFold() }) { Text("Fold") }
//                Button(onClick = { viewModel.sendCheck() }) { Text("Check") }
//                Button(onClick = { viewModel.sendBet(betAmount.toLongOrNull() ?: 0) }) { Text("Bet/Raise") }
//                OutlinedTextField(
//                    value = betAmount,
//                    onValueChange = { betAmount = it },
//                    label = { Text("Amount") },
//                    modifier = Modifier.width(100.dp)
//                )
//            }
        }

        // Логи
        Text("Logs:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
        SelectionContainer {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(logs) { logMessage ->
                    Text(logMessage, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}