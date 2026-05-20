package com.example.animedev20.ui.theme.feature.trivia.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.canModerateTrivia
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevEmptyState
import com.example.animedev20.ui.theme.ux.AnimeDevErrorState
import com.example.animedev20.ui.theme.ux.AnimeDevFilterBar
import com.example.animedev20.ui.theme.ux.AnimeDevFullScreenLoading
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard

@Composable
fun TriviaScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: TriviaViewModel = viewModel(
        factory = TriviaViewModel.provideFactory(appContainer.triviaRepository)
    ),
    onPlayTrivia: (Long) -> Unit = {},
    onAddQuestion: (Long) -> Unit = {},
    onOpenModeration: () -> Unit = {},
    onGoToLogin: () -> Unit = {},
    onGoToRegister: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val accessState by produceState<TriviaAccessState>(
        initialValue = TriviaAccessState.Loading
    ) {
        value = runCatching {
            appContainer.userRepository.getUserProfile()
        }.fold(
            onSuccess = { profile ->
                if (profile.email.isBlank()) {
                    TriviaAccessState.Guest
                } else {
                    TriviaAccessState.Authenticated(profile)
                }
            },
            onFailure = {
                TriviaAccessState.Guest
            }
        )
    }

    when (val currentAccessState = accessState) {
        TriviaAccessState.Loading -> AnimeDevFullScreenLoading(
            message = "Preparando tus trivias..."
        )

        TriviaAccessState.Guest -> GuestTriviaBlockedState(
            onGoToLogin = onGoToLogin,
            onGoToRegister = onGoToRegister
        )

        is TriviaAccessState.Authenticated -> {
            val showAdminButton = currentAccessState.profile.canModerateTrivia()

            when (val state = uiState) {
                is TriviaUiState.Loading -> AnimeDevFullScreenLoading(
                    message = "Cargando trivias de tus favoritos..."
                )

                is TriviaUiState.Error -> AnimeDevErrorState(
                    title = "No pudimos cargar tus trivias",
                    message = state.message,
                    onPrimaryAction = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )

                is TriviaUiState.Success -> TriviaListContent(
                    summaries = state.summaries,
                    showAdminButton = showAdminButton,
                    onPlayTrivia = onPlayTrivia,
                    onAddQuestion = onAddQuestion,
                    onOpenModeration = onOpenModeration
                )
            }
        }
    }
}

@Composable
private fun GuestTriviaBlockedState(
    onGoToLogin: () -> Unit,
    onGoToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevEmptyState(
            title = "Crea tu cuenta para organizar tus trivias",
            message = "Como invitado puedes jugar desde la información de cada anime y enviar preguntas. Regístrate para guardar resultados, desbloquear esta sección y seguir tu progreso.",
            primaryActionLabel = AnimeDevCopy.Actions.login,
            onPrimaryAction = onGoToLogin,
            secondaryActionLabel = AnimeDevCopy.Actions.goToRegister,
            onSecondaryAction = onGoToRegister
        )
    }
}

