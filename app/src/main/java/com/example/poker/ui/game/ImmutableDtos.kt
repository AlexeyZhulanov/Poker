package com.example.poker.ui.game

import com.example.poker.shared.dto.BlindLevel
import com.example.poker.shared.dto.BlindStructureType
import com.example.poker.shared.dto.GameMode
import com.example.poker.shared.dto.GameStage
import com.example.poker.shared.dto.Player
import com.example.poker.shared.dto.PlayerAction
import com.example.poker.shared.model.Card
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

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
    companion object {
        fun fromUserInput(room: com.example.poker.shared.dto.GameRoom): GameRoom = GameRoom(
            roomId = room.roomId,
            name = room.name,
            gameMode = room.gameMode,
            players = room.players.toImmutableList(),
            maxPlayers = room.maxPlayers,
            ownerId = room.ownerId,
            buyIn = room.buyIn,
            blindStructureType = room.blindStructureType,
            blindStructure = room.blindStructure?.toImmutableList()
        )
    }
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
    companion object {
        fun fromUserInput(state: com.example.poker.shared.dto.PlayerState): PlayerState = PlayerState(
            player = state.player,
            cards = state.cards.toImmutableList(),
            currentBet = state.currentBet,
            handContribution = state.handContribution,
            hasActedThisRound = state.hasActedThisRound,
            hasFolded = state.hasFolded,
            isAllIn = state.isAllIn,
            lastAction = state.lastAction
        )
    }
}

data class GameState(
    val roomId: String,
    val stage: GameStage = GameStage.PRE_FLOP,
    val communityCards: ImmutableList<Card> = persistentListOf(),
    val pot: Long = 0,
    val playerStates: ImmutableList<PlayerState> = persistentListOf(),
    val dealerPosition: Int = 0,
    val activePlayerPosition: Int = 0,
    val lastRaiseAmount: Long = 0,
    val bigBlindAmount: Long = 0,
    val amountToCall: Long = 0, // Сколько нужно доставить, чтобы уравнять
    val lastAggressorPosition: Int? = null,
    val runIndex: Int? = null,
    val turnExpiresAt: Long? = null
) {
    companion object {
        fun fromUserInput(state: com.example.poker.shared.dto.GameState): GameState = GameState(
            roomId = state.roomId,
            stage = state.stage,
            communityCards = state.communityCards.toImmutableList(),
            pot = state.pot,
            playerStates = state.playerStates.map { PlayerState.fromUserInput(it) }.toImmutableList(),
            dealerPosition = state.dealerPosition,
            activePlayerPosition = state.activePlayerPosition,
            lastRaiseAmount = state.lastRaiseAmount,
            bigBlindAmount = state.bigBlindAmount,
            amountToCall = state.amountToCall,
            lastAggressorPosition = state.lastAggressorPosition,
            runIndex = state.runIndex,
            turnExpiresAt = state.turnExpiresAt
        )
    }
}