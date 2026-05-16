package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.canModerateTrivia
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

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
        TriviaAccessState.Loading -> TriviaLoadingState()

        TriviaAccessState.Guest -> GuestTriviaBlockedState(
            onGoToLogin = onGoToLogin,
            onGoToRegister = onGoToRegister
        )

        is TriviaAccessState.Authenticated -> {
            val showModerationButton = currentAccessState.profile.canModerateTrivia()

            when (val state = uiState) {
                is TriviaUiState.Loading -> TriviaLoadingState()

                is TriviaUiState.Error -> TriviaErrorState(
                    message = state.message,
                    onRetry = viewModel::retry
                )

                is TriviaUiState.Success -> TriviaListContent(
                    summaries = state.summaries,
                    showModerationButton = showModerationButton,
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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Trivias disponibles solo para cuentas",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Inicia sesión o regístrate para desbloquear trivias, guardar resultados y proponer preguntas para tus series favoritas.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onGoToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }

            OutlinedButton(
                onClick = onGoToRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarme")
            }
        }
    }
}

@Composable
private fun TriviaListContent(
    summaries: List<TriviaSummary>,
    showModerationButton: Boolean,
    onPlayTrivia: (Long) -> Unit,
    onAddQuestion: (Long) -> Unit,
    onOpenModeration: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) {
        TriviaEmptyState(
            modifier = modifier,
            showModerationButton = showModerationButton,
            onOpenModeration = onOpenModeration
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Trivias de tus favoritos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Juega trivias o contribuye agregando preguntas sobre cada anime.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showModerationButton) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onOpenModeration,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Panel de moderación")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        items(
            items = summaries,
            key = { summary -> summary.anime.id }
        ) { summary ->
            TriviaAnimeCard(
                summary = summary,
                onPlayTrivia = {
                    onPlayTrivia(summary.anime.id)
                },
                onAddQuestion = {
                    onAddQuestion(summary.anime.id)
                }
            )
        }
    }
}

@Composable
private fun TriviaAnimeCard(
    summary: TriviaSummary,
    onPlayTrivia: () -> Unit,
    onAddQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBeenPlayed = summary.lastScore != null

    val buttonLabel = if (hasBeenPlayed) {
        "Volver a jugar"
    } else {
        "Jugar trivia"
    }

    val bestScoreLabel = "${summary.bestScore}/${summary.totalQuestions}"

    val lastAttemptLabel = summary.lastScore?.let { score ->
        "Último intento: $score/${summary.totalQuestions}"
    } ?: "Aún no has jugado esta trivia"

    val difficultyLabel = summary.lastDifficulty?.displayName ?: "Sin intentos previos"

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = summary.anime.coverImageUrl,
                    contentDescription = "Poster de ${summary.anime.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = summary.anime.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = summary.anime.genres.joinToString { genre ->
                            genre.name
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(if (hasBeenPlayed) "Ya jugada" else "Nueva")
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text("Preguntas: ${summary.totalQuestions}")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TriviaMetricRow(
                title = "Mejor puntaje",
                value = bestScoreLabel
            )

            Spacer(modifier = Modifier.height(6.dp))

            TriviaMetricRow(
                title = "Última dificultad",
                value = difficultyLabel
            )

            Spacer(modifier = Modifier.height(6.dp))

            TriviaMetricRow(
                title = "Estado",
                value = lastAttemptLabel
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPlayTrivia,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = buttonLabel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onAddQuestion,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Agregar pregunta")
            }
        }
    }
}

@Composable
private fun TriviaMetricRow(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TriviaLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun TriviaErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun TriviaEmptyState(
    modifier: Modifier = Modifier,
    showModerationButton: Boolean = false,
    onOpenModeration: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No tienes trivias desbloqueadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Añade animes a favoritos para desbloquear sus trivias, jugar y proponer preguntas.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (showModerationButton) {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onOpenModeration,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Panel de moderación")
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
                showModerationButton = true,
                onOpenModeration = {}
            )
        }
    }
}