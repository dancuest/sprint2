package com.example.animedev20.ui.theme.data.remote.session

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.util.UUID

class AuthTokenStore(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun saveSession(userId: String, accessToken: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }

    fun getOrCreateDeviceId(): String {
        prefs.getString(KEY_DEVICE_ID, null)?.let { return it }

        val secureId = runCatching {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        }
            .onFailure { Log.e(TAG, "No se pudo leer ANDROID_ID", it) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() && it != "9774d56d682e549c" }

        val newDeviceId = secureId ?: UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, newDeviceId).apply()
        return newDeviceId
    }

    companion object {
        private const val TAG = "AuthTokenStore"
        private const val PREFS_NAME = "auth_session_prefs"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
    }
}
