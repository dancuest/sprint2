package com.example.animedev20.ui.theme.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class DeviceLoginRequest(val deviceId: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val token: String,
    val newPassword: String
)

data class AuthProfileDto(
    val id: String,
    val email: String? = null,
    val displayName: String? = null,
    val deviceId: String? = null,
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null
)

data class AuthSessionResponse(
    val userId: String,
    @SerializedName("access_token")
    val accessToken: String,
    val authMode: String? = null,
    val profile: AuthProfileDto? = null
)

data class ForgotPasswordResponse(
    val success: Boolean,
    val message: String,
    val resetToken: String? = null,
    val expiresAt: String? = null,
    val note: String? = null
)

data class BasicMessageResponse(
    val success: Boolean,
    val message: String
)

interface AuthApiPlain {
    @POST("auth/device")
    suspend fun loginDevice(@Body req: DeviceLoginRequest): AuthSessionResponse

    @POST("auth/login")
    suspend fun login(@Body req: LoginRequest): AuthSessionResponse

    @POST("auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthSessionResponse

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest): ForgotPasswordResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest): BasicMessageResponse
}