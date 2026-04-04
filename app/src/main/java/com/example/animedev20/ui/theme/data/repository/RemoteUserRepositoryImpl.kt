package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.data.remote.AuthApiPlain
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.DeviceLoginRequest
import com.example.animedev20.ui.theme.data.remote.GenreDto
import com.example.animedev20.ui.theme.data.remote.UpdateProfileRequest
import com.example.animedev20.ui.theme.data.remote.UpdateSettingsRequest
import com.example.animedev20.ui.theme.data.remote.UserMeDto
import com.example.animedev20.ui.theme.data.remote.UserSettingsDto
import com.example.animedev20.ui.theme.data.remote.UsersApi
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserDemographicCatalog
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemoteUserRepositoryImpl(
    private val authApi: AuthApiPlain,
    private val usersApi: UsersApi,
    private val animeApi: AnimeApi,
    private val tokenStore: AuthTokenStore,
    private val context: Context,
    private val homeRefreshBus: HomeRefreshBus
) : UserRepository {

    private val profileFlow = MutableStateFlow(emptyUserProfile())
    private var cachedSettings: UserSettings = emptyUserSettings()
    private var cachedGenresById: Map<Int, GenreDto> = emptyMap()
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

        val dto = usersApi.me()
        // toDomain ahora usa el nickname real del servidor
        val profile = dto.toDomain(profileFlow.value)
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

        homeRefreshBus.trigger()
        return updated
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        val current = getUserSettings()
        return updateUserSettings(current.copy(preferredGenres = genres)).preferredGenres
    }

    override suspend fun updateAccountInfo(
        name: String,
        email: String,
        nickname: String
    ): UserProfile {
        ensureAuthenticated()

        val normalizedEmail = email.takeIf { it.isNotBlank() }
        val normalizedNickname = nickname.takeIf { it.isNotBlank() }
        val normalizedName = name.takeIf { it.isNotBlank() }

        // Ahora enviamos nickname al servidor correctamente
        val updatedDto = usersApi.updateProfile(
            UpdateProfileRequest(
                displayName = normalizedName,
                nickname = normalizedNickname,
                email = normalizedEmail
            )
        )

        // Construimos el perfil a partir de la respuesta real del servidor
        val merged = updatedDto.toDomain(profileFlow.value)

        // Si el servidor devuelve nickname vacío (usuario invitado), conservamos el local
        val finalProfile = merged.copy(
            nickname = updatedDto.nickname?.takeIf { it.isNotBlank() }
                ?: normalizedNickname
                ?: profileFlow.value.nickname
        )

        profileFlow.value = finalProfile
        return finalProfile
    }

    override suspend fun updateProfileImages(
        avatarUrl: String?,
        coverImageUrl: String?
    ): UserProfile {
        ensureAuthenticated()

        val updatedDto = usersApi.updateProfile(
            UpdateProfileRequest(
                avatarUrl = avatarUrl,
                coverImageUrl = coverImageUrl
            )
        )

        val merged = updatedDto.toDomain(profileFlow.value).copy(
            avatarUrl = avatarUrl ?: updatedDto.avatarUrl ?: profileFlow.value.avatarUrl,
            coverImageUrl = coverImageUrl ?: updatedDto.coverImageUrl ?: profileFlow.value.coverImageUrl,
            // Conservamos nickname y name del estado actual
            nickname = updatedDto.nickname?.takeIf { it.isNotBlank() } ?: profileFlow.value.nickname,
            name = profileFlow.value.name
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

    // ← FIX PRINCIPAL: ahora lee nickname del DTO en vez de ignorarlo
    private fun UserMeDto.toDomain(current: UserProfile): UserProfile {
        val display = displayName?.takeIf { it.isNotBlank() }
        val serverNickname = nickname?.takeIf { it.isNotBlank() }

        return current.copy(
            id = id,
            name = display ?: current.name,
            nickname = serverNickname ?: current.nickname, // ← usa el nickname del servidor
            email = email ?: current.email,
            avatarUrl = avatarUrl ?: current.avatarUrl,
            coverImageUrl = coverImageUrl ?: current.coverImageUrl,
            completedTrivias = completedTrivias ?: current.completedTrivias,
            totalAnimesWatched = favoriteCount ?: current.totalAnimesWatched
        )
    }

    private suspend fun UserSettingsDto.toDomain(current: UserSettings): UserSettings {
        val resolvedGenres = resolvePreferredGenres(this)

        return current.copy(
            ageRange = ageRange ?: current.ageRange,
            genderCode = genderCode ?: current.genderCode,
            regionCode = regionCode ?: current.regionCode,
            preferredGenres = resolvedGenres,
            preferredDurations = preferredDurations.mapNotNull { it.toDurationTypeOrNull() },
            notificationsEnabled = toggles["notificationsEnabled"] as? Boolean
                ?: current.notificationsEnabled,
            culturalAlertsEnabled = toggles["culturalAlertsEnabled"] as? Boolean
                ?: current.culturalAlertsEnabled,
            autoplayNextEpisode = toggles["autoplayNextEpisode"] as? Boolean
                ?: current.autoplayNextEpisode,
            hasCompletedOnboarding = toggles["hasCompletedOnboarding"] as? Boolean
                ?: current.hasCompletedOnboarding
        )
    }

    private suspend fun resolvePreferredGenres(settings: UserSettingsDto): List<Genre> {
        settings.preferredGenreDetails
            ?.takeIf { it.isNotEmpty() }
            ?.let { details ->
                return details.map { it.toDomainModel() }
            }

        val preferredIds = settings.preferredGenres
        if (preferredIds.isEmpty()) return emptyList()

        ensureGenresCache()
        return preferredIds.mapNotNull { genreId -> cachedGenresById[genreId]?.toDomainModel() }
    }

    private suspend fun ensureGenresCache() {
        if (cachedGenresById.isNotEmpty()) return

        val genres = animeApi.getGenres().data
        cachedGenresById = genres.mapNotNull { genre ->
            genre.id.toIntOrNull()?.let { id -> id to GenreDto(id = id, name = genre.name) }
        }.toMap()
    }

    private fun GenreDto.toDomainModel(): Genre {
        return Genre(
            id = id.toString(),
            name = name
        )
    }

    private fun UserSettings.toRequest(): UpdateSettingsRequest {
        return UpdateSettingsRequest(
            ageRange = ageRange,
            genderCode = genderCode,
            regionCode = regionCode,
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

    private fun String.toDurationTypeOrNull(): DurationType? {
        return DurationType.entries.firstOrNull { it.name.equals(this, ignoreCase = true) }
    }

    private fun emptyUserProfile(): UserProfile = UserProfile(
        id = "",
        name = "",
        nickname = "",
        email = "",
        avatarUrl = "",
        knowledgeLevel = "",
        xpPoints = 0,
        biography = "",
        totalAnimesWatched = 0,
        completedTrivias = 0,
        preferredDurations = emptyList(),
        favoriteGenres = emptyList(),
        badges = emptyList(),
        favoriteQuote = null,
        coverImageUrl = ""
    )

    private fun emptyUserSettings(): UserSettings = UserSettings(
        ageRange = UserDemographicCatalog.UNSPECIFIED_CODE,
        genderCode = UserDemographicCatalog.UNSPECIFIED_CODE,
        regionCode = UserDemographicCatalog.UNSPECIFIED_CODE,
        preferredGenres = emptyList(),
        preferredDurations = emptyList(),
        notificationsEnabled = true,
        culturalAlertsEnabled = true,
        autoplayNextEpisode = true,
        hasCompletedOnboarding = false
    )
}
