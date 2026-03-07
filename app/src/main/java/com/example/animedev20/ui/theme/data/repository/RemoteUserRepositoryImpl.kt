package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.remote.AuthApiPlain
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.DeviceLoginRequest
import com.example.animedev20.ui.theme.data.remote.UpdateProfileRequest
import com.example.animedev20.ui.theme.data.remote.UpdateSettingsRequest
import com.example.animedev20.ui.theme.data.remote.UserMeDto
import com.example.animedev20.ui.theme.data.remote.UserSettingsDto
import com.example.animedev20.ui.theme.data.remote.UsersApi
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemoteUserRepositoryImpl(
    private val authApi: AuthApiPlain,
    private val usersApi: UsersApi,
    private val tokenStore: AuthTokenStore,
    private val context: Context
) : UserRepository {

    private val profileFlow = MutableStateFlow(FakeDataSource.defaultUserProfile)
    private var cachedSettings: UserSettings = FakeDataSource.defaultUserSettings
    private var deviceId: String = "unknown"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        deviceId = tokenStore.getOrCreateDeviceId(context)
        isInitialized = true
    }

    override suspend fun getPreferredGenres(): List<Genre> = getUserSettings().preferredGenres

    override suspend fun getUserProfile(): UserProfile {
        initialize(context)
        ensureAuthenticated()
        val profile = usersApi.me().toDomain(profileFlow.value)
        profileFlow.value = profile
        return profile
    }

    override fun observeUserProfile(): Flow<UserProfile> = profileFlow.asStateFlow()

    override suspend fun getUserSettings(): UserSettings {
        ensureAuthenticated()
        val settings = usersApi.getSettings().toDomain(cachedSettings)
        cachedSettings = settings
        profileFlow.value = profileFlow.value.copy(
            favoriteGenres = settings.preferredGenres,
            preferredDurations = settings.preferredDurations
        )
        return settings
    }

    override suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        ensureAuthenticated()
        val updated = usersApi.updateSettings(settings.toRequest()).toDomain(settings)
        cachedSettings = updated
        profileFlow.value = profileFlow.value.copy(
            favoriteGenres = updated.preferredGenres,
            preferredDurations = updated.preferredDurations
        )
        return updated
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        val current = getUserSettings()
        return updateUserSettings(current.copy(preferredGenres = genres)).preferredGenres
    }

    override suspend fun updateAccountInfo(name: String, email: String, nickname: String): UserProfile {
        ensureAuthenticated()
        val updated = usersApi.updateProfile(
            UpdateProfileRequest(displayName = name, email = email)
        ).toDomain(profileFlow.value.copy(nickname = nickname, email = email, name = name))

        val merged = updated.copy(
            nickname = nickname.ifBlank { updated.nickname }
        )
        profileFlow.value = merged
        return merged
    }

    private suspend fun ensureAuthenticated() {
        initialize(context)
        if (tokenStore.getToken().isNullOrBlank()) {
            val login = authApi.loginDevice(DeviceLoginRequest(deviceId))
            tokenStore.saveToken(login.accessToken)
            tokenStore.saveUserId(login.userId)
        }
    }

    private fun UserMeDto.toDomain(current: UserProfile): UserProfile {
        val display = displayName?.takeIf { it.isNotBlank() }
        return current.copy(
            id = id,
            name = display ?: current.name,
            nickname = display ?: current.nickname,
            email = email ?: current.email
        )
    }

    private fun UserSettingsDto.toDomain(current: UserSettings): UserSettings {
        return current.copy(
            preferredGenres = preferredGenres.map { it.toGenre() }.ifEmpty { current.preferredGenres },
            preferredDurations = preferredDurations.mapNotNull { it.toDurationTypeOrNull() }
                .ifEmpty { current.preferredDurations },
            notificationsEnabled = toggles["notificationsEnabled"] ?: current.notificationsEnabled,
            culturalAlertsEnabled = toggles["culturalAlertsEnabled"] ?: current.culturalAlertsEnabled,
            autoplayNextEpisode = toggles["autoplayNextEpisode"] ?: current.autoplayNextEpisode,
            hasCompletedOnboarding = toggles["hasCompletedOnboarding"] ?: current.hasCompletedOnboarding
        )
    }

    private fun UserSettings.toRequest(): UpdateSettingsRequest {
        return UpdateSettingsRequest(
            preferredGenres = preferredGenres.mapNotNull { it.id.toIntOrNull() },
            preferredDurations = preferredDurations.map { it.name },
            toggles = mapOf(
                "notificationsEnabled" to notificationsEnabled,
                "culturalAlertsEnabled" to culturalAlertsEnabled,
                "autoplayNextEpisode" to autoplayNextEpisode,
                "hasCompletedOnboarding" to hasCompletedOnboarding
            )
        )
    }

    private fun Int.toGenre(): Genre {
        return Genre(id = toString(), name = "Genre $this")
    }

    private fun String.toDurationTypeOrNull(): DurationType? {
        return DurationType.values().firstOrNull { it.name.equals(this, ignoreCase = true) }
    }
}
