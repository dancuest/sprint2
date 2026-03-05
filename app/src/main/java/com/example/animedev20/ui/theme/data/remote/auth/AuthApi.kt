package com.example.animedev20.ui.theme.data.remote.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/device")
    suspend fun deviceLogin(@Body request: DeviceLoginRequest): DeviceLoginResponse
}
