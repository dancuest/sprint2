package com.example.animedev20.ui.theme.feature.profile.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevEmptyState
import com.example.animedev20.ui.theme.ux.AnimeDevErrorState
import com.example.animedev20.ui.theme.ux.AnimeDevFullScreenLoading
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard

@Composable
fun ProfileScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onGoToLogin: () -> Unit = {},
    onGoToRegister: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(
            userRepository = appContainer.userRepository,
            favoritesRepository = appContainer.favoritesRepository,
            homeRefreshBus = appContainer.homeRefreshBus
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

        uiState.isLoading -> AnimeDevFullScreenLoading(
            message = "Cargando tu perfil..."
        )

        uiState.errorMessage != null -> ProfileErrorState(
            message = uiState.errorMessage,
            onRetry = viewModel::refresh
        )

        else -> ProfileEmptyState(
            onRetry = viewModel::refresh
        )
    }
}

@Composable
private fun ProfileErrorState(
    message: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevErrorState(
            title = "No pudimos cargar tu perfil",
            message = message ?: "Ocurrió un problema al cargar la información de tu cuenta.",
            primaryActionLabel = AnimeDevCopy.Actions.tryAgain,
            onPrimaryAction = onRetry
        )
    }
}

@Composable
private fun ProfileEmptyState(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevEmptyState(
            title = "Perfil sin información",
            message = "Todavía no tenemos datos suficientes para mostrar tu perfil. Intenta volver a cargar.",
            primaryActionLabel = "Volver a cargar",
            onPrimaryAction = onRetry
        )
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
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimeDevEmptyState(
            title = "Perfil disponible para cuentas",
            message = "Inicia sesión o crea una cuenta para guardar tu foto, portada, favoritos, géneros y progreso otaku.",
            primaryActionLabel = AnimeDevCopy.Actions.login,
            onPrimaryAction = onGoToLogin,
            secondaryActionLabel = AnimeDevCopy.Actions.goToRegister,
            onSecondaryAction = onGoToRegister
        )
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
    var showFanLevelInfo by rememberSaveable {
        mutableStateOf(false)
    }

    if (showFanLevelInfo) {
        FanLevelInfoDialog(
            onDismiss = {
                showFanLevelInfo = false
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            ProfileHeader(profile = profile)
        }

        item {
            ProfileStatsRow(
                fanLevel = fanLevel,
                favoriteCount = favoriteAnimes.size,
                triviaPlayedCount = triviaPlayedCount,
                onFanLevelInfoClick = {
                    showFanLevelInfo = true
                }
            )
        }

        item {
            ProfileProgressInfo(
                fanLevel = fanLevel,
                favoriteCount = favoriteAnimes.size,
                triviaPlayedCount = triviaPlayedCount
            )
        }

        item {
            SectionTitle(title = "Géneros que me gustan")

            if (profile.favoriteGenres.isEmpty()) {
                EmptySectionMessage(
                    message = "Aún no has configurado géneros favoritos."
                )
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
                            label = {
                                Text(genre.name)
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(title = "Formato de historias preferido")

            if (profile.preferredDurations.isEmpty()) {
                EmptySectionMessage(
                    message = "Aún no has configurado una preferencia de duración."
                )
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
                            label = {
                                Text(durationPreferenceLabel(duration))
                            }
                        )
                    }
                }
            }
        }

        item {
            SectionTitle(title = "Animes favoritos")

            if (favoriteAnimes.isEmpty()) {
                EmptySectionMessage(
                    message = "Todavía no tienes animes en favoritos."
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = favoriteAnimes,
                        key = { it.id }
                    ) { anime ->
                        FavoriteAnimeCard(anime = anime)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileProgressInfo(
    fanLevel: String,
    favoriteCount: Int,
    triviaPlayedCount: Int
) {
    val nextGoal = when (fanLevel) {
        "Top Global" -> "Ya alcanzaste el nivel más alto. Sigue jugando para mantener tu perfil activo."
        "OtakuPro" -> "Siguiente meta: 12 favoritos y 15 trivias jugadas para llegar a Top Global."
        "Aprendiz" -> "Siguiente meta: 8 favoritos y 10 trivias jugadas para llegar a OtakuPro."
        else -> "Siguiente meta: 5 favoritos y 7 trivias jugadas para llegar a Aprendiz."
    }

    AnimeDevInfoCard(
        title = "Progreso del perfil",
        message = "Tienes $favoriteCount animes favoritos y $triviaPlayedCount trivias jugadas. $nextGoal",
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun FanLevelInfoDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar información"
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "¿Cómo funcionan los niveles?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Los niveles se calculan según la cantidad de animes en favoritos y trivias jugadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FanLevelRule(
                        title = "Novato",
                        description = "Nivel base. Referencia: 3 animes en favoritos y 5 trivias jugadas."
                    )

                    FanLevelRule(
                        title = "Aprendiz",
                        description = "Se alcanza con 5 animes en favoritos y 7 trivias jugadas."
                    )

                    FanLevelRule(
                        title = "OtakuPro",
                        description = "Se alcanza con 8 animes en favoritos y 10 trivias jugadas."
                    )

                    FanLevelRule(
                        title = "Top Global",
                        description = "Se alcanza con 12 animes en favoritos y 15 trivias jugadas."
                    )
                }
            }
        }
    }
}

@Composable
private fun FanLevelRule(
    title: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile
) {
    val displayName = profile.name.ifBlank {
        profile.nickname.ifBlank {
            "Usuario AnimeDev"
        }
    }

    val nickname = profile.nickname.ifBlank {
        "animefan"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .background(MaterialTheme.colorScheme.surface)
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
                    .background(Color.Black.copy(alpha = 0.16f))
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarImage(
                avatarUrl = profile.avatarUrl,
                fallbackText = displayName.firstOrNull()?.uppercase() ?: "A"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = "@$nickname",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            .background(MaterialTheme.colorScheme.surface),
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
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProfileStatsRow(
    fanLevel: String,
    favoriteCount: Int,
    triviaPlayedCount: Int,
    onFanLevelInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            ProfileStatCard(
                title = "Nivel de fan",
                value = fanLevel,
                onInfoClick = onFanLevelInfoClick
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            ProfileStatCard(
                title = "Favoritos",
                value = favoriteCount.toString()
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            ProfileStatCard(
                title = "Trivias",
                value = triviaPlayedCount.toString()
            )
        }
    }
}

@Composable
private fun ProfileStatCard(
    title: String,
    value: String,
    onInfoClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (onInfoClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Información del nivel de fan",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(15.dp)
                            .clickable {
                                onInfoClick()
                            }
                    )
                }
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FavoriteAnimeCard(
    anime: Anime
) {
    Card(
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            AsyncImage(
                model = anime.coverImageUrl,
                contentDescription = "Portada de ${anime.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = anime.genres
                        .take(2)
                        .joinToString(" • ") { it.name }
                        .ifBlank { "Sin géneros" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun EmptySectionMessage(
    message: String
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

private fun durationPreferenceLabel(
    duration: DurationType
): String {
    return when (duration) {
        DurationType.SHORT -> "Cortas"
        DurationType.MEDIUM -> "Medias"
        DurationType.LONG -> "Largas"
    }
}

private fun decodeImageBitmapFromDataUrl(
    dataUrl: String
): ImageBitmap? {
    if (!dataUrl.startsWith("data:image")) {
        return null
    }

    return runCatching {
        val encoded = dataUrl.substringAfter(
            delimiter = "base64,",
            missingDelimiterValue = ""
        )

        if (encoded.isBlank()) {
            return null
        }

        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}

@Preview(showBackground = true)
@Composable
private fun ProfileContentPreview() {
    AnimeDevTheme {
        Surface {
            ProfileContent(
                profile = UserProfile(
                    id = "1",
                    name = "Naruto Uzumaki",
                    nickname = "hokagefan",
                    email = "naruto@konoha.com",
                    avatarUrl = "",
                    knowledgeLevel = "Aprendiz",
                    xpPoints = 100,
                    biography = "El próximo Hokage",
                    favoriteGenres = listOf(
                        FakeDataSource.genres.first(),
                        FakeDataSource.genres[1]
                    ),
                    preferredDurations = listOf(
                        DurationType.SHORT,
                        DurationType.MEDIUM
                    ),
                    totalAnimesWatched = 0,
                    completedTrivias = 7,
                    badges = emptyList(),
                    coverImageUrl = ""
                ),
                favoriteAnimes = FakeDataSource.animeCatalog.take(3),
                fanLevel = "Aprendiz",
                triviaPlayedCount = 7
            )
        }
    }
}