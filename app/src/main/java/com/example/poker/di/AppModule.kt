package com.example.poker.di

import android.util.Log
import com.example.poker.data.remote.KtorApiClient
import com.example.poker.data.remote.dto.AppJson
import com.example.poker.shared.dto.AuthResponse
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
                        val refreshToken = appSettings.getRefreshToken()
                        if(refreshToken == null) {
                            Log.d("testRefreshToken", "Token is null")
                            authEventBus.postEvent(AuthEvent.SessionExpired)
                            return@refreshTokens null
                        }
                        // без отдельного клиента не будет работать (из-за плагина Auth)
                        val refreshClient = HttpClient(CIO) {
                            install(ContentNegotiation) { json(AppJson) }
                        }

                        val response = try {
                            // Делаем запрос на наш эндпоинт /auth/refresh
                            refreshClient.post("http://amessenger.ru:8080/auth/refresh") {
                                header("Authorization", "Bearer $refreshToken")
                            }
                        } catch (e: Exception) {
                            Log.d("testRefresh", "Except: ${e.message}")
                            null
                        } finally {
                            refreshClient.close()
                        }
                        if (response?.status == HttpStatusCode.OK) {
                            Log.d("testRefreshToken", "Done.")
                            val newTokens = response.body<AuthResponse>()
                            appSettings.saveAccessToken(newTokens.accessToken)
                            appSettings.saveRefreshToken(newTokens.refreshToken)
                            BearerTokens(newTokens.accessToken, newTokens.refreshToken)
                        } else {
                            Log.d("testRefreshToken", "Http not completed: $response")
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