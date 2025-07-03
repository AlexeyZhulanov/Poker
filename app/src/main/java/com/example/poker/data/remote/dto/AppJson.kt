package com.example.poker.data.remote.dto

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

// Создаем наш настроенный экземпляр Json
val AppJson = Json {
    // Говорим ему игнорировать неизвестные ключи (полезно, как мы уже выяснили)
    ignoreUnknownKeys = true

    // Это самый важный блок. Здесь мы регистрируем все наши "запечатанные" иерархии.
    serializersModule = SerializersModule {
        // Регистрируем иерархию для OutgoingMessage
        polymorphic(OutgoingMessage::class) {
            subclass(OutgoingMessage.GameStateUpdate::class)
            subclass(OutgoingMessage.PlayerJoined::class)
            subclass(OutgoingMessage.PlayerLeft::class)
            subclass(OutgoingMessage.ErrorMessage::class)
            subclass(OutgoingMessage.AllInEquityUpdate::class)
            subclass(OutgoingMessage.RunItMultipleTimesResult::class)
            subclass(OutgoingMessage.TournamentWinner::class)
            subclass(OutgoingMessage.BlindsUp::class)
            subclass(OutgoingMessage.OfferRunItMultipleTimes::class)
            subclass(OutgoingMessage.SocialActionBroadcast::class)
        }
        // Регистрируем иерархию для IncomingMessage
        polymorphic(IncomingMessage::class) {
            subclass(IncomingMessage.Fold::class)
            subclass(IncomingMessage.Check::class)
            subclass(IncomingMessage.Bet::class)
            subclass(IncomingMessage.SelectRunCount::class)
            subclass(IncomingMessage.PerformSocialAction::class)
        }
        // ... и так далее для ВСЕХ sealed-классов/интерфейсов, которые вы сериализуете.
        // Например, SocialAction и OutsInfo
        polymorphic(SocialAction::class) {
            subclass(SocialAction.ShowSticker::class)
            subclass(SocialAction.DrawLine::class)
            subclass(SocialAction.ThrowItem::class)
        }
        polymorphic(OutsInfo::class) {
            subclass(OutsInfo.DirectOuts::class)
            subclass(OutsInfo.RunnerRunner::class)
            subclass(OutsInfo.DrawingDead::class)
        }
    }
}