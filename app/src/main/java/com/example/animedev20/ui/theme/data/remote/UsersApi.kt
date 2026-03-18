package com.example.animedev20.ui.theme.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

data class UserMeDto(
    val id: String,
    val deviceId: String?,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null,
    val createdAt: String?,
    val completedTrivias: Int? = null,
    val favoriteCount: Int? = null
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null
)

data class GenreDto(
    val id: Int,
    val name: String
)

data class UserSettingsDto(
    val userId: String,
    val ageRange: Int? = null,
    val genderCode: Int? = null,
    val regionCode: Int? = null,
    val preferredGenres: List<Int> = emptyList(),
    val preferredGenreDetails: List<GenreDto>? = null,
    val preferredDurations: List<String> = emptyList(),
    val toggles: Map<String, Any?> = emptyMap(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class UpdateSettingsRequest(
    val ageRange: Int? = null,
    val genderCode: Int? = null,
    val regionCode: Int? = null,
    val preferredGenres: List<Int>? = null,
    val preferredDurations: List<String>? = null,
    val toggles: Map<String, Any?>? = null
)

interface UsersApi {
    @GET("users/me")
    suspend fun me(): UserMeDto

    @PUT("users/me/profile")
    suspend fun updateProfile(@Body req: UpdateProfileRequest): UserMeDto

    @GET("users/me/settings")
    suspend fun getSettings(): UserSettingsDto

    @PUT("users/me/settings")
    suspend fun updateSettings(@Body req: UpdateSettingsRequest): UserSettingsDto
}