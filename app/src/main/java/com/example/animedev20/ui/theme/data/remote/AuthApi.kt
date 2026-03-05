package com.example.animedev20.ui.theme.data.remote

import com.example.animedev20.ui.theme.domain.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class DeviceLoginRequest(val deviceId: String)
data class AuthResponse(val user: UserProfile, val token: String)

interface AuthApi {
    @POST("auth/device")
    suspend fun loginDevice(@Body request: DeviceLoginRequest): ApiResponse<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): ApiResponse<UserProfile>
}
