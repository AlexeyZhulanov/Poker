package com.example.poker.data.remote.dto

// DTO для найденной в сети игры
data class DiscoveredGame(
    val serviceName: String,
    val hostAddress: String,
    val port: Int
)