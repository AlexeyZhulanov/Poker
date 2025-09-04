package com.example.poker.ui.createroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.poker.shared.dto.BlindStructureType
import com.example.poker.shared.dto.GameMode

@Composable
fun CreateRoomScreen(
    viewModel: CreateRoomViewModel,
    onRoomCreated: (roomId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Состояния для полей формы
    val roomName by viewModel.roomName.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val selectedStructure by viewModel.blindStructureType.collectAsState()
    //val initialStack by viewModel.initialStack.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Слушаем событие навигации
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { roomId ->
            if (roomId != null) {
                onRoomCreated(roomId)
            } else {
                onNavigateBack()
            }
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                Text("Create New Room", style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(value = roomName, onValueChange = viewModel::onRoomNameChange, label = { Text("Room Name") })

                // Переключатель режима игры
                Row(Modifier.padding(vertical = 16.dp)) {
                    Text("Cash")
                    Switch(checked = gameMode == GameMode.TOURNAMENT, onCheckedChange = { isTournament ->
                        viewModel.onGameModeChange(if (isTournament) GameMode.TOURNAMENT else GameMode.CASH)
                    })
                    Text("Tournament")
                }

                //OutlinedTextField(value = initialStack, onValueChange = viewModel::onStackChange)

                // Условное отображение полей
                if (gameMode == GameMode.CASH) {
                    // TODO: Поля для кэш-игры (блайнды, стек)
                    OutlinedTextField(value = "", onValueChange = {}, label = { Text("Small Blind") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tournament Speed:", style = MaterialTheme.typography.titleMedium)

                    // Группа RadioButton для выбора
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        BlindStructureType.entries.forEach { type ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = (type == selectedStructure),
                                    onClick = { viewModel.onBlindStructureChange(type) }
                                )
                                Text(text = type.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = { viewModel.onCreateClick() }, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create")
                    }
                }
            }
        }
    }
}