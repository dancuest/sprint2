package com.example.animedev20.ui.theme.data.remote.users

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UsersApi {
    @GET("users/me")
    suspend fun getMe(): UserDto

    @PUT("users/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): UserDto

    @GET("users/me/settings")
    suspend fun getMySettings(): UserSettingsDto

    @PUT("users/me/settings")
    suspend fun updateMySettings(@Body request: UpdateSettingsRequest): UserSettingsDto
}
