package com.example.poker.shared.dto

import com.example.poker.shared.model.Card
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

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
sealed interface PlayerAction {
    val id: String // Уникальный ID для отслеживания анимации

    @Serializable
    @SerialName("action.fold")
    data class Fold(override val id: String = UUID.randomUUID().toString()) : PlayerAction

    @Serializable
    @SerialName("action.check")
    data class Check(override val id: String = UUID.randomUUID().toString()) : PlayerAction

    @Serializable
    @SerialName("action.call")
    data class Call(val amount: Long, override val id: String = UUID.randomUUID().toString()) : PlayerAction

    @Serializable
    @SerialName("action.bet")
    data class Bet(val amount: Long, override val id: String = UUID.randomUUID().toString()) : PlayerAction

    @Serializable
    @SerialName("action.raise")
    data class Raise(val amount: Long, override val id: String = UUID.randomUUID().toString()) : PlayerAction

    @Serializable
    @SerialName("action.allin")
    data class AllIn(override val id: String = UUID.randomUUID().toString()) : PlayerAction
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
    val buyIn: Long,
    val blindStructureType: BlindStructureType? = null,
    val blindStructure: List<BlindLevel>? = null
)

@Serializable
data class Player(
    val userId: String,
    val username: String,
    val stack: Long,
    val status: PlayerStatus = PlayerStatus.SPECTATING,
    val isReady: Boolean = false,
    val missedTurns: Int = 0,
    val isConnected: Boolean = true
)

@Serializable
enum class GameStage { PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN }

@Serializable
data class PlayerState(
    val player: Player,
    val cards: List<Card> = emptyList(),
    val currentBet: Long = 0,
    val handContribution: Long = 0,
    val hasActedThisRound: Boolean = false,
    val hasFolded: Boolean = false,
    val isAllIn: Boolean = false,
    val lastAction: PlayerAction? = null
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
    val bigBlindAmount: Long = 0,
    val amountToCall: Long = 0, // Сколько нужно доставить, чтобы уравнять
    val lastAggressorPosition: Int? = null,
    val runIndex: Int? = null,
    val turnExpiresAt: Long? = null
)

@Serializable
enum class PlayerStatus {
    SPECTATING,
    SITTING_OUT,
    IN_HAND
}