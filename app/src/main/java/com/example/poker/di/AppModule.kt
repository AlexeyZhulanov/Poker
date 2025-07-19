package com.example.poker.di

import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.AppJson
import com.example.poker.data.remote.dto.AuthResponse
import com.example.poker.data.storage.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHttpClient(appSettings: AppSettings, authEventBus: AuthEventBus): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) { json(AppJson) }
            install(WebSockets) { contentConverter =
                KotlinxWebsocketSerializationConverter(AppJson)
            }

            // --- ЛОГИКА АВТОМАТИЧЕСКОГО ОБНОВЛЕНИЯ ТОКЕНОВ ---
            install(Auth) {
                bearer {
                    // Блок, который говорит Ktor, где брать токены для каждого запроса
                    loadTokens {
                        val accessToken = appSettings.getAccessToken()
                        val refreshToken = appSettings.getRefreshToken()
                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(accessToken, refreshToken)
                        } else {
                            null
                        }
                    }

                    // Блок, который Ktor вызовет сам, если получит ошибку 401 Unauthorized
                    refreshTokens {
                        println("Ktor Auth: Refreshing tokens...")
                        val refreshToken = appSettings.getRefreshToken() ?: return@refreshTokens null

                        // Делаем запрос на наш эндпоинт /auth/refresh
                        val response = client.post("http://amessenger.ru/auth/refresh") {
                            header("Authorization", "Bearer $refreshToken")
                        }

                        if (response.status == HttpStatusCode.OK) {
                            val newTokens = response.body<AuthResponse>()
                            appSettings.saveAccessToken(newTokens.accessToken)
                            appSettings.saveRefreshToken(newTokens.refreshToken)
                            BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                        } else {
                            // Очищаем старые, невалидные токены
                            appSettings.saveAccessToken(null)
                            appSettings.saveRefreshToken(null)
                            authEventBus.postEvent(AuthEvent.SessionExpired)
                            null
                        }
                    }
                }
            }
        }
    }

    // Предоставляем KtorApiClient, который теперь использует настроенный HttpClient
    @Provides
    @Singleton
    fun provideKtorApiClient(httpClient: HttpClient): KtorApiClient {
        return KtorApiClient(httpClient)
    }
}