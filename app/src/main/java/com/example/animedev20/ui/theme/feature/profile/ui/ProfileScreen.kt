package com.example.animedev20.ui.theme.feature.profile.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

@Composable
fun ProfileScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onGoToLogin: () -> Unit = {},
    onGoToRegister: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(
            userRepository = appContainer.userRepository,
            favoritesRepository = appContainer.favoritesRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.profile != null && uiState.profile!!.email.isBlank() -> {
            GuestProfileBlockedState(
                onGoToLogin = onGoToLogin,
                onGoToRegister = onGoToRegister
            )
        }

        uiState.profile != null -> ProfileContent(
            profile = uiState.profile!!,
            favoriteAnimes = uiState.favoriteAnimes,
            fanLevel = uiState.fanLevel,
            triviaPlayedCount = uiState.triviaPlayedCount
        )

        uiState.isLoading -> ProfileLoadingState()

        uiState.errorMessage != null -> ProfileErrorState(
            message = uiState.errorMessage,
            onRetry = viewModel::refresh
        )

        else -> ProfileEmptyState(onRetry = viewModel::refresh)
    }
}

@Composable
private fun ProfileLoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ProfileErrorState(
    message: String?,
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
            text = message ?: "Ocurrió un error al cargar tu perfil",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Reintentar")
        }
    }
}

@Composable
private fun ProfileEmptyState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Todavía no hay información suficiente para mostrar tu perfil.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onRetry) {
            Text(text = "Volver a cargar")
        }
    }
}

@Composable
private fun GuestProfileBlockedState(
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
                text = "Perfil bloqueado para invitados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Para configurar tu foto, portada y datos del perfil, primero inicia sesión o crea una cuenta.",
                style = MaterialTheme.typography.bodyMedium,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileContent(
    profile: UserProfile,
    favoriteAnimes: List<Anime>,
    fanLevel: String,
    triviaPlayedCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7FB)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            ProfileHeader(
                profile = profile,
                fanLevel = fanLevel,
                favoriteCount = favoriteAnimes.size
            )
        }

        item {
            ProfileStatsRow(
                fanLevel = fanLevel,
                triviaPlayedCount = triviaPlayedCount
            )
        }

        item {
            SectionTitle(title = "Géneros que me gustan")

            if (profile.favoriteGenres.isEmpty()) {
                EmptySectionMessage(message = "Aún no has configurado géneros favoritos.")
            } else {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.favoriteGenres.forEach { genre ->
                        AssistChip(
                            onClick = {},
                            label = { Text(genre.name) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFE9E6FF)
                            )
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(title = "Me gustan las historias:")

            if (profile.preferredDurations.isEmpty()) {
                EmptySectionMessage(message = "Aún no has configurado una preferencia de duración.")
            } else {
                FlowRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.preferredDurations.forEach { duration ->
                        AssistChip(
                            onClick = {},
                            label = { Text(durationPreferenceLabel(duration)) }
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(title = "Animes favoritos")

            if (favoriteAnimes.isEmpty()) {
                EmptySectionMessage(message = "Todavía no tienes animes en favoritos.")
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteAnimes, key = { it.id }) { anime ->
                        FavoriteAnimeCard(anime = anime)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    fanLevel: String,
    favoriteCount: Int
) {
    val displayName = profile.name.ifBlank {
        profile.nickname.ifBlank { "Invitado" }
    }
    val nickname = profile.nickname.ifBlank { "animefan" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
        ) {
            CoverImage(
                coverImageUrl = profile.coverImageUrl,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.14f))
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarImage(
                avatarUrl = profile.avatarUrl,
                fallbackText = displayName.firstOrNull()?.uppercase() ?: "A"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayName,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "@$nickname",
                color = Color(0xFF6B6B7A)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = fanLevel,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A3AFF)
            )

            Text(
                text = "$favoriteCount favoritos sincronizados",
                color = Color(0xFF6B6B7A)
            )
        }
    }
}

@Composable
private fun CoverImage(
    coverImageUrl: String,
    modifier: Modifier = Modifier
) {
    val decodedBitmap = remember(coverImageUrl) {
        decodeImageBitmapFromDataUrl(coverImageUrl)
    }

    when {
        decodedBitmap != null -> Image(
            bitmap = decodedBitmap,
            contentDescription = "Portada del perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )

        coverImageUrl.isNotBlank() -> AsyncImage(
            model = coverImageUrl,
            contentDescription = "Portada del perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )

        else -> Box(
            modifier = modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6C63FF),
                        Color(0xFF9D7CFF),
                        Color(0xFFFF7EB6)
                    )
                )
            )
        )
    }
}

@Composable
private fun AvatarImage(
    avatarUrl: String,
    fallbackText: String
) {
    val decodedBitmap = remember(avatarUrl) {
        decodeImageBitmapFromDataUrl(avatarUrl)
    }

    Box(
        modifier = Modifier
            .size(112.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when {
            decodedBitmap != null -> Image(
                bitmap = decodedBitmap,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            avatarUrl.isNotBlank() -> AsyncImage(
                model = avatarUrl,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            else -> Text(
                text = fallbackText,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3AFF)
            )
        }
    }
}

@Composable
private fun ProfileStatsRow(
    fanLevel: String,
    triviaPlayedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ProfileStatCard(
                title = "Nivel fan",
                value = fanLevel
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            ProfileStatCard(
                title = "Trivias jugadas",
                value = triviaPlayedCount.toString()
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                textAlign = TextAlign.Center,
                color = Color(0xFF6B6B7A)
            )
        }
    }
}

@Composable
private fun FavoriteAnimeCard(anime: Anime) {
    Card(
        modifier = Modifier.size(width = 160.dp, height = 220.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        AsyncImage(
            model = anime.coverImageUrl,
            contentDescription = anime.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = anime.title,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = anime.genres.joinToString { it.name },
                color = Color(0xFF6B6B7A),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptySectionMessage(message: String) {
    Text(
        text = message,
        color = Color(0xFF6B6B7A),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

private fun durationPreferenceLabel(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Cortas"
    DurationType.MEDIUM -> "Medianas"
    DurationType.LONG -> "Largas"
}

private fun decodeImageBitmapFromDataUrl(dataUrl: String?): ImageBitmap? {
    if (dataUrl.isNullOrBlank()) return null
    if (!dataUrl.startsWith("data:image")) return null

    return try {
        val base64Part = dataUrl.substringAfter("base64,", "")
        if (base64Part.isBlank()) return null

        val bytes = Base64.decode(base64Part, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    AnimeDevTheme {
        Surface {
            ProfileContent(
                profile = FakeDataSource.defaultUserProfile,
                favoriteAnimes = FakeDataSource.recentAnimeHistory,
                fanLevel = "Muy fan del anime",
                triviaPlayedCount = 8
            )
        }
    }
}