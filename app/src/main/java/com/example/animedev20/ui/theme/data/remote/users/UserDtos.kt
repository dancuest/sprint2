package com.example.animedev20.ui.theme.data.remote.users

data class UserDto(
    val id: String,
    val deviceId: String,
    val email: String? = null,
    val displayName: String? = null,
    val createdAt: String? = null
)

data class UserSettingsDto(
    val userId: String,
    val preferredGenres: Any? = null,
    val preferredDurations: Any? = null,
    val toggles: Map<String, Boolean>? = null,
    val ageRange: String? = null,
    val genderCode: String? = null,
    val regionCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val email: String? = null
)

data class UpdateSettingsRequest(
    val preferredGenres: List<Int>? = null,
    val preferredDurations: List<String>? = null,
    val toggles: Map<String, Boolean>? = null,
    val ageRange: String? = null,
    val genderCode: String? = null,
    val regionCode: String? = null
)
