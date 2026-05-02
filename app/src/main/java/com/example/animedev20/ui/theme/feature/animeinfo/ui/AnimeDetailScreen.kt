package com.example.animedev20.ui.theme.feature.animeinfo.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

@Composable
fun AnimeDetailScreen(
    animeId: Long,
    appContainer: AppContainer = DefaultAppContainer(),
    onBack: () -> Unit,
    onTriviaRequested: (Long) -> Unit = {}
) {
    val viewModel: AnimeDetailViewModel = viewModel(
        factory = AnimeDetailViewModel.provideFactory(
            animeId = animeId,
            animeRepository = appContainer.animeRepository,
            favoritesRepository = appContainer.favoritesRepository,
            interactionRepository = appContainer.interactionRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        AnimeDetailUiState.Loading -> AnimeDetailLoading()

        is AnimeDetailUiState.Error -> AnimeDetailError(
            message = state.message,
            onRetry = viewModel::loadAnimeDetail,
            onBack = onBack
        )

        is AnimeDetailUiState.Success -> AnimeDetailContent(
            detail = state.detail,
            isFavorite = state.isFavorite,
            onBack = onBack,
            onTrivia = { onTriviaRequested(state.detail.anime.id) },
            onFavoriteToggle = viewModel::toggleFavorite
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimeDetailContent(
    detail: AnimeDetail,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onTrivia: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(text = detail.anime.title)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onFavoriteToggle,
                icon = {
                    val icon = if (isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    }

                    val contentDescription = if (isFavorite) {
                        "Eliminar de favoritos"
                    } else {
                        "Agregar a favoritos"
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription
                    )
                },
                text = {
                    Text(
                        text = if (isFavorite) {
                            "En favoritos"
                        } else {
                            "Agregar a favoritos"
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            item {
                AnimeHeroSection(anime = detail.anime)
            }

            item {
                ActionButtons(
                    trailerUrl = detail.anime.trailerUrl,
                    onTrailer = { url -> uriHandler.openUri(url) },
                    onTrivia = onTrivia,
                    mangaUrl = detail.anime.mangaUrl
                        ?: detail.anime.mangaPlusUrl.takeIf { it.isNotBlank() },
                    onManga = { url -> uriHandler.openUri(url) }
                )
            }

            item {
                GenreSection(genres = detail.anime.genres)
            }

            item {
                AnimeSynopsis(
                    synopsis = detail.anime.synopsis,
                    culturalNotes = detail.culturalNotes
                )
            }

            item {
                AnimeStats(anime = detail.anime)
            }
        }
    }
}

@Composable
private fun AnimeHeroSection(anime: Anime) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        AsyncImage(
            model = anime.coverImageUrl,
            contentDescription = "Portada de ${anime.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Text(
                text = "${anime.releaseYear ?: "Próximamente"}  •  ${anime.emissionStatus.toReadableText()}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    trailerUrl: String?,
    onTrailer: (String) -> Unit,
    onTrivia: () -> Unit,
    mangaUrl: String?,
    onManga: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 24.dp
        )
    ) {
        if (!trailerUrl.isNullOrBlank()) {
            Button(
                onClick = { onTrailer(trailerUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(text = "Ver trailer oficial")
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = onTrivia,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(text = "Jugar trivia")
        }

        if (!mangaUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onManga(mangaUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(text = "Ver manga relacionado")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreSection(genres: List<Genre>) {
    if (genres.isEmpty()) {
        return
    }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.forEach { genre ->
            AssistChip(
                onClick = {},
                label = {
                    Text(text = genre.name)
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun AnimeSynopsis(
    synopsis: String,
    culturalNotes: List<String>
) {
    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        Text(
            text = "Sinopsis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = synopsis.ifBlank { "Sinopsis no disponible." },
            style = MaterialTheme.typography.bodyMedium
        )

        if (culturalNotes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Notas culturales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            culturalNotes.forEach { note ->
                Text(
                    text = "• $note",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimeStats(anime: Anime) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Detalles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        RowOfStats(
            label = "Episodios",
            value = anime.totalEpisodes?.toString() ?: "Pendiente"
        )

        RowOfStats(
            label = "Duración",
            value = anime.durationType.toReadableText()
        )

        RowOfStats(
            label = "Estado",
            value = anime.emissionStatus.toReadableText()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun RowOfStats(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun AnimeDetailLoading() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun AnimeDetailError(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text(text = "Reintentar")
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Volver")
        }
    }
}

@Preview
@Composable
private fun AnimeDetailPreview() {
    AnimeDevTheme {
        Surface {
            AnimeDetailContent(
                detail = FakeDataSource.getAnimeDetail(FakeDataSource.heroAnime.id),
                isFavorite = true,
                onBack = {},
                onTrivia = {},
                onFavoriteToggle = {}
            )
        }
    }
}

private fun DurationType.toReadableText(): String = when (this) {
    DurationType.SHORT -> "Corto (≤15 min)"
    DurationType.MEDIUM -> "Medio (16-25 min)"
    DurationType.LONG -> "Largo (30+ min)"
}

private fun EmissionStatus.toReadableText(): String = when (this) {
    EmissionStatus.ON_AIR -> "En emisión"
    EmissionStatus.FINISHED -> "Finalizado"
    EmissionStatus.ON_BREAK -> "En pausa"
}