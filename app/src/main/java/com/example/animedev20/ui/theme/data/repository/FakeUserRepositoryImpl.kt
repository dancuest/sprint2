package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FakeUserRepositoryImpl : UserRepository {

    private const val PREFS_NAME = "animedev_user_prefs"
    private const val KEY_GENRES = "settings_genres"
    private const val KEY_DURATIONS = "settings_durations"
    private const val KEY_NOTIFICATIONS = "settings_notifications"
    private const val KEY_CULTURAL_ALERTS = "settings_cultural_alerts"
    private const val KEY_AUTOPLAY = "settings_autoplay"
    private const val KEY_ONBOARDING_COMPLETED = "settings_onboarding_completed"
    private const val KEY_PROFILE_NAME = "profile_name"
    private const val KEY_PROFILE_EMAIL = "profile_email"
    private const val KEY_PROFILE_NICKNAME = "profile_nickname"

    private var cachedSettings: UserSettings = FakeDataSource.defaultUserSettings
    private val profileFlow = MutableStateFlow(FakeDataSource.defaultUserProfile)
    private var appContext: Context? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultSettings = FakeDataSource.defaultUserSettings
        val defaultProfile = FakeDataSource.defaultUserProfile

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
            hasCompletedOnboarding = prefs.getBoolean(
                KEY_ONBOARDING_COMPLETED,
                defaultSettings.hasCompletedOnboarding
            )
        )

        profileFlow.value = defaultProfile.copy(
            name = prefs.getString(KEY_PROFILE_NAME, defaultProfile.name) ?: defaultProfile.name,
            email = prefs.getString(KEY_PROFILE_EMAIL, defaultProfile.email) ?: defaultProfile.email,
            nickname = prefs.getString(KEY_PROFILE_NICKNAME, defaultProfile.nickname)
                ?: defaultProfile.nickname,
            favoriteGenres = cachedSettings.preferredGenres,
            preferredDurations = cachedSettings.preferredDurations
        )
        isInitialized = true
    }

    override suspend fun getPreferredGenres(): List<Genre> {
        delay(500)
        return cachedSettings.preferredGenres
    }

    override suspend fun getUserProfile(): UserProfile {
        delay(400)
        return profileFlow.value
    }

    override fun observeUserProfile(): Flow<UserProfile> = profileFlow.asStateFlow()

    override suspend fun getUserSettings(): UserSettings {
        delay(400)
        return cachedSettings
    }

    override suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        delay(400)
        cachedSettings = settings
        profileFlow.value = profileFlow.value.copy(
            favoriteGenres = settings.preferredGenres,
            preferredDurations = settings.preferredDurations
        )
        persistState()
        return cachedSettings
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        delay(400)
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
        delay(400)
        val updatedProfile = profileFlow.value.copy(
            name = name,
            email = email,
            nickname = nickname
        )
        profileFlow.value = updatedProfile
        persistState()
        return updatedProfile
    }

    private fun persistState() {
        val context = appContext ?: return
        val profile = profileFlow.value
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_GENRES, cachedSettings.preferredGenres.map { encodeGenre(it) }.toSet())
            putStringSet(KEY_DURATIONS, cachedSettings.preferredDurations.map { it.name }.toSet())
            putBoolean(KEY_NOTIFICATIONS, cachedSettings.notificationsEnabled)
            putBoolean(KEY_CULTURAL_ALERTS, cachedSettings.culturalAlertsEnabled)
            putBoolean(KEY_AUTOPLAY, cachedSettings.autoplayNextEpisode)
            putBoolean(KEY_ONBOARDING_COMPLETED, cachedSettings.hasCompletedOnboarding)
            putString(KEY_PROFILE_NAME, profile.name)
            putString(KEY_PROFILE_EMAIL, profile.email)
            putString(KEY_PROFILE_NICKNAME, profile.nickname)
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
