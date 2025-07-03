package com.example.poker.data.remote

import com.example.poker.data.remote.dto.AppJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // todo в hilt-модуль переместить
class KtorApiClient @Inject constructor() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets)
    }
}