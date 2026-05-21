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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
fun ResetPasswordScreen(
    appContainer: AppContainer = DefaultAppContainer(),
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

    var email by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var wasSubmitted by rememberSaveable { mutableStateOf(false) }
    var passwordWasUpdated by rememberSaveable { mutableStateOf(false) }

    val emailValidation = AnimeDevFormValidators.validateEmail(email)
    val tokenValidation = AnimeDevFormValidators.validateResetToken(token)
    val newPasswordValidation = AnimeDevFormValidators.validateNewPassword(newPassword)
    val confirmPasswordValidation = AnimeDevFormValidators.validateConfirmPassword(
        password = newPassword,
        confirmPassword = confirmPassword
    )

    val showEmailError = wasSubmitted && !emailValidation.isValid
    val showTokenError = wasSubmitted && !tokenValidation.isValid
    val showNewPasswordError = wasSubmitted && !newPasswordValidation.isValid
    val showConfirmPasswordError = wasSubmitted && !confirmPasswordValidation.isValid

    val canSubmit = AnimeDevFormValidators.canSubmitResetPassword(
        email = email,
        token = token,
        newPassword = newPassword,
        confirmPassword = confirmPassword,
        isLoading = uiState.isLoading
    )

    val messageIsSuccess = uiState.message?.contains(
        other = "actualizada",
        ignoreCase = true
    ) == true

    if (messageIsSuccess) {
        passwordWasUpdated = true
    }

    fun submitResetPassword() {
        wasSubmitted = true
        focusManager.clearFocus()

        if (canSubmit) {
            viewModel.resetPassword(
                email = email,
                token = token,
                newPassword = newPassword
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
                        text = AnimeDevCopy.Auth.resetPasswordTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = AnimeDevCopy.Auth.resetPasswordSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                if (passwordWasUpdated) {
                    PasswordUpdatedCard()
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                            passwordWasUpdated = false
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
                                text = "Debe ser el mismo correo con el que generaste el token.",
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
                    value = token,
                    onValueChange = {
                        token = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                            passwordWasUpdated = false
                        }
                    },
                    label = {
                        Text(AnimeDevCopy.Auth.tokenLabel)
                    },
                    placeholder = {
                        Text(AnimeDevCopy.Auth.tokenPlaceholder)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = AnimeDevCopy.Accessibility.tokenIcon,
                            tint = if (showTokenError) {
                                Color(0xFFFF6B6B)
                            } else {
                                Color(0xFF9D7CFF)
                            }
                        )
                    },
                    isError = showTokenError,
                    supportingText = {
                        if (showTokenError) {
                            Text(
                                text = tokenValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = "Pega el token temporal que generaste en la pantalla anterior.",
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
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        if (wasSubmitted) {
                            viewModel.consumeMessage()
                            passwordWasUpdated = false
                        }
                    },
                    label = {
                        Text(AnimeDevCopy.Auth.newPasswordLabel)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = AnimeDevCopy.Accessibility.passwordIcon,
                            tint = if (showNewPasswordError) {
                                Color(0xFFFF6B6B)
                            } else {
                                Color(0xFF9D7CFF)
                            }
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                newPasswordVisible = !newPasswordVisible
                            }
                        ) {
                            Icon(
                                imageVector = if (newPasswordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (newPasswordVisible) {
                                    AnimeDevCopy.Accessibility.hidePassword
                                } else {
                                    AnimeDevCopy.Accessibility.showPassword
                                },
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    visualTransformation = if (newPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = showNewPasswordError,
                    supportingText = {
                        if (showNewPasswordError) {
                            Text(
                                text = newPasswordValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        } else {
                            Text(
                                text = "Usa mínimo 6 caracteres.",
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
                            passwordWasUpdated = false
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
                                text = "Vuelve a escribir la contraseña para confirmar.",
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
                            submitResetPassword()
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )

                uiState.message?.let { message ->
                    Text(
                        text = message,
                        color = if (messageIsSuccess) {
                            Color(0xFF6EE7B7)
                        } else {
                            Color(0xFFFF6B6B)
                        },
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
                            submitResetPassword()
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
                            text = AnimeDevCopy.Auth.goToResetPassword,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                TextButton(
                    onClick = onGoToLogin,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = AnimeDevCopy.Auth.backToLogin,
                        color = Color.White.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PasswordUpdatedCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF6EE7B7)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = AnimeDevCopy.Auth.passwordUpdatedTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = AnimeDevCopy.Auth.passwordUpdatedMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.68f)
                )
            }
        }
    }
}