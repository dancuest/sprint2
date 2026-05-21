package com.example.animedev20.ui.theme.feature.onboarding.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.CodedOption
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserDemographicCatalog
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.ux.AnimeDevErrorState
import com.example.animedev20.ui.theme.ux.AnimeDevFullScreenLoading
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard

@Composable
fun OnboardingPreferencesRoute(
    onContinue: () -> Unit,
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: OnboardingPreferencesViewModel = viewModel(
        factory = OnboardingPreferencesViewModel.provideFactory(
            userRepository = appContainer.userRepository,
            animeRepository = appContainer.animeRepository,
            homeRefreshBus = appContainer.homeRefreshBus
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
        onAgeRangeSelected = viewModel::onAgeRangeSelected,
        onGenderSelected = viewModel::onGenderSelected,
        onRegionSelected = viewModel::onRegionSelected,
        onContinue = viewModel::onContinue,
        onRetry = viewModel::retryLoading
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPreferencesScreen(
    state: OnboardingPreferencesUiState,
    onGenreSelected: (String) -> Unit,
    onDurationSelected: (DurationType) -> Unit,
    onAgeRangeSelected: (Int) -> Unit,
    onGenderSelected: (Int) -> Unit,
    onRegionSelected: (Int) -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { padding ->
        when {
            state.isLoading -> {
                AnimeDevFullScreenLoading(
                    message = "Preparando tus preferencias...",
                    modifier = modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }

            state.errorMessage != null -> {
                OnboardingErrorState(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = modifier.padding(padding)
                )
            }

            else -> {
                val canContinue = state.selectedGenres.isNotEmpty() &&
                        state.preferredDurations.isNotEmpty() &&
                        !state.isSaving

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    item {
                        OnboardingHero()
                    }

                    item {
                        AnimeDevInfoCard(
                            title = "Paso 1 de 3",
                            message = "Elige tus géneros favoritos. Esto ayuda a construir recomendaciones más relevantes desde el inicio."
                        )
                    }

                    item {
                        GenrePreferenceSection(
                            availableGenres = state.availableGenres,
                            selectedGenres = state.selectedGenres,
                            onGenreSelected = onGenreSelected
                        )
                    }

                    item {
                        AnimeDevInfoCard(
                            title = "Paso 2 de 3",
                            message = "Selecciona qué tipo de duración prefieres para tus series."
                        )
                    }

                    item {
                        DurationPreferenceSection(
                            selectedDurations = state.preferredDurations,
                            onDurationSelected = onDurationSelected
                        )
                    }

                    item {
                        AnimeDevInfoCard(
                            title = "Paso 3 de 3",
                            message = "Estos datos son opcionales. Puedes dejarlos sin especificar y cambiarlos luego en Ajustes."
                        )
                    }

                    item {
                        DemographicSection(
                            ageRange = state.ageRange,
                            genderCode = state.genderCode,
                            regionCode = state.regionCode,
                            onAgeRangeSelected = onAgeRangeSelected,
                            onGenderSelected = onGenderSelected,
                            onRegionSelected = onRegionSelected
                        )
                    }

                    item {
                        ContinueSection(
                            selectedGenresCount = state.selectedGenres.size,
                            selectedDurationsCount = state.preferredDurations.size,
                            isSaving = state.isSaving,
                            canContinue = canContinue,
                            onContinue = onContinue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingHero() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Bienvenido a AnimeDev",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Configura tus gustos para que el Home, las recomendaciones y las trivias se adapten mejor a tu perfil.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun GenrePreferenceSection(
    availableGenres: List<Genre>,
    selectedGenres: Set<String>,
    onGenreSelected: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "¿Qué géneros te interesan?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Selecciona uno o más. Puedes tocar un género otra vez para quitarlo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (availableGenres.isEmpty()) {
            AnimeDevInfoCard(
                title = "No hay géneros disponibles",
                message = "Intenta recargar la pantalla para volver a consultar las opciones."
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableGenres.forEach { genre ->
                    val isSelected = selectedGenres.contains(genre.id)

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            onGenreSelected(genre.id)
                        },
                        label = {
                            Text(genre.name)
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null
                                )
                            }
                        } else {
                            null
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        Text(
            text = "Géneros seleccionados: ${selectedGenres.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DurationPreferenceSection(
    selectedDurations: Set<DurationType>,
    onDurationSelected: (DurationType) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "¿Qué duración prefieres?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Puedes seleccionar una o varias opciones.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DurationType.values().forEach { duration ->
            DurationPreferenceCard(
                durationType = duration,
                selected = selectedDurations.contains(duration),
                onClick = {
                    onDurationSelected(duration)
                }
            )
        }

        Text(
            text = "Duraciones seleccionadas: ${selectedDurations.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DurationPreferenceCard(
    durationType: DurationType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderStroke = if (selected) {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        null
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = borderStroke,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    onClick()
                }
            )

            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = durationLabel(durationType),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = durationDescription(durationType),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DemographicSection(
    ageRange: Int,
    genderCode: Int,
    regionCode: Int,
    onAgeRangeSelected: (Int) -> Unit,
    onGenderSelected: (Int) -> Unit,
    onRegionSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Información opcional",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        DemographicSelector(
            title = "Rango de edad",
            options = UserDemographicCatalog.ageRanges,
            selectedCode = ageRange,
            onSelected = onAgeRangeSelected
        )

        DemographicSelector(
            title = "Sexo / género",
            options = UserDemographicCatalog.genders,
            selectedCode = genderCode,
            onSelected = onGenderSelected
        )

        DemographicSelector(
            title = "Región",
            options = UserDemographicCatalog.regions,
            selectedCode = regionCode,
            onSelected = onRegionSelected
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DemographicSelector(
    title: String,
    options: List<CodedOption>,
    selectedCode: Int,
    onSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedCode == option.code,
                    onClick = {
                        onSelected(option.code)
                    },
                    label = {
                        Text(option.label)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun ContinueSection(
    selectedGenresCount: Int,
    selectedDurationsCount: Int,
    isSaving: Boolean,
    canContinue: Boolean,
    onContinue: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isSaving) {
            LinearSavingIndicator()
        }

        if (selectedGenresCount == 0 || selectedDurationsCount == 0) {
            AnimeDevInfoCard(
                title = "Falta completar lo básico",
                message = "Selecciona al menos un género y una duración para continuar."
            )
        }

        Button(
            onClick = onContinue,
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar y continuar")
            }
        }

        Text(
            text = "Podrás cambiar estas preferencias en Ajustes cuando quieras.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LinearSavingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
        )

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevErrorState(
            title = "No pudimos cargar tus preferencias",
            message = message,
            primaryActionLabel = "Reintentar",
            onPrimaryAction = onRetry
        )
    }
}

private fun durationLabel(
    durationType: DurationType
): String {
    return when (durationType) {
        DurationType.SHORT -> "Series cortas"
        DurationType.MEDIUM -> "Series medianas"
        DurationType.LONG -> "Series largas"
    }
}

private fun durationDescription(
    durationType: DurationType
): String {
    return when (durationType) {
        DurationType.SHORT -> "Hasta 13 episodios. Ideales para empezar rápido o probar nuevos géneros."
        DurationType.MEDIUM -> "Entre 14 y 40 episodios. Buen equilibrio entre desarrollo e inversión de tiempo."
        DurationType.LONG -> "Más de 40 episodios. Pensadas para sagas extensas, arcos largos y maratones."
    }
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
                preferredDurations = emptySet(),
                ageRange = 0,
                genderCode = 0,
                regionCode = 0
            ),
            onGenreSelected = {},
            onDurationSelected = {},
            onAgeRangeSelected = {},
            onGenderSelected = {},
            onRegionSelected = {},
            onContinue = {},
            onRetry = {}
        )
    }
}