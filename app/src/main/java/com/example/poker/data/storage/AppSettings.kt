package com.example.poker.data.storage

interface AppSettings {
    fun saveAccessToken(token: String?)
    fun getAccessToken(): String?
    fun saveRefreshToken(token: String?)
    fun getRefreshToken(): String?
    fun getScaleMultiplier(): Float
    fun saveScaleMultiplier(value: Float)
    fun getPerformanceMode(): Boolean
    fun savePerformanceMode(value: Boolean)
}