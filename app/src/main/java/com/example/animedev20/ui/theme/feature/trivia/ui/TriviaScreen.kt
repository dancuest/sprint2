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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

@Composable
fun TriviaScreen(
    viewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory),
    onPlayTrivia: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is TriviaUiState.Loading -> TriviaLoadingState()
        is TriviaUiState.Error -> TriviaErrorState(
            message = state.message,
            onRetry = viewModel::retry
        )
        is TriviaUiState.Success -> TriviaListContent(
            summaries = state.summaries,
            onPlayTrivia = onPlayTrivia
        )
    }
}

@Composable
private fun TriviaListContent(
    summaries: List<TriviaSummary>,
    onPlayTrivia: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (summaries.isEmpty()) {
        TriviaEmptyState(modifier)
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Trivias personalizadas",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Pon a prueba tus conocimientos de cada anime y presume tu puntuación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        items(summaries, key = { it.anime.id }) { summary ->
            TriviaAnimeCard(
                summary = summary,
                onPlayTrivia = { onPlayTrivia(summary.anime.id) }
            )
        }
    }
}

@Composable
private fun TriviaAnimeCard(
    summary: TriviaSummary,
    onPlayTrivia: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = summary.anime.coverImageUrl,
                contentDescription = "Poster de ${summary.anime.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = summary.anime.synopsis ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                TriviaScoreRow(summary)
            }
            Button(onClick = onPlayTrivia) {
                Text("Jugar")
            }
        }
    }
}

@Composable
private fun TriviaScoreRow(summary: TriviaSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val lastScoreText = summary.lastScore?.let { score ->
            val difficulty = summary.lastDifficulty?.displayName ?: ""
            "Último intento: $score/${summary.totalQuestions} $difficulty"
        } ?: "Aún no has jugado"
        Text(
            text = lastScoreText,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = "Mejor puntaje: ${summary.bestScore}/${summary.totalQuestions}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TriviaLoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
        Text(text = message, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
private fun TriviaEmptyState(modifier: Modifier = Modifier) {
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
                text = "No hay trivias disponibles",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Cuando añadamos más animes verás sus retos culturales aquí",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
                onPlayTrivia = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TriviaListPreview() {
    AnimeDevTheme {
        Surface {
            TriviaListContent(
                summaries = FakeDataSource.animeCatalog.mapIndexed { index, anime ->
                    TriviaSummary(
                        anime = anime,
                        lastScore = if (index % 2 == 0) 2 else null,
                        totalQuestions = 3,
                        lastDifficulty = if (index % 2 == 0) TriviaDifficulty.MEDIUM else null,
                        bestScore = if (index % 2 == 0) 3 else 0
                    )
                },
                onPlayTrivia = {}
            )
        }
    }
}
