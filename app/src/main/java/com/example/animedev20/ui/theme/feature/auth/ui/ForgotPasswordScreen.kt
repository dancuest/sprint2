package com.example.animedev20.ui.theme.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer

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
    var email by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recuperar contraseña",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "En modo demo, el token de recuperación se mostrará aquí.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.message != null) {
            Text(
                text = uiState.message!!,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (uiState.demoResetToken != null) {
            Text(
                text = "Token demo: ${uiState.demoResetToken}",
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        if (uiState.demoResetExpiresAt != null) {
            Text(
                text = "Expira: ${uiState.demoResetExpiresAt}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { viewModel.forgotPassword(email) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generar token")
        }

        if (uiState.demoResetToken != null) {
            TextButton(onClick = onGoToReset) {
                Text("Ir a restablecer contraseña")
            }
        }

        TextButton(onClick = onGoToLogin) {
            Text("Volver al login")
        }

        TextButton(onClick = onGoBack) {
            Text("Atrás")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun ForgotPasswordPreview() {
    Surface {
        ForgotPasswordScreen(
            onGoToReset = {},
            onGoToLogin = {},
            onGoBack = {}
        )
    }
}
