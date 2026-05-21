package com.example.animedev20.ui.theme.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevFormValidators

@Composable
fun RegisterScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onAuthSuccessRoute: (String) -> Unit,
    onGoToLogin: () -> Unit,
    onGoBack: () -> Unit = {},
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(
            appContext = LocalContext.current.applicationContext,
            userRepository = appContainer.userRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var displayName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var wasSubmitted by rememberSaveable { mutableStateOf(false) }

    val nameValidation = AnimeDevFormValidators.validateDisplayName(displayName)
    val emailValidation = AnimeDevFormValidators.validateEmail(email)
    val passwordValidation = AnimeDevFormValidators.validatePassword(password)
    val confirmPasswordValidation = AnimeDevFormValidators.validateConfirmPassword(
        password = password,
        confirmPassword = confirmPassword
    )

    val showNameError = wasSubmitted && !nameValidation.isValid
    val showEmailError = wasSubmitted && !emailValidation.isValid
    val showPasswordError = wasSubmitted && !passwordValidation.isValid
    val showConfirmPasswordError = wasSubmitted && !confirmPasswordValidation.isValid

    val canSubmit = !uiState.isLoading &&
            nameValidation.isValid &&
            emailValidation.isValid &&
            passwordValidation.isValid &&
            confirmPasswordValidation.isValid

    LaunchedEffect(uiState.nextRoute) {
        val route = uiState.nextRoute ?: return@LaunchedEffect
        onAuthSuccessRoute(route)
        viewModel.consumeNavigation()
    }

    fun submitRegister() {
        wasSubmitted = true
        focusManager.clearFocus()

        if (canSubmit) {
            viewModel.register(
                email = email,
                password = password,
                displayName = displayName
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0533),
                        Color(0xFF2D1B69)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = AnimeDevCopy.Actions.goBack,
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AnimeDevCopy.Auth.registerTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = AnimeDevCopy.Auth.registerSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                        }
                    },
                    label = {
                        Text(AnimeDevCopy.Auth.displayNameLabel)
                    },
                    placeholder = {
                        Text(AnimeDevCopy.Auth.displayNamePlaceholder)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = AnimeDevCopy.Accessibility.userIcon,
                            tint = if (showNameError) {
                                Color(0xFFFF6B6B)
                            } else {
                                Color(0xFF9D7CFF)
                            }
                        )
                    },
                    isError = showNameError,
                    supportingText = {
                        if (showNameError) {
                            Text(
                                text = nameValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = "Este nombre aparecerá en tu perfil.",
                                color = Color.White.copy(alpha = 0.48f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                        }
                    },
                    label = {
                        Text(AnimeDevCopy.Auth.emailLabel)
                    },
                    placeholder = {
                        Text(AnimeDevCopy.Auth.emailPlaceholder)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = AnimeDevCopy.Accessibility.emailIcon,
                            tint = if (showEmailError) {
                                Color(0xFFFF6B6B)
                            } else {
                                Color(0xFF9D7CFF)
                            }
                        )
                    },
                    isError = showEmailError,
                    supportingText = {
                        if (showEmailError) {
                            Text(
                                text = emailValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = "Lo usaremos para iniciar sesión y recuperar tu cuenta.",
                                color = Color.White.copy(alpha = 0.48f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                        }
                    },
                    label = {
                        Text(AnimeDevCopy.Auth.passwordLabel)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = AnimeDevCopy.Accessibility.passwordIcon,
                            tint = if (showPasswordError) {
                                Color(0xFFFF6B6B)
                            } else {
                                Color(0xFF9D7CFF)
                            }
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                passwordVisible = !passwordVisible
                            }
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    AnimeDevCopy.Accessibility.hidePassword
                                } else {
                                    AnimeDevCopy.Accessibility.showPassword
                                },
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = showPasswordError,
                    supportingText = {
                        if (showPasswordError) {
                            Text(
                                text = passwordValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = "Mínimo 6 caracteres. Fácil para ti, difícil para los villanos.",
                                color = Color.White.copy(alpha = 0.48f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                        }
                    },
                    label = {
                        Text(AnimeDevCopy.Auth.confirmPasswordLabel)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = AnimeDevCopy.Accessibility.passwordIcon,
                            tint = if (showConfirmPasswordError) {
                                Color(0xFFFF6B6B)
                            } else {
                                Color(0xFF9D7CFF)
                            }
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                confirmPasswordVisible = !confirmPasswordVisible
                            }
                        ) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (confirmPasswordVisible) {
                                    AnimeDevCopy.Accessibility.hidePassword
                                } else {
                                    AnimeDevCopy.Accessibility.showPassword
                                },
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = showConfirmPasswordError,
                    supportingText = {
                        if (showConfirmPasswordError) {
                            Text(
                                text = confirmPasswordValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = "Confirma tu contraseña para evitar errores de escritura.",
                                color = Color.White.copy(alpha = 0.48f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitRegister()
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )

                uiState.message?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF9D7CFF),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            submitRegister()
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF),
                            disabledContainerColor = Color(0xFF6C63FF).copy(alpha = 0.4f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = AnimeDevCopy.Actions.register,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AnimeDevCopy.Auth.alreadyHaveAccount,
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    TextButton(onClick = onGoToLogin) {
                        Text(
                            text = AnimeDevCopy.Actions.goToLogin,
                            color = Color(0xFF9D7CFF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}