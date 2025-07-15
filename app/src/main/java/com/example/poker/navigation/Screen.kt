package com.example.poker.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login_screen")
    data object Register : Screen("register_screen")
    data object Lobby : Screen("lobby_screen")
    data object Settings : Screen("settings_screen")
}