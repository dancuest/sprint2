package com.example.animedev20.ui.theme.feature.favorites.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevEmptyState
import com.example.animedev20.ui.theme.ux.AnimeDevFilterBar
import com.example.animedev20.ui.theme.ux.AnimeDevFullScreenLoading
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard

@Composable
fun FavoritesScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModel.provideFactory(appContainer.favoritesRepository)
    ),
    onAnimeSelected: (Long) -> Unit = {},
    onGoToLogin: () -> Unit = {},
    onGoToRegister: () -> Unit = {}
) {
    val favorites by viewModel.favorites.collectAsState()

    val isGuestMode by produceState<Boolean?>(initialValue = null) {
        value = runCatching {
            appContainer.userRepository.getUserProfile().email.isBlank()
        }.getOrDefault(true)
    }

    when (isGuestMode) {
        null -> AnimeDevFullScreenLoading(
            message = "Preparando tus favoritos..."
        )

        true -> GuestFavoritesBlockedState(
            onGoToLogin = onGoToLogin,
            onGoToRegister = onGoToRegister
        )

        false -> FavoritesContent(
            favorites = favorites,
            onAnimeSelected = onAnimeSelected,
            onRemoveFavorite = viewModel::removeFavorite
        )
    }
}

@Composable
private fun GuestFavoritesBlockedState(
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
            title = "Guarda favoritos con una cuenta",
            message = "Inicia sesión o crea una cuenta para guardar animes, organizar tu lista y recibir mejores recomendaciones.",
            primaryActionLabel = AnimeDevCopy.Actions.login,
            onPrimaryAction = onGoToLogin,
            secondaryActionLabel = AnimeDevCopy.Actions.goToRegister,
            onSecondaryAction = onGoToRegister
        )
    }
}

