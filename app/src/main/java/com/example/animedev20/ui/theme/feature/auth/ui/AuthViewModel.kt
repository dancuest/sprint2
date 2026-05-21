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
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevFormValidators
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
        val validationError = AnimeDevFormValidators.loginError(
            email = email,
            password = password
        )

        if (validationError != null) {
            showMessage(validationError)
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
            navigateToRoute(resolveRouteAfterLogin())
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String
    ) {
        val validationError = AnimeDevFormValidators.registerError(
            displayName = displayName,
            email = email,
            password = password
        )

        if (validationError != null) {
            showMessage(validationError)
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
            navigateToRoute(resolveRouteAfterRegistration())
        }
    }

    fun forgotPassword(email: String) {
        val validationError = AnimeDevFormValidators.forgotPasswordError(email)

        if (validationError != null) {
            showMessage(validationError)
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
                    message = AnimeDevCopy.Success.tokenGenerated,
                    demoResetToken = response.resetToken,
                    demoResetExpiresAt = response.expiresAt
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = friendlyAuthError(
                        error = error,
                        fallback = "No pudimos generar el token. Revisa el correo e inténtalo de nuevo."
                    )
                )
            }
        }
    }

    fun resetPassword(
        email: String,
        token: String,
        newPassword: String
    ) {
        val emailValidation = AnimeDevFormValidators.validateEmail(email)
        if (!emailValidation.isValid) {
            showMessage(emailValidation.message ?: AnimeDevCopy.Validation.invalidEmail)
            return
        }

        val tokenValidation = AnimeDevFormValidators.validateResetToken(token)
        if (!tokenValidation.isValid) {
            showMessage(tokenValidation.message ?: AnimeDevCopy.Validation.requiredToken)
            return
        }

        val passwordValidation = AnimeDevFormValidators.validateNewPassword(newPassword)
        if (!passwordValidation.isValid) {
            showMessage(passwordValidation.message ?: AnimeDevCopy.Validation.requiredNewPassword)
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
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Contraseña actualizada correctamente."
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = friendlyAuthError(
                        error = error,
                        fallback = "No pudimos restablecer la contraseña. Revisa el token e inténtalo de nuevo."
                    )
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

            runCatching {
                action()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = friendlyAuthError(
                        error = error,
                        fallback = AnimeDevCopy.Errors.authGeneric
                    )
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

    private suspend fun resolveRouteAfterLogin(): String {
        return resolveRouteAfterAuthentication(
            defaultRouteWhenProfileExists = Screen.Home.route,
            defaultRouteWhenEverythingFails = Screen.Home.route
        )
    }

    private suspend fun resolveRouteAfterRegistration(): String {
        return resolveRouteAfterAuthentication(
            defaultRouteWhenProfileExists = Screen.Onboarding.route,
            defaultRouteWhenEverythingFails = Screen.Onboarding.route
        )
    }

    private suspend fun resolveRouteAfterAuthentication(
        defaultRouteWhenProfileExists: String,
        defaultRouteWhenEverythingFails: String
    ): String {
        val routeFromSettings = runCatching {
            val settings = userRepository.getUserSettings()

            if (settings.hasCompletedOnboarding) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }
        }.getOrNull()

        if (routeFromSettings != null) {
            return routeFromSettings
        }

        val profile = runCatching {
            userRepository.getUserProfile()
        }.getOrNull()

        if (profile != null && profile.id.isNotBlank()) {
            return if (profile.email.isBlank()) {
                Screen.Onboarding.route
            } else {
                defaultRouteWhenProfileExists
            }
        }

        return defaultRouteWhenEverythingFails
    }

    private fun navigateToRoute(route: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            nextRoute = route,
            message = null
        )
    }

    private fun showMessage(text: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            message = text
        )
    }

    private fun friendlyAuthError(
        error: Throwable,
        fallback: String
    ): String {
        val rawMessage = error.message.orEmpty()

        return when {
            rawMessage.contains("401", ignoreCase = true) ||
                    rawMessage.contains("Unauthorized", ignoreCase = true) -> {
                "Correo o contraseña incorrectos. Revisa tus datos e inténtalo de nuevo."
            }

            rawMessage.contains("404", ignoreCase = true) ||
                    rawMessage.contains("not found", ignoreCase = true) -> {
                "No encontramos una cuenta con esos datos."
            }

            rawMessage.contains("409", ignoreCase = true) ||
                    rawMessage.contains("conflict", ignoreCase = true) -> {
                "Ya existe una cuenta registrada con ese correo."
            }

            rawMessage.contains("422", ignoreCase = true) ||
                    rawMessage.contains("400", ignoreCase = true) -> {
                "Hay un dato que no cumple el formato esperado. Revisa el formulario."
            }

            rawMessage.contains("500", ignoreCase = true) ||
                    rawMessage.contains("503", ignoreCase = true) -> {
                AnimeDevCopy.Errors.server
            }

            rawMessage.contains("timeout", ignoreCase = true) ||
                    rawMessage.contains("Unable to resolve host", ignoreCase = true) ||
                    rawMessage.contains("Failed to connect", ignoreCase = true) -> {
                AnimeDevCopy.Errors.network
            }

            else -> fallback
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
                    userRepository = userRepository
                ) as T
            }
        }
    }
}