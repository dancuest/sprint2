package com.example.animedev20.ui.theme.feature.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

@Composable
fun OnboardingPreferencesRoute(
    onContinue: () -> Unit,
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: OnboardingPreferencesViewModel = viewModel(
        factory = OnboardingPreferencesViewModel.provideFactory(
            userRepository = appContainer.userRepository,
            animeRepository = appContainer.animeRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.completed) {
        if (uiState.completed) {
            onContinue()
        }
    }

    OnboardingPreferencesScreen(
        state = uiState,
        onGenreSelected = viewModel::onGenreSelected,
        onDurationSelected = viewModel::onDurationSelected,
        onContinue = viewModel::onContinue,
        onRetry = viewModel::retryLoading
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingPreferencesScreen(
    state: OnboardingPreferencesUiState,
    onGenreSelected: (String) -> Unit,
    onDurationSelected: (DurationType) -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                OnboardingErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { OnboardingHero() }
                    item {
                        Text(
                            text = "¿Qué categorías te emocionan?",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableGenres.forEach { genre ->
                                FilterChip(
                                    selected = state.selectedGenres.contains(genre.id),
                                    onClick = { onGenreSelected(genre.id) },
                                    label = { Text(genre.name) },
                                    leadingIcon = if (state.selectedGenres.contains(genre.id)) {
                                        {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Podrás modificar estas preferencias desde Ajustes cuando quieras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item {
                        Text(
                            text = "Elige una o más duraciones",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DurationType.values().forEach { duration ->
                                DurationPreferenceCard(
                                    durationType = duration,
                                    selected = state.preferredDurations.contains(duration),
                                    onClick = { onDurationSelected(duration) }
                                )
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (state.isSaving) {
                                LinearSavingIndicator()
                            }
                            Button(
                                onClick = onContinue,
                                enabled = state.selectedGenres.isNotEmpty() &&
                                        state.preferredDurations.isNotEmpty() &&
                                        !state.isSaving,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Continuar")
                                }
                            }
                            Text(
                                text = "Usaremos estas preferencias para recomendarte el mejor anime y crear trivias más personalizadas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingHero() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Bienvenido a la aplicacion AnimeDev",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Queremos conocerte mejor para recomendarte historias y trivias acordes a tus gustos culturales.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DurationPreferenceCard(
    durationType: DurationType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderStroke = if (selected) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = selected, onCheckedChange = { onClick() })
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = durationLabel(durationType),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = durationDescription(durationType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LinearSavingIndicator() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        Text(
            text = "Guardando tus preferencias...",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun OnboardingErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )
        OutlinedButton(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

private fun durationLabel(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Series cortas"
    DurationType.MEDIUM -> "Series medianas"
    DurationType.LONG -> "Series largas"
}

private fun durationDescription(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Hasta 13 episodios. Perfectas para introducirte en nuevas historias."
    DurationType.MEDIUM -> "Entre 14 y 40 episodios. Equilibrio ideal entre historia y tiempo."
    DurationType.LONG -> "Más de 40 episodios para sagas épicas y detalladas."
}

@Preview(showBackground = true)
@Composable
private fun OnboardingPreferencesPreview() {
    AnimeDevTheme {
        OnboardingPreferencesScreen(
            state = OnboardingPreferencesUiState(
                isLoading = false,
                availableGenres = FakeDataSource.genres,
                selectedGenres = emptySet(),
                preferredDurations = emptySet()
            ),
            onGenreSelected = {},
            onDurationSelected = {},
            onContinue = {},
            onRetry = {}
        )
    }
}
