package com.example.poker.data.repository

import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.CreateRoomRequest
import com.example.poker.data.remote.dto.GameRoom
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val apiClient: KtorApiClient
) {
    suspend fun createRoom(request: CreateRoomRequest): Result<GameRoom> {
        return try {
            val response = apiClient.client.post("http://amessenger.ru:8080/rooms") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created) {
                Result.Success(response.body())
            } else {
                Result.Error(response.toString())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun joinRoom(roomId: String): Result<GameRoom> {
        return try {
            val response = apiClient.client.post("http://amessenger.ru:8080/rooms/$roomId/join")
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body())
            } else {
                Result.Error("Failed to join room: ${response.status}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun startGame(roomId: String): Result<Unit> {
        return try {
            val response = apiClient.client.post("http://amessenger.ru:8080/rooms/$roomId/start")
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body())
            } else {
                Result.Error("Failed to start game: ${response.status}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}