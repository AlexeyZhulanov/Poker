package com.example.poker.shared.util

import com.example.poker.shared.model.User
import io.ktor.util.AttributeKey

// Создаем типизированный ключ для хранения объекта User в атрибутах запроса
val UserAttributeKey = AttributeKey<User>("UserAttributeKey")