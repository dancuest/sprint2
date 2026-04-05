package com.example.animedev20.ui.theme.feature.auth.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.remote.AnimeApiFactory
import com.example.animedev20.ui.theme.data.remote.ApiConfig
import com.example.animedev20.ui.theme.data.remote.AuthApiPlain
import com.example.animedev20.ui.theme.data.remote.AuthSessionResponse
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.DeviceLoginRequest
import com.example.animedev20.ui.theme.data.remote.ForgotPasswordRequest
import com.example.animedev20.ui.theme.data.remote.LoginRequest
import com.example.animedev20.ui.theme.data.remote.RegisterRequest
import com.example.animedev20.ui.theme.data.remote.ResetPasswordRequest
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import com.example.animedev20.ui.theme.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val nextRoute: String? = null,
    val demoResetToken: String? = null,
    val demoResetExpiresAt: String? = null
)

class AuthViewModel(
    private val authApi: AuthApiPlain,
    private val tokenStore: AuthTokenStore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun continueAsGuest() {
        runAuthAction {
            val deviceId = tokenStore.createFreshGuestDeviceId()
            val response = authApi.loginDevice(DeviceLoginRequest(deviceId))
            persistSession(response)
            navigateToRoute(Screen.Onboarding.route)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            showMessage("Completa correo y contraseña")
            return
        }

        runAuthAction {
            val response = authApi.login(
                LoginRequest(
                    email = email.trim(),
                    password = password
                )
            )
            persistSession(response)
            navigateAccordingToSettings()
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String
    ) {
        if (email.isBlank() || password.isBlank()) {
            showMessage("Completa correo y contraseña")
            return
        }

        runAuthAction {
            startFreshGuestSessionForRegistration()

            val response = authApi.register(
                RegisterRequest(
                    email = email.trim(),
                    password = password,
                    displayName = displayName.trim().ifBlank { null }
                )
            )

            persistSession(response)
            navigateAccordingToSettings()
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            showMessage("Ingresa tu correo")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                demoResetToken = null,
                demoResetExpiresAt = null
            )

            runCatching {
                authApi.forgotPassword(ForgotPasswordRequest(email.trim()))
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = response.message,
                    demoResetToken = response.resetToken,
                    demoResetExpiresAt = response.expiresAt
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = error.message ?: "No fue posible generar el token"
                )
            }
        }
    }

    fun resetPassword(
        email: String,
        token: String,
        newPassword: String
    ) {
        if (email.isBlank() || token.isBlank() || newPassword.isBlank()) {
            showMessage("Completa correo, token y nueva contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null
            )

            runCatching {
                authApi.resetPassword(
                    ResetPasswordRequest(
                        email = email.trim(),
                        token = token.trim(),
                        newPassword = newPassword
                    )
                )
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = response.message
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = error.message ?: "No fue posible restablecer la contraseña"
                )
            }
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun consumeNavigation() {
        _uiState.value = _uiState.value.copy(nextRoute = null)
    }

    private fun runAuthAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null
            )

            runCatching { action() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = error.message ?: "Ocurrió un error de autenticación"
                    )
                }
        }
    }

    private suspend fun startFreshGuestSessionForRegistration() {
        val deviceId = tokenStore.createFreshGuestDeviceId()
        val guestResponse = authApi.loginDevice(DeviceLoginRequest(deviceId))
        persistSession(guestResponse)
    }

    private suspend fun persistSession(response: AuthSessionResponse) {
        tokenStore.saveToken(response.accessToken)
        tokenStore.saveUserId(response.userId)
        response.profile?.deviceId
            ?.takeIf { it.isNotBlank() }
            ?.let(tokenStore::saveDeviceId)
    }

    private suspend fun navigateAccordingToSettings() {
        val nextRoute = runCatching {
            val settings = userRepository.getUserSettings()
            if (settings.hasCompletedOnboarding) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }
        }.getOrElse {
            Screen.Onboarding.route
        }

        navigateToRoute(nextRoute)
    }

    private fun navigateToRoute(route: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            nextRoute = route,
            message = null
        )
    }

    private fun showMessage(text: String) {
        _uiState.value = _uiState.value.copy(message = text)
    }

    companion object {
        fun provideFactory(
            appContext: Context,
            userRepository: UserRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val tokenStore = AuthTokenStore(appContext)
                val retrofit = AnimeApiFactory.createRetrofit(ApiConfig.baseUrl, tokenStore)
                val authApi = retrofit.create(AuthApiPlain::class.java)

                return AuthViewModel(
                    authApi = authApi,
                    tokenStore = tokenStore,
                    userRepository = userRepository
                ) as T
            }
        }
    }
}