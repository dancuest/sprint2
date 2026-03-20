package com.example.animedev20.ui.theme.data.remote

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit
import java.util.UUID

class AuthTokenStore(private val context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun getOrCreateDeviceId(context: Context): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.takeIf { it.isNotBlank() }

        val deviceId = androidId ?: UUID.randomUUID().toString()
        prefs.edit { putString(KEY_DEVICE_ID, deviceId) }
        return deviceId
    }

    fun createFreshGuestDeviceId(): String {
        val newDeviceId = "guest-${UUID.randomUUID()}"
        prefs.edit {
            putString(KEY_DEVICE_ID, newDeviceId)
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
        }
        return newDeviceId
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getDeviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun saveUserId(userId: String) {
        prefs.edit { putString(KEY_USER_ID, userId) }
    }

    fun saveDeviceId(deviceId: String) {
        prefs.edit { putString(KEY_DEVICE_ID, deviceId) }
    }

    fun clearSession() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
        }
    }

    fun clearAll() {
        prefs.edit {
            remove(KEY_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_DEVICE_ID)
        }
    }

    companion object {
        private const val PREFS_NAME = "auth_token_store"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
    }
}