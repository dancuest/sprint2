package com.example.animedev20.ui.theme.feature.auth.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.ux.AnimeDevCopy

@Composable
fun AuthWelcomeScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onGoToLogin: () -> Unit,
    onGoToRegister: () -> Unit,
    onAuthSuccessRoute: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.provideFactory(
            appContext = LocalContext.current.applicationContext,
            userRepository = appContainer.userRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.nextRoute) {
        val route = uiState.nextRoute ?: return@LaunchedEffect
        onAuthSuccessRoute(route)
        viewModel.consumeNavigation()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0533),
                        Color(0xFF2D1B69),
                        Color(0xFF1A0533)
                    )
                )
            )
    ) {
        DecorativeWelcomeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            WelcomeHero()

            WelcomeActions(
                isLoading = uiState.isLoading,
                message = uiState.message,
                onGoToLogin = onGoToLogin,
                onGoToRegister = onGoToRegister,
                onContinueAsGuest = viewModel::continueAsGuest
            )
        }
    }
}

@Composable
private fun DecorativeWelcomeBackground() {
    Box(
        modifier = Modifier
            .size(300.dp)
            .clearAndSetSemantics { }
            .padding(top = 40.dp)
            .clip(CircleShape)
            .background(
                Color(0xFF6C63FF).copy(alpha = 0.15f)
            )
    )

    Box(
        modifier = Modifier
            .size(210.dp)
            .clearAndSetSemantics { }
            .padding(bottom = 60.dp)
            .clip(CircleShape)
            .background(
                Color(0xFFFF6B9D).copy(alpha = 0.10f)
            )
    )
}

@Composable
private fun WelcomeHero() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9D7CFF),
                            Color(0xFF6C63FF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = AnimeDevCopy.Accessibility.appLogo,
                tint = Color.White,
                modifier = Modifier.size(52.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = AnimeDevCopy.Auth.welcomeTitle,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = AnimeDevCopy.Auth.welcomeSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.09f)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = AnimeDevCopy.Auth.welcomeHelper,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun WelcomeActions(
    isLoading: Boolean,
    message: String?,
    onGoToLogin: () -> Unit,
    onGoToRegister: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        message?.let {
            Text(
                text = it,
                color = Color(0xFFFF6B6B),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = onGoToLogin,
            enabled = !isLoading,
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
                text = AnimeDevCopy.Actions.login,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = onGoToRegister,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.45f)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = Color.White.copy(alpha = if (isLoading) 0.25f else 0.5f)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = AnimeDevCopy.Actions.register,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF9D7CFF),
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(32.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = onContinueAsGuest
                ) {
                    Text(
                        text = AnimeDevCopy.Actions.continueAsGuest,
                        color = Color.White.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = AnimeDevCopy.Auth.guestHelper,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.48f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}