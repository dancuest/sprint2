package com.example.animedev20.ui.theme.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val USER_PREFS_NAME = "user_prefs"

val Context.userPrefsDataStore by preferencesDataStore(name = USER_PREFS_NAME)

object UserPrefsKeys {
    val DISPLAY_NAME = stringPreferencesKey("display_name")
    val BIO = stringPreferencesKey("bio")
    val PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    val LEVEL = stringPreferencesKey("level")
    val ACCOUNT_NAME = stringPreferencesKey("account_name")
    val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
    val ACCOUNT_NICKNAME = stringPreferencesKey("account_nickname")
    val COMPLETED_TRIVIAS = intPreferencesKey("completed_trivias")
    val PREFERRED_GENRES = stringPreferencesKey("preferred_genres")

    val NOTIFICATIONS = booleanPreferencesKey("notifications")
    val CULTURAL_ALERTS = booleanPreferencesKey("cultural_alerts")
    val AUTOPLAY = booleanPreferencesKey("autoplay")
    val TEXT_SIZE = stringPreferencesKey("text_size")
}
