package com.example.poker.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    CASH,
    TOURNAMENT
}

@Serializable
data class BlindLevel(
    val level: Int,
    val smallBlind: Long,
    val bigBlind: Long
)

// Все эти классы должны быть идентичны серверным моделям
@Serializable
data class GameRoom(
    val roomId: String,
    val name: String,
    val gameMode: GameMode,
    val players: List<Player>,
    val maxPlayers: Int = 9,
    val ownerId: String,
    val blindStructure: List<BlindLevel>? = null,
    val levelDurationMinutes: Int? = null,
)

@Serializable
data class Player(val userId: String, val username: String, val stack: Long)

@Serializable
enum class GameStage { PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN }

@Serializable
data class Card(val rank: String, val suit: String) // Для простоты используем строки

@Serializable
data class PlayerState(
    val player: Player,
    val cards: List<Card> = emptyList(),
    val currentBet: Long = 0,
    val hasFolded: Boolean = false,
    val isAllIn: Boolean = false
)

@Serializable
data class GameState(
    val stage: GameStage,
    val communityCards: List<Card>,
    val pot: Long,
    val playerStates: List<PlayerState>,
    val activePlayerPosition: Int
)