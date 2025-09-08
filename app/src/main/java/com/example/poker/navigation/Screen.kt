package com.example.poker.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object Lobby : Screen("lobby_screen")
    data object Settings : Screen("settings_screen")
    data object CreateRoom : Screen("create_room_screen/{isOffline}") {
        fun createRoute(isOffline: Boolean) = "create_room_screen/$isOffline"
    }
    data object Game : Screen("game_screen?url={gameUrl}&isOffline={isOffline}") {
        fun createRoute(url: String, isOffline: Boolean): String {
            // Кодируем URL, чтобы он мог безопасно передаваться как параметр
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            return "game_screen?url=$encodedUrl&isOffline=$isOffline"
        }
    }
}