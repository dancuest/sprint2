package com.example.animedev20.ui.theme.data.remote

data class ApiResponse<T>(
    val data: T,
    val meta: Meta? = null
)

data class Meta(
    val limit: Int? = null
)
