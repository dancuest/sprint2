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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevFormValidators

@Composable
fun ForgotPasswordScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onGoToReset: () -> Unit,
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
    var wasSubmitted by rememberSaveable { mutableStateOf(false) }

    val emailValidation = AnimeDevFormValidators.validateEmail(email)
    val showEmailError = wasSubmitted && !emailValidation.isValid
    val canSubmit = AnimeDevFormValidators.canSubmitForgotPassword(
        email = email,
        isLoading = uiState.isLoading
    )

    val tokenWasGenerated = uiState.demoResetToken != null
    val messageColor = if (tokenWasGenerated) {
        Color(0xFF6EE7B7)
    } else {
        Color(0xFFFF6B6B)
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
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AnimeDevCopy.Auth.forgotPasswordTitle,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = AnimeDevCopy.Auth.forgotPasswordSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.68f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                    supportingText = if (showEmailError) {
                        {
                            Text(
                                text = emailValidation.message.orEmpty(),
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    } else {
                        {
                            Text(
                                text = "Usa el correo asociado a tu cuenta de AnimeDev.",
                                color = Color.White.copy(alpha = 0.48f)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            wasSubmitted = true

                            if (canSubmit) {
                                viewModel.forgotPassword(email)
                            }
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = authTextFieldColors()
                )

                uiState.message?.let { message ->
                    Text(
                        text = message,
                        color = messageColor,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (tokenWasGenerated) {
                    RecoveryTokenCard(
                        token = uiState.demoResetToken.orEmpty(),
                        expiresAt = uiState.demoResetExpiresAt
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
                            wasSubmitted = true

                            if (canSubmit) {
                                focusManager.clearFocus()
                                viewModel.forgotPassword(email)
                            }
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
                            text = AnimeDevCopy.Auth.generateToken,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (tokenWasGenerated) {
                    Button(
                        onClick = onGoToReset,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9D7CFF)
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
private fun RecoveryTokenCard(
    token: String,
    expiresAt: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.10f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6EE7B7)
                )

                Text(
                    text = AnimeDevCopy.Auth.demoTokenLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Copia este token y úsalo en la pantalla de restablecer contraseña.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.68f)
            )

            SelectionContainer {
                Text(
                    text = token,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF6EE7B7),
                    fontWeight = FontWeight.SemiBold
                )
            }

            expiresAt?.takeIf { it.isNotBlank() }?.let { expiration ->
                Text(
                    text = "${AnimeDevCopy.Auth.demoTokenExpirationLabel}: $expiration",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.58f)
                )
            }
        }
    }
}