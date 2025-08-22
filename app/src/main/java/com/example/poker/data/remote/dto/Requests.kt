package com.example.poker.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val accessToken: String, val refreshToken: String)

@Serializable
data class CreateRoomRequest(
    val name: String,
    val gameMode: GameMode,
    val maxPlayers: Int,
    val smallBlind: Long?,
    val bigBlind: Long?,
    val blindStructureType: BlindStructureType? = null
)