package com.example.poker.server

import com.example.poker.server.data.DatabaseFactory
import com.example.poker.server.plugins.configureMonitoring
import com.example.poker.server.plugins.configureRouting
import com.example.poker.server.plugins.configureSecurity
import com.example.poker.server.plugins.configureSerialization
import com.example.poker.server.plugins.configureSockets
import com.example.poker.shared.model.GameRoomService
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.doublereceive.DoubleReceive

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    val gameRoomService = GameRoomService()
    DatabaseFactory.init(environment.config)
    install(DoubleReceive)
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureSockets(gameRoomService)
    configureRouting(gameRoomService)
}
