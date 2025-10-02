package com.example.poker.shared.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.security.Principal
import java.util.UUID

// Модель для передачи данных о пользователе внутри приложения, passwordHash нельзя покидать сервер
@Serializable
data class User(
    val id: @Contextual UUID,
    val username: String,
    val email: String,
    val passwordHash: String,
    val cashBalance: @Contextual BigDecimal
) : Principal {
    override fun getName(): String? {
        return id.toString()
    }
}