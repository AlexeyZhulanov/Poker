package com.example.poker.ui.game

import com.example.poker.data.remote.dto.BlindLevel
import com.example.poker.data.remote.dto.BlindStructureType
import com.example.poker.data.remote.dto.Card
import com.example.poker.data.remote.dto.GameMode
import com.example.poker.data.remote.dto.GameRoom
import com.example.poker.data.remote.dto.GameStage
import com.example.poker.data.remote.dto.Player
import com.example.poker.data.remote.dto.PlayerAction
import com.example.poker.data.remote.dto.PlayerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class GameRoom(
    val roomId: String,
    val name: String,
    val gameMode: GameMode,
    val players: ImmutableList<Player>,
    val maxPlayers: Int = 9,
    val ownerId: String,
    val buyIn: Long,
    val blindStructureType: BlindStructureType? = null,
    val blindStructure: ImmutableList<BlindLevel>? = null
) {
    fun toGameRoom(): GameRoom = GameRoom(
        roomId = roomId,
        name = name,
        gameMode = gameMode,
        players = players,
        maxPlayers = maxPlayers,
        ownerId = ownerId,
        buyIn = buyIn,
        blindStructureType = blindStructureType,
        blindStructure = blindStructure
    )
}

data class PlayerState(
    val player: Player,
    val cards: ImmutableList<Card> = persistentListOf(),
    val currentBet: Long = 0,
    val handContribution: Long = 0,
    val hasActedThisRound: Boolean = false,
    val hasFolded: Boolean = false,
    val isAllIn: Boolean = false,
    val lastAction: PlayerAction? = null
) {
    fun toPlayerState(): PlayerState = PlayerState(
        player = player,
        cards = cards,
        currentBet = currentBet,
        handContribution = handContribution,
        hasActedThisRound = hasActedThisRound,
        hasFolded = hasFolded,
        isAllIn = isAllIn,
        lastAction = lastAction
    )
}

data class GameState(
    val roomId: String,
    val stage: GameStage = GameStage.PRE_FLOP,
    val communityCards: ImmutableList<Card> = persistentListOf(),
    val pot: Long = 0,
    val playerStates: ImmutableList<com.example.poker.ui.game.PlayerState> = persistentListOf(),
    val dealerPosition: Int = 0,
    val activePlayerPosition: Int = 0,
    val lastRaiseAmount: Long = 0,
    val bigBlindAmount: Long = 0,
    val amountToCall: Long = 0, // Сколько нужно доставить, чтобы уравнять
    val lastAggressorPosition: Int? = null,
    val runIndex: Int? = null,
    val turnExpiresAt: Long? = null
) {
    fun toGameState(): GameState = GameState(
        roomId = roomId,
        stage = stage,
        communityCards = communityCards,
        pot = pot,
        playerStates = playerStates,
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