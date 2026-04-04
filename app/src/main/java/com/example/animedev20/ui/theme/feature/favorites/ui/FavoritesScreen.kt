package com.example.animedev20.ui.theme.feature.favorites.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

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
        null -> Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

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
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Favoritos disponibles solo para cuentas",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Inicia sesión o regístrate para guardar animes, organizarlos y personalizar tus recomendaciones.",
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
private fun FavoritesContent(
    favorites: List<Anime>,
    onAnimeSelected: (Long) -> Unit,
    onRemoveFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (favorites.isEmpty()) {
        EmptyFavoritesState(modifier)
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Tu lista de favoritos",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(favorites, key = { it.id }) { anime ->
            FavoriteAnimeCard(
                anime = anime,
                onAnimeSelected = onAnimeSelected,
                onRemoveFavorite = onRemoveFavorite
            )
        }
    }
}

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Todavía no tienes favoritos guardados.",
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FavoriteAnimeCard(
    anime: Anime,
    onAnimeSelected: (Long) -> Unit,
    onRemoveFavorite: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = anime.coverImageUrl,
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = anime.genres.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onAnimeSelected(anime.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver detalle")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onRemoveFavorite(anime.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Quitar de favoritos")
            }
        }
    }
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