@Composable
private fun FavoritesContent(
    favorites: List<Anime>,
    onAnimeSelected: (Long) -> Unit,
    onRemoveFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf(FavoriteFilter.ALL)
    }

    var pendingRemovalAnimeId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val filteredFavorites = favorites.filter { anime ->
        when (selectedFilter) {
            FavoriteFilter.ALL -> true
            FavoriteFilter.ON_AIR -> anime.emissionStatus == EmissionStatus.ON_AIR
            FavoriteFilter.FINISHED -> anime.emissionStatus == EmissionStatus.FINISHED
            FavoriteFilter.WITH_MANGA -> !anime.mangaUrl.isNullOrBlank() || anime.mangaPlusUrl.isNotBlank()
        }
    }

    val pendingRemovalAnime = favorites.firstOrNull { anime ->
        anime.id == pendingRemovalAnimeId
    }

    pendingRemovalAnime?.let { anime ->
        RemoveFavoriteDialog(
            animeTitle = anime.title,
            onConfirm = {
                onRemoveFavorite(anime.id)
                pendingRemovalAnimeId = null
            },
            onDismiss = {
                pendingRemovalAnimeId = null
            }
        )
    }

    if (favorites.isEmpty()) {
        EmptyFavoritesState(modifier = modifier)
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
            FavoritesHeader(
                total = favorites.size,
                onAir = favorites.count { it.emissionStatus == EmissionStatus.ON_AIR },
                finished = favorites.count { it.emissionStatus == EmissionStatus.FINISHED }
            )
        }

        item {
            AnimeDevFilterBar(
                items = FavoriteFilter.values().toList(),
                selectedItem = selectedFilter,
                label = { it.label },
                onItemSelected = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (filteredFavorites.isEmpty()) {
            item {
                AnimeDevEmptyState(
                    title = selectedFilter.emptyTitle,
                    message = selectedFilter.emptyMessage,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(
                items = filteredFavorites,
                key = { it.id }
            ) { anime ->
                FavoriteAnimeCard(
                    anime = anime,
                    onAnimeSelected = { onAnimeSelected(anime.id) },
                    onRequestRemoveFavorite = { pendingRemovalAnimeId = anime.id }
                )
            }
        }
    }
}

@Composable
private fun FavoritesHeader(
    total: Int,
    onAir: Int,
    finished: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Tus animes favoritos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Aquí vive tu lista personal. Úsala para volver rápido a tus animes, desbloquear trivias y mejorar recomendaciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FavoriteStatCard(
                label = "Guardados",
                value = total.toString(),
                modifier = Modifier.weight(1f)
            )

            FavoriteStatCard(
                label = "En emisión",
                value = onAir.toString(),
                modifier = Modifier.weight(1f)
            )

            FavoriteStatCard(
                label = "Finalizados",
                value = finished.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        AnimeDevInfoCard(
            title = "Tip otaku",
            message = "Agregar favoritos ayuda a AnimeDev a mostrarte trivias y recomendaciones más cercanas a tus gustos."
        )
    }
}

@Composable
private fun FavoriteStatCard(
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

@Composable
private fun EmptyFavoritesState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevEmptyState(
            title = "Todavía no tienes favoritos",
            message = "Explora animes y toca el corazón para guardarlos aquí. Así podrás encontrarlos rápido y desbloquear más experiencia personalizada."
        )
    }
}

@Composable
private fun FavoriteAnimeCard(
    anime: Anime,
    onAnimeSelected: () -> Unit,
    onRequestRemoveFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    model = anime.coverImageUrl,
                    contentDescription = "Portada del anime ${anime.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 104.dp, height = 142.dp)
                        .clip(RoundedCornerShape(18.dp))
                )

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .clearAndSetSemantics { }
                        )

                        Text(
                            text = "Favorito",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = anime.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = anime.genres.joinToString { it.name }
                            .ifBlank { "Sin géneros registrados" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(anime.releaseYear?.toString() ?: "Sin año")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        AssistChip(
                            onClick = {},
                            label = {
                                Text(anime.emissionStatus.toDisplayLabel())
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Text(
                text = anime.synopsis.ifBlank { "Sin sinopsis disponible." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            anime.totalEpisodes?.let { episodes ->
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        text = "Episodios: $episodes",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onAnimeSelected,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver información del anime")
            }

            OutlinedButton(
                onClick = onRequestRemoveFavorite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .clearAndSetSemantics { }
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text("Quitar de favoritos")
            }
        }
    }
}

@Composable
private fun RemoveFavoriteDialog(
    animeTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("¿Quitar de favoritos?")
        },
        text = {
            Text(
                text = "Vas a quitar \"$animeTitle\" de tu lista. Podrás agregarlo de nuevo cuando quieras."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Sí, quitar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mantener favorito")
            }
        }
    )
}

private fun EmissionStatus.toDisplayLabel(): String {
    return when (this) {
        EmissionStatus.ON_AIR -> "En emisión"
        EmissionStatus.FINISHED -> "Finalizado"
        EmissionStatus.ON_BREAK -> "En pausa"
    }
}

private enum class FavoriteFilter(
    val label: String,
    val emptyTitle: String,
    val emptyMessage: String
) {
    ALL(
        label = "Todos",
        emptyTitle = "No hay favoritos",
        emptyMessage = "Cuando guardes animes, aparecerán aquí."
    ),
    ON_AIR(
        label = "En emisión",
        emptyTitle = "No tienes favoritos en emisión",
        emptyMessage = "Cuando agregues animes que estén saliendo actualmente, aparecerán en este filtro."
    ),
    FINISHED(
        label = "Finalizados",
        emptyTitle = "No tienes favoritos finalizados",
        emptyMessage = "Tus animes favoritos finalizados aparecerán aquí."
    ),
    WITH_MANGA(
        label = "Con manga",
        emptyTitle = "No tienes favoritos con manga",
        emptyMessage = "Cuando un favorito tenga enlace de manga disponible, aparecerá en este filtro."
    )
}

@Preview(showBackground = true)
@Composable
private fun FavoritesPreview() {
    AnimeDevTheme {
        Surface {
            FavoritesContent(
                favorites = FakeDataSource.recentAnimeHistory,
                onAnimeSelected = {},
                onRemoveFavorite = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyFavoritesPreview() {
    AnimeDevTheme {
        Surface {
            EmptyFavoritesState()
        }
    }
}