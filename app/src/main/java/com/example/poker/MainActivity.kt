package com.example.poker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.poker.navigation.Screen
import com.example.poker.ui.auth.AuthViewModel
import com.example.poker.ui.auth.LoginScreen
import com.example.poker.ui.auth.RegisterScreen
import com.example.poker.ui.createroom.CreateRoomScreen
import com.example.poker.ui.createroom.CreateRoomViewModel
import com.example.poker.ui.lobby.LobbyScreen
import com.example.poker.ui.lobby.LobbyViewModel
import com.example.poker.ui.settings.SettingsScreen
import com.example.poker.ui.settings.SettingsViewModel
import com.example.poker.ui.theme.PokerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokerTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route // Начинаем с экрана входа
                ) {
                    composable(Screen.Login.route) {
                        val viewModel: AuthViewModel = hiltViewModel()
                        LoginScreen(
                            viewModel = viewModel,
                            onNavigateToRegister = {
                                navController.navigate(Screen.Register.route)
                            },
                            onLoginSuccess = {
                                navController.navigate(Screen.Lobby.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Register.route) {
                        val viewModel: AuthViewModel = hiltViewModel()
                        RegisterScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = {
                                navController.popBackStack()
                            },
                            onRegisterSuccess = {
                                navController.navigate(Screen.Lobby.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Lobby.route) {
                        val viewModel: LobbyViewModel = hiltViewModel()
                        LobbyScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToCreateRoom = { navController.navigate(Screen.CreateRoom.route) }
                        )
                    }
                    composable(Screen.Settings.route) {
                        val viewModel: SettingsViewModel = hiltViewModel()
                        SettingsScreen(
                            viewModel = viewModel,
                            onLogout = {
                                navController.navigate(Screen.Login.route) { popUpTo(0) }
                            }
                        )
                    }
                    composable(Screen.CreateRoom.route) {
                        val viewModel: CreateRoomViewModel = hiltViewModel()
                        CreateRoomScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun TestScreen(viewModel: TestViewModel) {
//    val logs by viewModel.logs.collectAsState()
//    val gameState by viewModel.gameState.collectAsState()
//    var betAmount by remember { mutableStateOf("") }
//    val myUserId by viewModel.myUserId.collectAsState()
//    var roomIdInput by remember { mutableStateOf("") }
//
//    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
//        // Верхняя панель с кнопками
//        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//            Button(onClick = { viewModel.register() }) { Text("Register") }
//            Button(onClick = { viewModel.login() }) { Text("Login") }
//            Button(onClick = { viewModel.createRoom() }) { Text("Create room") }
//        }
//        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//            Button(onClick = {viewModel.clearLogs()}) { Text("Clear logs") }
//            Button(onClick = {viewModel.startGame(roomIdInput)}) { Text("Start game") }
//        }
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.padding(top = 2.dp)
//        ) {
//            OutlinedTextField(
//                value = roomIdInput,
//                onValueChange = { roomIdInput = it },
//                label = { Text("Room ID") },
//                modifier = Modifier.weight(1f)
//            )
//            Spacer(modifier = Modifier.width(8.dp))
//            Button(onClick = { viewModel.joinRoom(roomIdInput) }) { Text("Join") }
//        }
//
//        HorizontalDivider(
//            modifier = Modifier.padding(vertical = 8.dp),
//            thickness = DividerDefaults.Thickness,
//            color = DividerDefaults.color
//        )
//
//        // Игровая информация
//        gameState?.let { state ->
//            Text("Stage: ${state.stage}", style = MaterialTheme.typography.titleMedium)
//            Text("Pot: ${state.pot}")
//            Text("Community Cards: ${state.communityCards.joinToString { it.rank.toString() + it.suit.toString() }}")
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Информация об игроках
//            state.playerStates.forEachIndexed { index, playerState ->
//                val indicator = if (index == state.activePlayerPosition) "->" else ""
//                Text("$indicator ${playerState.player.username} (${playerState.player.stack}) Bet: ${playerState.currentBet} Cards: ${playerState.cards.joinToString { it.rank.toString() + it.suit.toString() }}")
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            val activePlayerId = state.playerStates.getOrNull(state.activePlayerPosition)?.player?.userId
//            if (activePlayerId == myUserId) { // The magic condition!
//                Spacer(modifier = Modifier.height(16.dp))
//                Text("Your Turn!", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.Top,
//                    modifier = Modifier.padding(top=8.dp),) {
//                    Button(onClick = { viewModel.sendFold() }) { Text("Fold") }
//                    Button(onClick = { viewModel.sendCheck() }) { Text("Check") }
//                    Button(onClick = { viewModel.sendCall() }) { Text("Call") }
//                }
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.Top,
//                    modifier = Modifier.padding(top=8.dp),) {
//                    Button(onClick = { viewModel.sendBet(betAmount.toLongOrNull() ?: 0) }) { Text("Bet/Raise") }
//                    OutlinedTextField(
//                        value = betAmount,
//                        onValueChange = { betAmount = it },
//                        label = { Text("am") },
//                        modifier = Modifier.height(50.dp)
//                    )
//                }
//            }
//        }
//
//        // Логи
//        Text("Logs:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
//        SelectionContainer {
//            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
//                items(logs) { logMessage ->
//                    Text(logMessage, style = MaterialTheme.typography.bodySmall)
//                }
//            }
//        }
//    }
//}