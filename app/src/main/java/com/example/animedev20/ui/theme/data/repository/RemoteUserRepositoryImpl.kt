package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.remote.AuthApi
import com.example.animedev20.ui.theme.data.remote.DeviceLoginRequest
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class RemoteUserRepositoryImpl(
    private val authApi: AuthApi
) : UserRepository {

    private val PREFS_NAME = "animedev_user_prefs_remote"
    private val KEY_GENRES = "settings_genres"
    private val KEY_DURATIONS = "settings_durations"
    private val KEY_NOTIFICATIONS = "settings_notifications"
    private val KEY_CULTURAL_ALERTS = "settings_cultural_alerts"
    private val KEY_AUTOPLAY = "settings_autoplay"
    private val KEY_ONBOARDING_COMPLETED = "settings_onboarding_completed"

    private var cachedSettings: UserSettings = FakeDataSource.defaultUserSettings
    private val profileFlow = MutableStateFlow(FakeDataSource.defaultUserProfile)
    private var appContext: Context? = null
    private var isInitialized = false
    private var deviceId: String = "unknown"

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        
        deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown_device"
        Log.d("RemoteUserRepository", "Initializing with device ID: $deviceId")
        
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultSettings = FakeDataSource.defaultUserSettings

        val restoredGenres = prefs.getStringSet(KEY_GENRES, null)
            ?.mapNotNull(::decodeGenre)
            ?.ifEmpty { null }
            ?: defaultSettings.preferredGenres

        val restoredDurations = prefs.getStringSet(KEY_DURATIONS, null)
            ?.mapNotNull { name -> DurationType.values().firstOrNull { it.name == name } }
            ?.ifEmpty { null }
            ?: defaultSettings.preferredDurations

        cachedSettings = defaultSettings.copy(
            preferredGenres = restoredGenres,
            preferredDurations = restoredDurations,
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, defaultSettings.notificationsEnabled),
            culturalAlertsEnabled = prefs.getBoolean(KEY_CULTURAL_ALERTS, defaultSettings.culturalAlertsEnabled),
            autoplayNextEpisode = prefs.getBoolean(KEY_AUTOPLAY, defaultSettings.autoplayNextEpisode),
            hasCompletedOnboarding = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, defaultSettings.hasCompletedOnboarding)
        )
        isInitialized = true
    }

    override suspend fun getPreferredGenres(): List<Genre> {
        return cachedSettings.preferredGenres
    }

    override suspend fun getUserProfile(): UserProfile {
        try {
            val response = authApi.loginDevice(DeviceLoginRequest(deviceId))
            val profile = response.data.user
            profileFlow.value = profile
            return profile
        } catch (e: Exception) {
            Log.e("RemoteUserRepo", "Error en loginDevice API", e)
        }
        return profileFlow.value
    }

    override fun observeUserProfile(): Flow<UserProfile> = profileFlow.asStateFlow()

    override suspend fun getUserSettings(): UserSettings {
        return cachedSettings
    }

    override suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        cachedSettings = settings
        profileFlow.value = profileFlow.value.copy(
            favoriteGenres = settings.preferredGenres,
            preferredDurations = settings.preferredDurations
        )
        persistState()
        return cachedSettings
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        cachedSettings = cachedSettings.copy(preferredGenres = genres)
        profileFlow.value = profileFlow.value.copy(favoriteGenres = genres)
        persistState()
        return cachedSettings.preferredGenres
    }

    override suspend fun updateAccountInfo(
        name: String,
        email: String,
        nickname: String
    ): UserProfile {
        val updatedProfile = profileFlow.value.copy(
            name = name,
            email = email,
            nickname = nickname
        )
        profileFlow.value = updatedProfile
        return updatedProfile
    }

    private fun persistState() {
        val context = appContext ?: return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_GENRES, cachedSettings.preferredGenres.map { encodeGenre(it) }.toSet())
            putStringSet(KEY_DURATIONS, cachedSettings.preferredDurations.map { it.name }.toSet())
            putBoolean(KEY_NOTIFICATIONS, cachedSettings.notificationsEnabled)
            putBoolean(KEY_CULTURAL_ALERTS, cachedSettings.culturalAlertsEnabled)
            putBoolean(KEY_AUTOPLAY, cachedSettings.autoplayNextEpisode)
            putBoolean(KEY_ONBOARDING_COMPLETED, cachedSettings.hasCompletedOnboarding)
        }
    }

    private fun encodeGenre(genre: Genre): String = "${genre.id}|${genre.name}"

    private fun decodeGenre(value: String): Genre? {
        val separator = value.indexOf('|')
        if (separator <= 0 || separator == value.lastIndex) return null
        val id = value.substring(0, separator)
        val name = value.substring(separator + 1)
        return Genre(id = id, name = name)
    }
}
