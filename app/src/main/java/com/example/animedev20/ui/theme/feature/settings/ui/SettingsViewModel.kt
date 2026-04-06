package com.example.animedev20.ui.theme.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.data.remote.ChangePasswordRequest
import com.example.animedev20.ui.theme.data.remote.UsersApi
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserDemographicCatalog
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val animeRepository: AnimeRepository,
    private val homeRefreshBus: HomeRefreshBus,
    private val usersApi: UsersApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                val settingsDeferred = async { userRepository.getUserSettings() }
                val profileDeferred = async { userRepository.getUserProfile() }
                val genresDeferred = async { animeRepository.getGenres() }

                Triple(
                    settingsDeferred.await(),
                    profileDeferred.await(),
                    genresDeferred.await()
                )
            }.onSuccess { (settings, profile, genres) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableGenres = genres,
                        selectedGenres = settings.preferredGenres.map { genre -> genre.id }.toSet(),
                        preferredDurations = settings.preferredDurations.toSet(),
                        ageRange = settings.ageRange,
                        genderCode = settings.genderCode,
                        regionCode = settings.regionCode,
                        notificationsEnabled = settings.notificationsEnabled,
                        culturalAlertsEnabled = settings.culturalAlertsEnabled,
                        autoplayNextEpisode = settings.autoplayNextEpisode,
                        hasCompletedOnboarding = settings.hasCompletedOnboarding,
                        name = profile.name,
                        email = profile.email,
                        nickname = profile.nickname,
                        avatarUrl = profile.avatarUrl,
                        coverImageUrl = profile.coverImageUrl,
                        isGuest = profile.email.isBlank(),
                        message = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "No pudimos cargar tus ajustes"
                    )
                }
            }
        }
    }

    fun onGenreSelected(genreId: String) {
        _uiState.update { state ->
            val newSelection = state.selectedGenres.toMutableSet().apply {
                if (!add(genreId)) remove(genreId)
            }
            state.copy(selectedGenres = newSelection)
        }
    }

    fun onDurationSelected(duration: DurationType) {
        _uiState.update { state ->
            val updated = state.preferredDurations.toMutableSet().apply {
                if (!add(duration)) remove(duration)
            }
            state.copy(preferredDurations = updated)
        }
    }

    fun onAgeRangeSelected(code: Int) {
        _uiState.update { it.copy(ageRange = code) }
    }

    fun onGenderSelected(code: Int) {
        _uiState.update { it.copy(genderCode = code) }
    }

    fun onRegionSelected(code: Int) {
        _uiState.update { it.copy(regionCode = code) }
    }

    fun onNotificationsToggled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun onCulturalAlertsToggled(enabled: Boolean) {
        _uiState.update { it.copy(culturalAlertsEnabled = enabled) }
    }

    fun onAutoplayToggled(enabled: Boolean) {
        _uiState.update { it.copy(autoplayNextEpisode = enabled) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onNicknameChanged(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun onAvatarImageSelected(imageDataUrl: String) {
        if (_uiState.value.isGuest) return

        viewModelScope.launch {
            runCatching {
                userRepository.updateProfileImages(avatarUrl = imageDataUrl)
            }.onSuccess { updatedProfile ->
                _uiState.update {
                    it.copy(
                        avatarUrl = updatedProfile.avatarUrl,
                        coverImageUrl = updatedProfile.coverImageUrl,
                        message = "Foto de perfil actualizada"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(message = error.message ?: "No se pudo actualizar la foto de perfil")
                }
            }
        }
    }

    fun onCoverImageSelected(imageDataUrl: String) {
        if (_uiState.value.isGuest) return

        viewModelScope.launch {
            runCatching {
                userRepository.updateProfileImages(coverImageUrl = imageDataUrl)
            }.onSuccess { updatedProfile ->
                _uiState.update {
                    it.copy(
                        avatarUrl = updatedProfile.avatarUrl,
                        coverImageUrl = updatedProfile.coverImageUrl,
                        message = "Portada actualizada"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(message = error.message ?: "No se pudo actualizar la portada")
                }
            }
        }
    }

    fun savePreferences() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedGenres = state.availableGenres.filter { genre ->
                state.selectedGenres.contains(genre.id)
            }

            runCatching {
                val currentSettings = userRepository.getUserSettings()
                val updatedSettings = currentSettings.copy(
                    ageRange = state.ageRange,
                    genderCode = state.genderCode,
                    regionCode = state.regionCode,
                    preferredGenres = selectedGenres,
                    preferredDurations = state.preferredDurations.toList(),
                    notificationsEnabled = state.notificationsEnabled,
                    culturalAlertsEnabled = state.culturalAlertsEnabled,
                    autoplayNextEpisode = state.autoplayNextEpisode,
                    hasCompletedOnboarding = currentSettings.hasCompletedOnboarding
                )
                userRepository.updateUserSettings(updatedSettings)
            }.onSuccess {
                homeRefreshBus.trigger()
                _uiState.update { current ->
                    current.copy(message = "Preferencias actualizadas")
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(message = error.message)
                }
            }
        }
    }

    fun saveAccountInfo() {
        viewModelScope.launch {
            val state = _uiState.value

            runCatching {
                userRepository.updateAccountInfo(state.name, state.email, state.nickname)
            }.onSuccess { updatedProfile ->
                _uiState.update { current ->
                    current.copy(
                        message = "Perfil actualizado correctamente",
                        name = updatedProfile.name,
                        email = updatedProfile.email,
                        nickname = updatedProfile.nickname,
                        avatarUrl = updatedProfile.avatarUrl,
                        coverImageUrl = updatedProfile.coverImageUrl,
                        isGuest = updatedProfile.email.isBlank()
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(
                        message = error.message ?: "Hubo un error al guardar tus datos"
                    )
                }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (_uiState.value.isGuest) return

        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _uiState.update { it.copy(message = "Completa ambos campos") }
            return
        }
        if (newPassword.length < 6) {
            _uiState.update { it.copy(message = "La nueva contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true) }

            runCatching {
                usersApi.changePassword(
                    ChangePasswordRequest(
                        currentPassword = currentPassword,
                        newPassword = newPassword
                    )
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        message = "Contraseña actualizada correctamente",
                        passwordChangeSuccess = true
                    )
                }
            }.onFailure { error ->
                val backendMessage = when (error) {
                    is retrofit2.HttpException -> {
                        try {
                            error.response()?.errorBody()?.string()
                                ?.substringAfter("\"message\":\"")
                                ?.substringBefore("\"")
                                ?.replace("\\\"", "\"")
                        } catch (_: Exception) {
                            null
                        }
                    }
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        message = backendMessage ?: error.message ?: "No se pudo cambiar la contraseña"
                    )
                }
            }
        }
    }

    fun onPasswordChangeConsumed() {
        _uiState.update { it.copy(passwordChangeSuccess = false) }
    }

    fun onMessageConsumed() {
        _uiState.update { it.copy(message = null) }
    }

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            animeRepository: AnimeRepository,
            homeRefreshBus: HomeRefreshBus,
            usersApi: UsersApi
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(
                        userRepository,
                        animeRepository,
                        homeRefreshBus,
                        usersApi
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val availableGenres: List<Genre> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val preferredDurations: Set<DurationType> = emptySet(),
    val ageRange: Int = UserDemographicCatalog.UNSPECIFIED_CODE,
    val genderCode: Int = UserDemographicCatalog.UNSPECIFIED_CODE,
    val regionCode: Int = UserDemographicCatalog.UNSPECIFIED_CODE,
    val notificationsEnabled: Boolean = true,
    val culturalAlertsEnabled: Boolean = true,
    val autoplayNextEpisode: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val name: String = "",
    val email: String = "",
    val nickname: String = "",
    val avatarUrl: String = "",
    val coverImageUrl: String = "",
    val isGuest: Boolean = true,
    val isChangingPassword: Boolean = false,
    val passwordChangeSuccess: Boolean = false,
    val message: String? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
)