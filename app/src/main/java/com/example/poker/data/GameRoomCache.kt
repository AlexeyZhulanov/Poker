package com.example.poker.data

import com.example.poker.shared.dto.GameRoom

// Простой синглтон для временного хранения объекта GameRoom между экранами
object GameRoomCache {
    var currentRoom: GameRoom? = null
}