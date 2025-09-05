package com.example.poker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.poker.di.AuthEvent
import com.example.poker.di.AuthEventBus
import com.example.poker.navigation.Screen
import com.example.poker.ui.auth.AuthViewModel
import com.example.poker.ui.auth.LoginScreen
import com.example.poker.ui.auth.RegisterScreen
import com.example.poker.ui.createroom.CreateRoomScreen
import com.example.poker.ui.createroom.CreateRoomViewModel
import com.example.poker.ui.game.GameScreen
import com.example.poker.ui.game.GameViewModel
import com.example.poker.ui.lobby.LobbyScreen
import com.example.poker.ui.lobby.LobbyViewModel
import com.example.poker.ui.lobby.OfflineLobbyViewModel
import com.example.poker.ui.settings.SettingsScreen
import com.example.poker.ui.settings.SettingsViewModel
import com.example.poker.ui.theme.PokerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authEventBus: AuthEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokerTheme {
                val navController = rememberNavController()

                // Слушаем события от шины
                LaunchedEffect(Unit) {
                    authEventBus.events.collectLatest { event ->
                        when (event) {
                            AuthEvent.SessionExpired -> {
                                // Перекидываем на экран входа с полной очисткой стека
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0)
                                }
                            }
                            AuthEvent.KickedFromRoom -> {
                                navController.navigate(Screen.Lobby.route) {
                                    popUpTo(Screen.Lobby.route) { inclusive = true }
                                }
                                Toast.makeText(applicationContext, "Вы были исключены из комнаты", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Lobby.route
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
                        val onlineViewModel: LobbyViewModel = hiltViewModel()
                        val offlineViewModel: OfflineLobbyViewModel = hiltViewModel()
                        LobbyScreen(
                            onlineViewModel = onlineViewModel,
                            offlineViewModel = offlineViewModel,
                            onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                            onNavigateToCreateRoom = { isOffline ->
                                navController.navigate(Screen.CreateRoom.createRoute(isOffline))
                            },
                            onNavigateToOnlineGame = { roomId ->
                                val url = "ws://amessenger.ru:8080/play/$roomId"
                                navController.navigate(Screen.Game.createRoute(url, isOffline = false))
                            },
                            onNavigateToOfflineGame = { gameUrl ->
                                navController.navigate(Screen.Game.createRoute(gameUrl, isOffline = true))
                            }
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
                    composable(
                        route = Screen.CreateRoom.route,
                        arguments = listOf(navArgument("isOffline") { type = NavType.BoolType })
                    ) {
                        val viewModel: CreateRoomViewModel = hiltViewModel()
                        CreateRoomScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onRoomCreated = { url, isOffline ->
                                navController.navigate(Screen.Game.createRoute(url, isOffline)) {
                                    popUpTo(Screen.Lobby.route)
                                }
                            }
                        )
                    }
                    composable(
                        route = Screen.Game.route,
                        arguments = listOf(
                            navArgument("gameUrl") { type = NavType.StringType },
                            navArgument("isOffline") { type = NavType.BoolType }
                        )
                    ) {
                        val viewModel: GameViewModel = hiltViewModel()
                        GameScreen(viewModel = viewModel,
                            onNavigateToLobby = {
                                navController.navigate(Screen.Lobby.route) {
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
