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
    private const val KEY_GENRE_IDS = "settings_genre_ids"
    private const val KEY_DURATIONS = "settings_durations"
    private const val KEY_NOTIFICATIONS = "settings_notifications"
    private const val KEY_CULTURAL_ALERTS = "settings_cultural_alerts"
    private const val KEY_AUTOPLAY = "settings_autoplay"
    private const val KEY_ONBOARDING_COMPLETED = "settings_onboarding_completed"

    private var cachedSettings: UserSettings = FakeDataSource.defaultUserSettings
    private val profileFlow = MutableStateFlow(FakeDataSource.defaultUserProfile)
    private var appContext: Context? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val default = FakeDataSource.defaultUserSettings
        val genreIds = prefs.getStringSet(KEY_GENRE_IDS, null)
        val durations = prefs.getStringSet(KEY_DURATIONS, null)
        val restoredSettings = default.copy(
            preferredGenres = genreIds
                ?.mapNotNull { id -> FakeDataSource.genres.firstOrNull { it.id == id } }
                ?: default.preferredGenres,
            preferredDurations = durations
                ?.mapNotNull { name -> DurationType.values().firstOrNull { it.name == name } }
                ?.ifEmpty { null }
                ?: default.preferredDurations,
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, default.notificationsEnabled),
            culturalAlertsEnabled = prefs.getBoolean(KEY_CULTURAL_ALERTS, default.culturalAlertsEnabled),
            autoplayNextEpisode = prefs.getBoolean(KEY_AUTOPLAY, default.autoplayNextEpisode),
            hasCompletedOnboarding = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, default.hasCompletedOnboarding)
        )
        cachedSettings = restoredSettings
        profileFlow.value = profileFlow.value.copy(
            favoriteGenres = restoredSettings.preferredGenres,
            preferredDurations = restoredSettings.preferredDurations
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
        persistSettings()
        return cachedSettings
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        delay(400)
        cachedSettings = cachedSettings.copy(preferredGenres = genres)
        profileFlow.value = profileFlow.value.copy(favoriteGenres = genres)
        persistSettings()
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
        return updatedProfile
    }

    private fun persistSettings() {
        val context = appContext ?: return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_GENRE_IDS, cachedSettings.preferredGenres.map { it.id }.toSet())
            putStringSet(KEY_DURATIONS, cachedSettings.preferredDurations.map { it.name }.toSet())
            putBoolean(KEY_NOTIFICATIONS, cachedSettings.notificationsEnabled)
            putBoolean(KEY_CULTURAL_ALERTS, cachedSettings.culturalAlertsEnabled)
            putBoolean(KEY_AUTOPLAY, cachedSettings.autoplayNextEpisode)
            putBoolean(KEY_ONBOARDING_COMPLETED, cachedSettings.hasCompletedOnboarding)
        }
    }
}
