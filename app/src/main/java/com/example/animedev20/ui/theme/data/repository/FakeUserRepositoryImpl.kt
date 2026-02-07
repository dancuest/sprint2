package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FakeUserRepositoryImpl : UserRepository {

    private var cachedSettings: UserSettings = FakeDataSource.defaultUserSettings
    private val profileFlow = MutableStateFlow(FakeDataSource.defaultUserProfile)

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
        val updatedProfile = profileFlow.value.copy(
            favoriteGenres = settings.preferredGenres,
            preferredDuration = settings.preferredDuration
        )
        profileFlow.value = updatedProfile
        return cachedSettings
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
}