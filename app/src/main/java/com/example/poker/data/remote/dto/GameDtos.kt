package com.example.poker.data.remote.dto

import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    CASH,
    TOURNAMENT
}

@Serializable
enum class BlindStructureType {
    STANDARD,
    FAST,
    TURBO
}

@Serializable
data class BlindLevel(
    val level: Int,
    val smallBlind: Long,
    val bigBlind: Long,
    val ante: Long = 0L
)

@Serializable
data class GameRoom(
    val roomId: String,
    val name: String,
    val gameMode: GameMode,
    val players: List<Player>,
    val maxPlayers: Int = 9,
    val ownerId: String,
    val blindStructureType: BlindStructureType? = null,
    val blindStructure: List<BlindLevel>? = null,
    val gameStarted: Boolean = false
)

@Serializable
data class Player(val userId: String, val username: String, val stack: Long, val isReady: Boolean = false)

@Serializable
enum class GameStage { PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN }

@Serializable
data class Card(val rank: Rank, val suit: Suit)

@Serializable
data class PlayerState(
    val player: Player,
    val cards: List<Card> = emptyList(),
    val currentBet: Long = 0,
    val handContribution: Long = 0,
    val hasActedThisRound: Boolean = false,
    val hasFolded: Boolean = false,
    val isAllIn: Boolean = false
)

@Serializable
data class GameState(
    val roomId: String,
    val stage: GameStage = GameStage.PRE_FLOP,
    val communityCards: List<Card> = emptyList(),
    val pot: Long = 0,
    val playerStates: List<PlayerState> = emptyList(),
    val dealerPosition: Int = 0,
    val activePlayerPosition: Int = 0,
    val lastRaiseAmount: Long = 0,
    val amountToCall: Long = 0, // Сколько нужно доставить, чтобы уравнять
    val lastAggressorPosition: Int? = null,
    val showdownResults: Map<String, List<Card>>? = null
)