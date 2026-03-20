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
    private val userRepository: UserRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun continueAsGuest() {
        viewModelScope.launch {
            executeAuthAction {
                val deviceId = tokenStore.getOrCreateDeviceId(appContext)
                val response = authApi.loginDevice(DeviceLoginRequest(deviceId))
                persistSession(response)
                navigateToNextRoute()
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            showMessage("Completa correo y contraseña")
            return
        }

        viewModelScope.launch {
            executeAuthAction {
                val response = authApi.login(
                    LoginRequest(
                        email = email.trim(),
                        password = password
                    )
                )
                persistSession(response)
                navigateToNextRoute()
            }
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

        viewModelScope.launch {
            executeAuthAction {
                val response = authApi.register(
                    RegisterRequest(
                        email = email.trim(),
                        password = password,
                        displayName = displayName.trim().ifBlank { null }
                    )
                )
                persistSession(response)
                navigateToNextRoute()
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            showMessage("Ingresa tu correo")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

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
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

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

    private suspend fun persistSession(response: AuthSessionResponse) {
        tokenStore.saveToken(response.accessToken)
        tokenStore.saveUserId(response.userId)
    }

    private suspend fun navigateToNextRoute() {
        val next = runCatching {
            val settings = userRepository.getUserSettings()
            if (settings.hasCompletedOnboarding) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }
        }.getOrElse {
            Screen.Home.route
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            nextRoute = next,
            message = null
        )
    }

    private fun showMessage(text: String) {
        _uiState.value = _uiState.value.copy(message = text)
    }

    private fun executeAuthAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

            runCatching { block() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = error.message ?: "Ocurrió un error de autenticación"
                    )
                }
        }
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
                    userRepository = userRepository,
                    appContext = appContext.applicationContext
                ) as T
            }
        }
    }
}