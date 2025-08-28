package com.example.poker.data.remote.dto

import com.example.poker.domain.model.Rank
import com.example.poker.domain.model.Suit
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
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
sealed interface PlayerAction {
    val id: String
    @Serializable
    @SerialName("action.fold")
    data class Fold(override val id: String) : PlayerAction
    @Serializable
    @SerialName("action.check")
    data class Check(override val id: String) : PlayerAction
    @Serializable
    @SerialName("action.call")
    data class Call(val amount: Long, override val id: String) : PlayerAction
    @Serializable
    @SerialName("action.bet")
    data class Bet(val amount: Long, override val id: String) : PlayerAction
    @Serializable
    @SerialName("action.raise")
    data class Raise(val amount: Long, override val id: String) : PlayerAction
    @Serializable
    @SerialName("action.allin")
    data class AllIn(override val id: String) : PlayerAction
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
) {
    fun toGameRoomUi(): com.example.poker.ui.game.GameRoom = com.example.poker.ui.game.GameRoom(
        roomId = roomId,
        name = name,
        gameMode = gameMode,
        players = players.toImmutableList(),
        maxPlayers = maxPlayers,
        ownerId = ownerId,
        buyIn = buyIn,
        blindStructureType = blindStructureType,
        blindStructure = blindStructure?.toImmutableList()
    )
}

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
data class Card(val rank: Rank, val suit: Suit)

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
) {
    fun toPlayerStateUi(): com.example.poker.ui.game.PlayerState = com.example.poker.ui.game.PlayerState(
        player = player,
        cards = cards.toImmutableList(),
        currentBet = currentBet,
        handContribution = handContribution,
        hasActedThisRound = hasActedThisRound,
        hasFolded = hasFolded,
        isAllIn = isAllIn,
        lastAction = lastAction
    )
}

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
) {
    fun toGameStateUi(): com.example.poker.ui.game.GameState = com.example.poker.ui.game.GameState(
        roomId = roomId,
        stage = stage,
        communityCards = communityCards.toImmutableList(),
        pot = pot,
        playerStates = playerStates.map { it.toPlayerStateUi() }.toImmutableList(),
        dealerPosition = dealerPosition,
        activePlayerPosition = activePlayerPosition,
        lastRaiseAmount = lastRaiseAmount,
        bigBlindAmount = bigBlindAmount,
        amountToCall = amountToCall,
        lastAggressorPosition = lastAggressorPosition,
        runIndex = runIndex,
        turnExpiresAt = turnExpiresAt
    )
}

@Serializable
enum class PlayerStatus {
    SPECTATING,
    SITTING_OUT,
    IN_HAND
}