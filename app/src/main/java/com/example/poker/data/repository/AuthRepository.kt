package com.example.poker.data.repository

import com.example.poker.data.remote.KtorApiClient
import com.example.poker.shared.dto.AuthResponse
import com.example.poker.shared.dto.LoginRequest
import com.example.poker.shared.dto.RegisterRequest
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiClient: KtorApiClient
) {
    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiClient.client.post("http://amessenger.ru:8080/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                Result.Success(response.body())
            } else {
                Result.Error("Invalid username or password")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "An unknown error occurred")
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiClient.client.post("http://amessenger.ru:8080/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.Created) {
                Result.Success(response.body())
            } else {
                Result.Error("User with this email or username already exists")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "An unknown error occurred")
        }
    }
}