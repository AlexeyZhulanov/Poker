package com.example.poker.data.repository

import com.example.poker.data.remote.KtorApiClient
import com.example.poker.shared.dto.CreateRoomRequest
import com.example.poker.shared.dto.GameRoom
import com.example.poker.shared.dto.GameState
import com.example.poker.shared.dto.LeaveRequest
import com.example.poker.util.serverUrl
import io.ktor.client.call.body
import io.ktor.client.request.get
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
            val response = apiClient.client.post("$serverUrl/rooms") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created) {
                Result.Success(response.body())
            } else {
                Result.Error(response.body())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun joinRoom(roomId: String): Result<GameRoom> {
        return try {
            val response = apiClient.client.post("$serverUrl/rooms/$roomId/join")
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body())
            } else {
                Result.Error("Failed to join room: ${response.status}")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getRoomDetails(roomId: String): Result<GameRoom> {
        return try {
            val response = apiClient.client.get("$serverUrl/rooms/$roomId")
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body())
            } else {
                Result.Error("Room not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getGameState(roomId: String): Result<GameState> {
        return try {
            val response = apiClient.client.get("$serverUrl/rooms/$roomId/state")
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body())
            } else {
                Result.Error("Game state not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun leaveOfflineGame(hostUrl: String, userId: String): Result<Unit> {
        // Превращаем WebSocket URL (ws://...) в HTTP URL (http://...)
        val httpUrl = hostUrl
            .replace("ws://", "http://")
            .substringBefore("/play") // Убираем путь к сокету

        return try {
            apiClient.client.post("$httpUrl/leave") {
                contentType(ContentType.Application.Json)
                setBody(LeaveRequest(userId))
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}