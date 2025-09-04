package com.example.poker.server.util

import com.example.poker.server.domain.model.User
import io.ktor.util.AttributeKey

// Создаем типизированный ключ для хранения объекта User в атрибутах запроса
val UserAttributeKey = AttributeKey<User>("UserAttributeKey")
