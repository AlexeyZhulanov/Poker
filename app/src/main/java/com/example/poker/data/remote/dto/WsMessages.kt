package com.example.poker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//================================================
// Вспомогательные классы, используемые в сообщениях
//================================================
@Serializable
sealed interface OutsInfo {
    @Serializable
    @SerialName("outs.direct")
    data class DirectOuts(val cards: List<Card>) : OutsInfo
    @Serializable
    @SerialName("outs.runner_runner")
    data object RunnerRunner : OutsInfo
    @Serializable
    @SerialName("outs.drawing_dead")
    data object DrawingDead : OutsInfo
}

//===========================================
// ИСХОДЯЩИЕ (Сервер -> Клиент)
//===========================================

@Serializable
sealed interface OutgoingMessage {
    @Serializable
    @SerialName("out.game_state")
    data class GameStateUpdate(val state: GameState?) : OutgoingMessage
    @Serializable
    @SerialName("out.player_joined")
    data class PlayerJoined(val player: Player) : OutgoingMessage
    @Serializable
    @SerialName("out.player_left")
    data class PlayerLeft(val userId: String) : OutgoingMessage
    @Serializable
    @SerialName("out.error_message")
    data class ErrorMessage(val message: String) : OutgoingMessage
    @Serializable
    @SerialName("out.blinds_up")
    data class BlindsUp(val smallBlind: Long, val bigBlind: Long, val ante: Long, val level: Int, val levelTime: Long) : OutgoingMessage
    @Serializable
    @SerialName("out.tournament_winner")
    data class TournamentWinner(val winnerUserId: String) : OutgoingMessage
    @Serializable
    @SerialName("out.start_board_run")
    data class StartBoardRun(val runIndex: Int, val totalRuns: Int) : OutgoingMessage
    @Serializable
    @SerialName("out.equity_update")
    data class AllInEquityUpdate(val equities: Map<String, Double>, val outs: Map<String, OutsInfo> = emptyMap(), val runIndex: Int) : OutgoingMessage
    @Serializable
    @SerialName("out.board_result")
    data class BoardResult(val payments: List<Pair<String, Long>>) : OutgoingMessage
    @Serializable
    @SerialName("out.run_multiple_offer")
    data class OfferRunItMultipleTimes(val underdogId: String, val times: Int, val expiresAt: Long) : OutgoingMessage
    @Serializable
    @SerialName("out.run_offer_underdog")
    data class OfferRunItForUnderdog(val expiresAt: Long) : OutgoingMessage
    @Serializable
    @SerialName("out.social_action_broadcast")
    data class SocialActionBroadcast(val fromPlayerId: String, val action: SocialAction) : OutgoingMessage
    @Serializable
    @SerialName("out.lobby_update")
    data class LobbyUpdate(val rooms: List<GameRoom>) : OutgoingMessage
    @Serializable
    @SerialName("out.player_ready_update")
    data class PlayerReadyUpdate(val userId: String, val isReady: Boolean) : OutgoingMessage
    @Serializable
    @SerialName("out.player_status_update")
    data class PlayerStatusUpdate(val userId: String, val status: PlayerStatus, val stack: Long) : OutgoingMessage
    @Serializable
    @SerialName("out.connection_status")
    data class ConnectionStatusUpdate(val userId: String, val isConnected: Boolean) : OutgoingMessage
}


//===========================================
// ВХОДЯЩИЕ (Клиент -> Сервер)
//===========================================
@Serializable
sealed interface IncomingMessage {
    @Serializable
    @SerialName("in.fold")
    data class Fold(val temp: String = "") : IncomingMessage
    @Serializable
    @SerialName("in.bet")
    data class Bet(val amount: Long) : IncomingMessage
    @Serializable
    @SerialName("in.check")
    data class Check(val temp: String = "") : IncomingMessage
    @Serializable
    @SerialName("in.call")
    data class Call(val temp: String = "") : IncomingMessage
    @Serializable
    @SerialName("in.run_count")
    data class SelectRunCount(val times: Int) : IncomingMessage
    @Serializable
    @SerialName("in.agree_run_count")
    data class AgreeRunCount(val isAgree: Boolean) : IncomingMessage
    @Serializable
    @SerialName("in.social_action")
    data class PerformSocialAction(val action: SocialAction) : IncomingMessage
    @Serializable
    @SerialName("in.set_ready")
    data class SetReady(val isReady: Boolean) : IncomingMessage
    @Serializable
    @SerialName("in.sit_at_table")
    data object SitAtTable : IncomingMessage
}

@Serializable
sealed interface SocialAction {
    @Serializable
    @SerialName("social.sticker")
    data class ShowSticker(val stickerId: String) : SocialAction
    @Serializable
    @SerialName("social.draw")
    data class DrawLine(val points: List<Point>, val color: String) : SocialAction
    @Serializable
    @SerialName("social.throw")
    data class ThrowItem(val itemId: String, val targetUserId: String) : SocialAction
}

@Serializable
data class Point(val x: Float, val y: Float)