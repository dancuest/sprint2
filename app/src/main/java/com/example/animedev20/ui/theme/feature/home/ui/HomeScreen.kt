package com.example.animedev20.ui.theme.feature.home.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.ux.AnimeDevEmptyState
import com.example.animedev20.ui.theme.ux.AnimeDevErrorState
import com.example.animedev20.ui.theme.ux.AnimeDevFullScreenLoading
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard

@Composable
fun HomeScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            appContainer.animeRepository,
            appContainer.userRepository,
            appContainer.homeRefreshBus
        )
    ),
    onAnimeSelected: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        HomeUiState.Loading -> AnimeDevFullScreenLoading(
            message = "Preparando recomendaciones para ti..."
        )

        is HomeUiState.Error -> AnimeDevErrorState(
            title = "No pudimos cargar el inicio",
            message = state.message,
            onPrimaryAction = viewModel::loadHomeContent,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        )

        is HomeUiState.Success -> HomeSuccessContent(
            homeContent = state.homeContent,
            selectedGenreId = state.selectedGenreId,
            onToggleGenre = viewModel::toggleGenreFilter,
            onAnimeSelected = onAnimeSelected
        )
    }
}

@Composable
private fun HomeSuccessContent(
    homeContent: HomeContent,
    selectedGenreId: String?,
    onToggleGenre: (String) -> Unit,
    onAnimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseSections = homeContent.sections.filter { section ->
        section.animes.isNotEmpty() || section.genre.id == RECOMMENDATIONS_GENRE_ID
    }

    val visibleSections = if (selectedGenreId == null) {
        baseSections
    } else {
        baseSections.filter { section ->
            section.genre.id == RECOMMENDATIONS_GENRE_ID || section.genre.id == selectedGenreId
        }
    }

    val selectedGenreName = homeContent.preferredGenres
        .firstOrNull { it.id == selectedGenreId }
        ?.name

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            HomeHeader(
                selectedGenreName = selectedGenreName
            )
        }

        item {
            HeroRecommendation(
                anime = homeContent.heroAnime,
                onAnimeSelected = onAnimeSelected
            )
        }

        item {
            PreferredGenresSection(
                preferredGenres = homeContent.preferredGenres,
                selectedGenreId = selectedGenreId,
                onToggleGenre = onToggleGenre
            )
        }

        if (visibleSections.isEmpty()) {
            item {
                AnimeDevEmptyState(
                    title = "No encontramos recomendaciones para este filtro",
                    message = "Prueba otro género o actualiza tus preferencias para descubrir más animes.",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(
                items = visibleSections,
                key = { it.genre.id }
            ) { section ->
                AnimeSectionRow(
                    section = section,
                    onAnimeSelected = onAnimeSelected
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    selectedGenreName: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Inicio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (selectedGenreName == null) {
                "Recomendaciones basadas en tus gustos, favoritos e historial dentro de AnimeDev."
            } else {
                "Estás viendo recomendaciones relacionadas con $selectedGenreName."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeroRecommendation(
    anime: Anime,
    onAnimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(320.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = {
            onAnimeSelected(anime.id)
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = anime.coverImageUrl,
                contentDescription = "Imagen destacada del anime ${anime.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.78f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(16.dp)
                                .clearAndSetSemantics { }
                        )

                        Text(
                            text = "Recomendado para ti",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = anime.synopsis.ifBlank {
                        "Aún no tenemos sinopsis disponible para este anime."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .clearAndSetSemantics { }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Ver información",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PreferredGenresSection(
    preferredGenres: List<Genre>,
    selectedGenreId: String?,
    onToggleGenre: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (preferredGenres.isEmpty()) {
        AnimeDevInfoCard(
            title = "Personaliza tus recomendaciones",
            message = "Cuando elijas géneros en tus preferencias, aparecerán aquí como filtros rápidos.",
            modifier = modifier.padding(horizontal = 16.dp)
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (selectedGenreId == null) {
                "Tus géneros favoritos"
            } else {
                "Filtro activo"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Elige un género para enfocar las recomendaciones. Tócalo de nuevo para ver todo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            preferredGenres.forEach { genre ->
                val isSelected = selectedGenreId == genre.id

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onToggleGenre(genre.id)
                    },
                    label = {
                        Text(genre.name)
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clearAndSetSemantics { }
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
}

@Composable
private fun AnimeSectionRow(
    section: AnimeSection,
    onAnimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = section.genre.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = section.subtitle(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (section.animes.isEmpty()) {
            AnimeDevInfoCard(
                title = "Sin recomendaciones por ahora",
                message = "Seguiremos buscando opciones para esta categoría.",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = section.animes,
                    key = { it.id }
                ) { anime ->
                    AnimeCard(
                        anime = anime,
                        onAnimeSelected = onAnimeSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimeCard(
    anime: Anime,
    onAnimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(166.dp)
            .height(274.dp),
        shape = MaterialTheme.shapes.large,
        onClick = {
            onAnimeSelected(anime.id)
        }
    ) {
        Column {
            Box {
                AsyncImage(
                    model = anime.coverImageUrl,
                    contentDescription = "Carátula del anime ${anime.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(188.dp)
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = anime.releaseYear?.toString() ?: "S/A",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = anime.genres.joinToString { it.name }
                        .ifBlank { "Sin géneros registrados" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(14.dp)
                            .clearAndSetSemantics { }
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Ver detalle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun AnimeSection.subtitle(): String {
    return when {
        genre.id == RECOMMENDATIONS_GENRE_ID -> {
            "Selección principal basada en tu actividad."
        }

        animes.size == 1 -> {
            "1 anime relacionado con este género."
        }

        else -> {
            "${animes.size} animes relacionados con este género."
        }
    }
}

private const val RECOMMENDATIONS_GENRE_ID = "recommendations"

@Preview(showBackground = true)
@Composable
private fun HomeSuccessPreview() {
    AnimeDevTheme {
        Surface {
            HomeSuccessContent(
                homeContent = HomeContent(
                    heroAnime = FakeDataSource.heroAnime,
                    preferredGenres = FakeDataSource.preferredGenres,
                    sections = FakeDataSource.buildSectionsForGenres(FakeDataSource.preferredGenres)
                ),
                selectedGenreId = null,
                onToggleGenre = {},
                onAnimeSelected = {}
            )
        }
    }
}