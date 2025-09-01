@file:Suppress("DEPRECATION") // all androidx.security.crypto

package com.example.poker.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

private const val PREF_ACCESS_TOKEN = "access_token"
private const val PREF_REFRESH_TOKEN = "refresh_token"
private const val PREFERENCES_FILE_NAME = "poker_app_encrypted_prefs"
private const val PREFERENCES_SCALE_MULTIPLIER = "scale_multiplier"
private const val PREFERENCES_PERFORMANCE_MODE = "performance_mode"

@Singleton
class EncryptedAppSettings @Inject constructor(
    @ApplicationContext appContext: Context
) : AppSettings {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        PREFERENCES_FILE_NAME,
        masterKeyAlias,
        appContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveAccessToken(token: String?) =
        sharedPreferences.edit { putString(PREF_ACCESS_TOKEN, token) }

    override fun getAccessToken(): String? =
        sharedPreferences.getString(PREF_ACCESS_TOKEN, null)

    override fun saveRefreshToken(token: String?) =
        sharedPreferences.edit { putString(PREF_REFRESH_TOKEN, token) }

    override fun getRefreshToken(): String? =
        sharedPreferences.getString(PREF_REFRESH_TOKEN, null)

    override fun getScaleMultiplier(): Float =
        sharedPreferences.getFloat(PREFERENCES_SCALE_MULTIPLIER, 1.0f)

    override fun saveScaleMultiplier(value: Float) =
        sharedPreferences.edit { putFloat(PREFERENCES_SCALE_MULTIPLIER, value) }

    override fun getPerformanceMode(): Boolean =
        sharedPreferences.getBoolean(PREFERENCES_PERFORMANCE_MODE, false)

    override fun savePerformanceMode(value: Boolean) =
        sharedPreferences.edit { putBoolean(PREFERENCES_PERFORMANCE_MODE, value) }
}