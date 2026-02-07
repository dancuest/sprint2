package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getPreferredGenres(): List<Genre>
    suspend fun getUserProfile(): UserProfile
    fun observeUserProfile(): Flow<UserProfile>
    suspend fun getUserSettings(): UserSettings
    suspend fun updateUserSettings(settings: UserSettings): UserSettings
    suspend fun updateAccountInfo(name: String, email: String, nickname: String): UserProfile
}