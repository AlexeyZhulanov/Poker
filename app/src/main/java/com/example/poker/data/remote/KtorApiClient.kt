package com.example.poker.data.remote

import io.ktor.client.HttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KtorApiClient @Inject constructor(val client: HttpClient)