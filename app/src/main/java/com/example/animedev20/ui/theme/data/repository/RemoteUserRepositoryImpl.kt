package com.example.animedev20.ui.theme.data.repository

import android.util.Log
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.data.remote.auth.AuthApi
import com.example.animedev20.ui.theme.data.remote.auth.DeviceLoginRequest
import com.example.animedev20.ui.theme.data.remote.session.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.users.UpdateProfileRequest
import com.example.animedev20.ui.theme.data.remote.users.UpdateSettingsRequest
import com.example.animedev20.ui.theme.data.remote.users.UserDto
import com.example.animedev20.ui.theme.data.remote.users.UserSettingsDto
import com.example.animedev20.ui.theme.data.remote.users.UsersApi
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withTimeoutOrNull

class RemoteUserRepositoryImpl(
    private val authApi: AuthApi,
    private val usersApi: UsersApi,
    private val animeApi: AnimeApi,
    private val tokenStore: AuthTokenStore
) : UserRepository {

    private val profileFlow = MutableStateFlow(FakeDataSource.defaultUserProfile)
    private val settingsFlow = MutableStateFlow(FakeDataSource.defaultUserSettings)

    override suspend fun getPreferredGenres(): List<Genre> {
        val settings = getUserSettings()
        return settings.preferredGenres
    }

    override suspend fun getUserProfile(): UserProfile {
        withTimeoutOrNull(NETWORK_TIMEOUT_MS) { refreshProfile() }
        return profileFlow.value
    }

    override fun observeUserProfile(): Flow<UserProfile> {
        return profileFlow.asStateFlow().onStart {
            refreshProfile()
        }
    }

    override suspend fun getUserSettings(): UserSettings {
        withTimeoutOrNull(NETWORK_TIMEOUT_MS) { refreshSettings() }
        return settingsFlow.value
    }

    override suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        return runCatching {
            ensureSession()
            val request = UpdateSettingsRequest(
                preferredGenres = settings.preferredGenres.mapNotNull { it.id.toIntOrNull() },
                preferredDurations = settings.preferredDurations.map { it.name },
                toggles = settings.toToggleMap()
            )
            val updated = usersApi.updateMySettings(request)
            val domain = updated.toDomainSettings(animeApi)
            settingsFlow.value = domain
            profileFlow.value = profileFlow.value.copy(
                favoriteGenres = domain.preferredGenres,
                preferredDurations = domain.preferredDurations
            )
            domain
        }.getOrElse { error ->
            Log.e(TAG, "Error actualizando settings", error)
            throw Exception("No fue posible actualizar la configuración del usuario.")
        }
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        val current = getUserSettings()
        val updated = updateUserSettings(current.copy(preferredGenres = genres))
        return updated.preferredGenres
    }

    override suspend fun updateAccountInfo(name: String, email: String, nickname: String): UserProfile {
        return runCatching {
            ensureSession()
            val updatedUser = usersApi.updateProfile(
                UpdateProfileRequest(
                    displayName = name,
                    email = email
                )
            )
            val domain = updatedUser.toDomainProfile(
                previous = profileFlow.value,
                nickname = nickname
            )
            profileFlow.value = domain
            domain
        }.getOrElse { error ->
            Log.e(TAG, "Error actualizando perfil", error)
            throw Exception("No fue posible actualizar la información de la cuenta.")
        }
    }

    private suspend fun refreshProfile() {
        runCatching {
            ensureSession()
            val profileDto = usersApi.getMe()
            profileFlow.value = profileDto.toDomainProfile(
                previous = profileFlow.value,
                nickname = profileFlow.value.nickname
            )
        }.onFailure { error ->
            Log.e(TAG, "Error refrescando perfil", error)
        }
    }

    private suspend fun refreshSettings() {
        runCatching {
            ensureSession()
            val settingsDto = usersApi.getMySettings()
            val domain = settingsDto.toDomainSettings(animeApi)
            settingsFlow.value = domain
            profileFlow.value = profileFlow.value.copy(
                favoriteGenres = domain.preferredGenres,
                preferredDurations = domain.preferredDurations
            )
        }.onFailure { error ->
            Log.e(TAG, "Error refrescando settings", error)
        }
    }

    private suspend fun ensureSession() {
        if (!tokenStore.getToken().isNullOrBlank()) return

        val deviceId = tokenStore.getOrCreateDeviceId()
        val session = authApi.deviceLogin(DeviceLoginRequest(deviceId = deviceId))
        tokenStore.saveSession(userId = session.userId, accessToken = session.accessToken)
    }

    private fun UserDto.toDomainProfile(previous: UserProfile, nickname: String): UserProfile {
        return previous.copy(
            id = id,
            name = displayName ?: previous.name,
            nickname = nickname,
            email = email ?: previous.email
        )
    }

    private suspend fun UserSettingsDto.toDomainSettings(animeApi: AnimeApi): UserSettings {
        val availableGenres = runCatching { animeApi.getGenres().data }
            .onFailure { Log.e(TAG, "Error cargando catálogo de géneros", it) }
            .getOrDefault(emptyList())

        val selectedGenreIds = parseGenreIds(preferredGenres)
        val mappedGenres = availableGenres.filter { genre ->
            genre.id.toIntOrNull() in selectedGenreIds
        }

        val mappedDurations = parseDurations(preferredDurations)

        return settingsFlow.value.copy(
            preferredGenres = mappedGenres,
            preferredDurations = mappedDurations,
            notificationsEnabled = toggles?.get("notificationsEnabled")
                ?: settingsFlow.value.notificationsEnabled,
            culturalAlertsEnabled = toggles?.get("showMatureContent")
                ?: settingsFlow.value.culturalAlertsEnabled,
            autoplayNextEpisode = toggles?.get("showRecommendations")
                ?: settingsFlow.value.autoplayNextEpisode,
            hasCompletedOnboarding = toggles?.get("hasCompletedOnboarding")
                ?: settingsFlow.value.hasCompletedOnboarding
        )
    }


    private fun parseGenreIds(raw: Any?): Set<Int> {
        return when (raw) {
            is Number -> setOf(raw.toInt())
            is String -> raw.toIntOrNull()?.let(::setOf).orEmpty()
            is List<*> -> raw.mapNotNull {
                when (it) {
                    is Number -> it.toInt()
                    is String -> it.toIntOrNull()
                    else -> null
                }
            }.toSet()
            else -> emptySet()
        }
    }

    private fun parseDurations(raw: Any?): List<DurationType> {
        val values = when (raw) {
            is String -> listOf(raw)
            is List<*> -> raw.mapNotNull { it?.toString() }
            else -> emptyList()
        }

        return values.mapNotNull { durationName ->
            DurationType.values().firstOrNull { it.name == durationName }
        }
    }

    private fun UserSettings.toToggleMap(): Map<String, Boolean> {
        return mapOf(
            "notificationsEnabled" to notificationsEnabled,
            "showMatureContent" to culturalAlertsEnabled,
            "showRecommendations" to autoplayNextEpisode,
            "hasCompletedOnboarding" to hasCompletedOnboarding
        )
    }

    companion object {
        private const val TAG = "RemoteUserRepository"
        private const val NETWORK_TIMEOUT_MS = 2_500L
    }
}
