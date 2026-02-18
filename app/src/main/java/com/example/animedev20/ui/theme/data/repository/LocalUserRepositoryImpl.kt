package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.local.UserPrefsKeys
import com.example.animedev20.ui.theme.data.local.userPrefsDataStore
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class LocalUserRepositoryImpl(
    private val context: Context
) : UserRepository {

    private val dataStore = context.userPrefsDataStore

    override suspend fun getPreferredGenres(): List<Genre> = getUserSettings().preferredGenres

    override suspend fun getUserProfile(): UserProfile = observeUserProfile().first()

    override suspend fun updateUserProfile(profile: UserProfile): UserProfile {
        dataStore.edit { prefs ->
            prefs[UserPrefsKeys.DISPLAY_NAME] = profile.name
            prefs[UserPrefsKeys.BIO] = profile.biography
            prefs[UserPrefsKeys.PROFILE_IMAGE_URI] = profile.avatarUrl
            prefs[UserPrefsKeys.LEVEL] = profile.knowledgeLevel
            prefs[UserPrefsKeys.COMPLETED_TRIVIAS] = profile.completedTrivias
            prefs[UserPrefsKeys.PREFERRED_GENRES] = serializeGenres(profile.favoriteGenres)
            prefs[UserPrefsKeys.PREFERRED_GENRES] = profile.favoriteGenres.joinToString(",") { it.id }
            prefs[UserPrefsKeys.ACCOUNT_NAME] = profile.name
            prefs[UserPrefsKeys.ACCOUNT_EMAIL] = profile.email
            prefs[UserPrefsKeys.ACCOUNT_NICKNAME] = profile.nickname
        }
        return getUserProfile()
    }

    override fun observeUserProfile(): Flow<UserProfile> = dataStore.data.map { prefs ->
        val default = FakeDataSource.defaultUserProfile
        val favoriteGenres = deserializeGenres(prefs[UserPrefsKeys.PREFERRED_GENRES])
            .ifEmpty { default.favoriteGenres }
        val genresRaw = prefs[UserPrefsKeys.PREFERRED_GENRES]
        val favoriteGenres = genresRaw
            ?.split(',')
            ?.mapNotNull { id -> FakeDataSource.genres.firstOrNull { it.id == id } }
            ?.takeIf { it.isNotEmpty() }
            ?: default.favoriteGenres

        default.copy(
            name = prefs[UserPrefsKeys.ACCOUNT_NAME] ?: default.name,
            nickname = prefs[UserPrefsKeys.ACCOUNT_NICKNAME] ?: default.nickname,
            email = prefs[UserPrefsKeys.ACCOUNT_EMAIL] ?: default.email,
            biography = prefs[UserPrefsKeys.BIO] ?: default.biography,
            avatarUrl = prefs[UserPrefsKeys.PROFILE_IMAGE_URI] ?: default.avatarUrl,
            knowledgeLevel = prefs[UserPrefsKeys.LEVEL] ?: default.knowledgeLevel,
            completedTrivias = prefs[UserPrefsKeys.COMPLETED_TRIVIAS] ?: default.completedTrivias,
            favoriteGenres = favoriteGenres
        )
    }

    override suspend fun getUserSettings(): UserSettings = observeUserSettings().first()

    private fun observeUserSettings(): Flow<UserSettings> = dataStore.data.map { prefs ->
        val default = FakeDataSource.defaultUserSettings
        val preferredGenres = deserializeGenres(prefs[UserPrefsKeys.PREFERRED_GENRES])
            .ifEmpty { default.preferredGenres }

        default.copy(
    override suspend fun getUserSettings(): UserSettings {
        val prefs = dataStore.data.first()
        val default = FakeDataSource.defaultUserSettings
        val genresRaw = prefs[UserPrefsKeys.PREFERRED_GENRES]
        val preferredGenres = genresRaw
            ?.split(',')
            ?.mapNotNull { id -> FakeDataSource.genres.firstOrNull { it.id == id } }
            ?.takeIf { it.isNotEmpty() }
            ?: default.preferredGenres

        return default.copy(
            preferredGenres = preferredGenres,
            notificationsEnabled = prefs[UserPrefsKeys.NOTIFICATIONS] ?: default.notificationsEnabled,
            culturalAlertsEnabled = prefs[UserPrefsKeys.CULTURAL_ALERTS] ?: default.culturalAlertsEnabled,
            autoplayNextEpisode = prefs[UserPrefsKeys.AUTOPLAY] ?: default.autoplayNextEpisode
        )
    }

    override suspend fun updateUserSettings(settings: UserSettings): UserSettings {
        dataStore.edit { prefs ->
            prefs[UserPrefsKeys.PREFERRED_GENRES] = serializeGenres(settings.preferredGenres)
            prefs[UserPrefsKeys.PREFERRED_GENRES] = settings.preferredGenres.joinToString(",") { it.id }
            prefs[UserPrefsKeys.NOTIFICATIONS] = settings.notificationsEnabled
            prefs[UserPrefsKeys.CULTURAL_ALERTS] = settings.culturalAlertsEnabled
            prefs[UserPrefsKeys.AUTOPLAY] = settings.autoplayNextEpisode
            prefs[UserPrefsKeys.TEXT_SIZE] = "medium"
        }
        return getUserSettings()
    }

    override suspend fun updatePreferredGenres(genres: List<Genre>): List<Genre> {
        dataStore.edit { prefs ->
            prefs[UserPrefsKeys.PREFERRED_GENRES] = serializeGenres(genres)
            prefs[UserPrefsKeys.PREFERRED_GENRES] = genres.joinToString(",") { it.id }
        }
        return getPreferredGenres()
    }

    override suspend fun updateAccountInfo(name: String, email: String, nickname: String): UserProfile {
        val current = getUserProfile()
        return updateUserProfile(current.copy(name = name, email = email, nickname = nickname))
    }

    private fun serializeGenres(genres: List<Genre>): String = genres.joinToString(";") { genre ->
        listOf(genre.id, genre.name, genre.imageUrl)
            .joinToString("|") { value -> value.urlEncode() }
    }

    private fun deserializeGenres(raw: String?): List<Genre> {
        if (raw.isNullOrBlank()) return emptyList()

        return if (!raw.contains("|")) {
            raw.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { id -> Genre(id = id, name = id, imageUrl = "") }
        } else {
            raw.split(';').mapNotNull { encodedGenre ->
                if (encodedGenre.isBlank()) return@mapNotNull null
                val parts = encodedGenre.split('|')
                if (parts.size < 3) return@mapNotNull null
                Genre(
                    id = parts[0].urlDecode(),
                    name = parts[1].urlDecode(),
                    imageUrl = parts[2].urlDecode()
                )
            }
        }
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun String.urlDecode(): String = URLDecoder.decode(this, Charsets.UTF_8.name())
}
