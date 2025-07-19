package com.example.poker.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object Lobby : Screen("lobby_screen")
    data object Settings : Screen("settings_screen")
    data object CreateRoom : Screen("create_room_screen")
    data object Game : Screen("game_screen/{roomId}") {
        fun createRoute(roomId: String) = "game_screen/$roomId"
    }
}