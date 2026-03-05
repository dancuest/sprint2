package com.example.animedev20.ui.theme.data.remote.auth

import com.google.gson.annotations.SerializedName

data class DeviceLoginRequest(
    val deviceId: String
)

data class DeviceLoginResponse(
    val userId: String,
    @SerializedName("access_token")
    val accessToken: String
)
