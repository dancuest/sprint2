package com.example.animedev20.ui.theme.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

data class DeviceLoginRequest(val deviceId: String)

data class DeviceLoginResponse(
    val userId: String,
    @SerializedName("access_token")
    val accessToken: String
)

interface AuthApiPlain {
    @POST("auth/device")
    suspend fun loginDevice(@Body req: DeviceLoginRequest): DeviceLoginResponse
}