@Composable
private fun TriviaListContent(
    summaries: List<TriviaSummary>,
    showAdminButton: Boolean,
    onPlayTrivia: (Long) -> Unit,
    onAddQuestion: (Long) -> Unit,
    onOpenModeration: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf(TriviaListFilter.ALL)
    }

    val filteredSummaries = summaries.filter { summary ->
        when (selectedFilter) {
            TriviaListFilter.ALL -> true
            TriviaListFilter.PENDING -> summary.lastScore == null
            TriviaListFilter.PLAYED -> summary.lastScore != null
        }
    }

    if (summaries.isEmpty()) {
        TriviaEmptyState(
            showAdminButton = showAdminButton,
            onOpenModeration = onOpenModeration,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TriviaHeader(
                total = summaries.size,
                played = summaries.count { it.lastScore != null },
                perfect = summaries.count {
                    it.totalQuestions > 0 && it.bestScore >= it.totalQuestions
                },
                showAdminButton = showAdminButton,
                onOpenModeration = onOpenModeration
            )
        }

        item {
            AnimeDevFilterBar(
                items = TriviaListFilter.values().toList(),
                selectedItem = selectedFilter,
                label = { it.label },
                onItemSelected = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (filteredSummaries.isEmpty()) {
            item {
                AnimeDevEmptyState(
                    title = selectedFilter.emptyTitle,
                    message = selectedFilter.emptyMessage,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(
                items = filteredSummaries,
                key = { it.anime.id }
            ) { summary ->
                TriviaAnimeCard(
                    summary = summary,
                    onPlayTrivia = { onPlayTrivia(summary.anime.id) },
                    onAddQuestion = { onAddQuestion(summary.anime.id) }
                )
            }
        }
    }
}

@Composable
private fun TriviaHeader(
    total: Int,
    played: Int,
    perfect: Int,
    showAdminButton: Boolean,
    onOpenModeration: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Trivias de tus favoritos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cada anime favorito desbloquea una trivia. Juega, mejora tu score y reporta preguntas cuando algo no cuadre.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TriviaStatCard(
                label = "Disponibles",
                value = total.toString(),
                modifier = Modifier.weight(1f)
            )

            TriviaStatCard(
                label = "Jugadas",
                value = played.toString(),
                modifier = Modifier.weight(1f)
            )

            TriviaStatCard(
                label = "Perfectas",
                value = perfect.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        if (showAdminButton) {
            OutlinedButton(
                onClick = onOpenModeration,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Panel de administración")
            }
        }

        AnimeDevInfoCard(
            title = "Tip de experiencia",
            message = "Si un anime no tiene suficientes preguntas, puedes enviar una nueva desde su tarjeta o desde la pantalla de detalle."
        )
    }
}

@Composable
private fun TriviaStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TriviaAnimeCard(
    summary: TriviaSummary,
    onPlayTrivia: () -> Unit,
    onAddQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBeenPlayed = summary.lastScore != null
    val hasQuestions = summary.totalQuestions > 0

    val buttonLabel = when {
        !hasQuestions -> "Enviar pregunta"
        hasBeenPlayed -> "Volver a jugar"
        else -> "Jugar trivia"
    }

    val bestScoreLabel = if (summary.totalQuestions > 0) {
        "${summary.bestScore}/${summary.totalQuestions}"
    } else {
        "Sin banco"
    }

    val lastAttemptLabel = summary.lastScore?.let { score ->
        "Último intento: $score/${summary.totalQuestions}"
    } ?: "Aún no has jugado esta trivia"

    val difficultyLabel = summary.lastDifficulty?.displayName ?: "Sin intentos previos"

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = summary.anime.coverImageUrl,
                    contentDescription = "Portada de ${summary.anime.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(18.dp))
                )

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = summary.anime.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = summary.anime.genres.joinToString { it.name }
                            .ifBlank { "Sin géneros registrados" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (hasBeenPlayed) "Ya jugada" else "Nueva",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (hasQuestions) {
                                "${summary.totalQuestions} preguntas"
                            } else {
                                "Sin preguntas"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TriviaMetricCard(
                    title = "Mejor score",
                    value = bestScoreLabel,
                    modifier = Modifier.weight(1f)
                )

                TriviaMetricCard(
                    title = "Dificultad",
                    value = difficultyLabel,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = lastAttemptLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!hasQuestions) {
                AnimeDevInfoCard(
                    title = "Banco pendiente",
                    message = "Este anime todavía necesita preguntas aprobadas para activar una trivia completa."
                )
            }

            Button(
                onClick = {
                    if (hasQuestions) {
                        onPlayTrivia()
                    } else {
                        onAddQuestion()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonLabel)
            }

            if (hasQuestions) {
                OutlinedButton(
                    onClick = onAddQuestion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enviar una pregunta para este anime")
                }
            }
        }
    }
}

@Composable
private fun TriviaMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TriviaEmptyState(
    showAdminButton: Boolean,
    onOpenModeration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimeDevEmptyState(
                title = "Aún no tienes trivias desbloqueadas",
                message = "Agrega animes a favoritos para desbloquear sus trivias y empezar a subir tu nivel."
            )

            if (showAdminButton) {
                OutlinedButton(
                    onClick = onOpenModeration,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Panel de administración")
                }
            }
        }
    }
}

private sealed class TriviaAccessState {
    data object Loading : TriviaAccessState()
    data object Guest : TriviaAccessState()

    data class Authenticated(
        val profile: UserProfile
    ) : TriviaAccessState()
}

private enum class TriviaListFilter(
    val label: String,
    val emptyTitle: String,
    val emptyMessage: String
) {
    ALL(
        label = "Todas",
        emptyTitle = "No hay trivias disponibles",
        emptyMessage = "Cuando agregues animes a favoritos, sus trivias aparecerán aquí."
    ),
    PENDING(
        label = "Sin jugar",
        emptyTitle = "Todo está jugado",
        emptyMessage = "Ya probaste todas tus trivias disponibles. Buen negocio, senpai."
    ),
    PLAYED(
        label = "Jugadas",
        emptyTitle = "Todavía no has jugado trivias",
        emptyMessage = "Elige una trivia y registra tu primer intento."
    )
}

@Preview(showBackground = true)
@Composable
private fun TriviaCardPreview() {
    AnimeDevTheme {
        Surface {
            TriviaAnimeCard(
                summary = TriviaSummary(
                    anime = FakeDataSource.heroAnime,
                    lastScore = 2,
                    totalQuestions = 3,
                    lastDifficulty = TriviaDifficulty.MEDIUM,
                    bestScore = 3
                ),
                onPlayTrivia = {},
                onAddQuestion = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TriviaEmptyPreview() {
    AnimeDevTheme {
        Surface {
            TriviaEmptyState(
                showAdminButton = false,
                onOpenModeration = {}
            )
        }
    }
}