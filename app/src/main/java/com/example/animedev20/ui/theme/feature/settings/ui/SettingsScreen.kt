package com.example.animedev20.ui.theme.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            userRepository = appContainer.userRepository,
            animeRepository = appContainer.animeRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageConsumed()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ajustes y preferencias") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isLoading) {
            SettingsLoadingState(modifier = Modifier.padding(padding))
        } else {
            SettingsContent(
                state = uiState,
                onGenreSelected = viewModel::onGenreSelected,
                onDurationSelected = viewModel::onDurationSelected,
                onSavePreferences = viewModel::savePreferences,
                onSaveAccountInfo = viewModel::saveAccountInfo,
                onNameChange = viewModel::onNameChanged,
                onEmailChange = viewModel::onEmailChanged,
                onNicknameChange = viewModel::onNicknameChanged,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onGenreSelected: (String) -> Unit,
    onDurationSelected: (DurationType) -> Unit,
    onSavePreferences: () -> Unit,
    onSaveAccountInfo: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var genreQuery by rememberSaveable { mutableStateOf("") }
    val filteredGenres = state.availableGenres.filter { genre ->
        genre.name.contains(genreQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Personaliza tu experiencia",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Estas preferencias impactarán en tus recomendaciones y trivias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            SettingSectionTitle(title = "Categorías que te interesan")
            OutlinedTextField(
                value = genreQuery,
                onValueChange = { genreQuery = it },
                label = { Text("Buscar género") },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                filteredGenres.forEach { genre ->
                    FilterChip(
                        selected = state.selectedGenres.contains(genre.id),
                        onClick = { onGenreSelected(genre.id) },
                        label = { Text(genre.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
            Text(
                text = "Seleccionados: ${state.selectedGenres.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            SettingSectionTitle(title = "Duraciones preferidas de las series")
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DurationType.values().forEach { duration ->
                    DurationPreferenceChip(
                        label = durationLabel(duration),
                        description = durationDescription(duration),
                        selected = state.preferredDurations.contains(duration),
                        onClick = { onDurationSelected(duration) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Button(
                onClick = onSavePreferences,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Guardar preferencias")
            }
        }
        item {
            SettingSectionTitle(title = "Datos del perfil")
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("Nombre público o nickname") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = onSaveAccountInfo,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Actualizar datos de perfil")
                }
            }
        }
    }
}

@Composable
private fun SettingsLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Cargando tus preferencias…")
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DurationPreferenceChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 176.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (selected) "Quitar" else "Agregar")
        }
    }
}

private fun durationLabel(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Cortos"
    DurationType.MEDIUM -> "Medianos"
    DurationType.LONG -> "Largos"
}

private fun durationDescription(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Menos de 15 episodios, perfectos para maratones rápidos."
    DurationType.MEDIUM -> "Series entre 16 y 40 episodios para equilibrar historia y tiempo."
    DurationType.LONG -> "SagAs extensas, ideales si disfrutas seguir historias épicas."
}

@Preview(showBackground = true)
@Composable
private fun SettingsContentPreview() {
    AnimeDevTheme {
        SettingsContent(
            state = SettingsUiState(
                isLoading = false,
                availableGenres = FakeDataSource.genres,
                selectedGenres = FakeDataSource.preferredGenres.map { it.id }.toSet(),
                preferredDurations = setOf(DurationType.MEDIUM),
                notificationsEnabled = true,
                culturalAlertsEnabled = true,
                autoplayNextEpisode = true,
                hasCompletedOnboarding = true,
                name = "Akira Morales",
                email = "akira@animedev.io",
                nickname = "OtakuSensei"
            ),
            onGenreSelected = {},
            onDurationSelected = {},
            onSavePreferences = {},
            onSaveAccountInfo = {},
            onNameChange = {},
            onEmailChange = {},
            onNicknameChange = {}
        )
    }
}
