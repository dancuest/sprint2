package com.example.animedev20.ui.theme.feature.settings.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.domain.model.CodedOption
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.UserDemographicCatalog
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onLogoutRequest: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.provideFactory(
            userRepository = appContainer.userRepository,
            animeRepository = appContainer.animeRepository,
            homeRefreshBus = appContainer.homeRefreshBus
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val tokenStore = remember { AuthTokenStore(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var isProcessingImage by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            isProcessingImage = true
            val imageDataUrl = withContext(Dispatchers.IO) {
                context.uriToCompressedJpegDataUrl(uri)
            }

            if (imageDataUrl != null) {
                viewModel.onAvatarImageSelected(imageDataUrl)
            }

            isProcessingImage = false
        }
    }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            isProcessingImage = true
            val imageDataUrl = withContext(Dispatchers.IO) {
                context.uriToCompressedJpegDataUrl(uri)
            }

            if (imageDataUrl != null) {
                viewModel.onCoverImageSelected(imageDataUrl)
            }

            isProcessingImage = false
        }
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.onMessageConsumed()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ajustes y preferencias") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                SettingsLoadingState(modifier = Modifier.fillMaxSize())
            } else {
                SettingsContent(
                    state = uiState,
                    onGenreSelected = viewModel::onGenreSelected,
                    onDurationSelected = viewModel::onDurationSelected,
                    onAgeRangeSelected = viewModel::onAgeRangeSelected,
                    onGenderSelected = viewModel::onGenderSelected,
                    onRegionSelected = viewModel::onRegionSelected,
                    onSavePreferences = viewModel::savePreferences,
                    onSaveAccountInfo = viewModel::saveAccountInfo,
                    onNameChange = viewModel::onNameChanged,
                    onEmailChange = viewModel::onEmailChanged,
                    onNicknameChange = viewModel::onNicknameChanged,
                    onPickAvatar = { avatarPicker.launch("image/*") },
                    onPickCover = { coverPicker.launch("image/*") },
                    onLogout = {
                        tokenStore.clearAll()
                        onLogoutRequest()
                    }
                )
            }

            if (isProcessingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.22f))
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onGenreSelected: (String) -> Unit,
    onDurationSelected: (DurationType) -> Unit,
    onAgeRangeSelected: (Int) -> Unit,
    onGenderSelected: (Int) -> Unit,
    onRegionSelected: (Int) -> Unit,
    onSavePreferences: () -> Unit,
    onSaveAccountInfo: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onPickAvatar: () -> Unit,
    onPickCover: () -> Unit,
    onLogout: () -> Unit
) {
    var genreQuery by rememberSaveable { mutableStateOf("") }

    val filteredGenres = state.availableGenres.filter { genre ->
        genre.name.contains(genreQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Personaliza tu experiencia",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Estas preferencias impactarán en tus recomendaciones y trivias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingSectionTitle(title = "Personalización visual")

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarSettingCard(
                    avatarUrl = state.avatarUrl,
                    nickname = state.nickname,
                    onClick = onPickAvatar
                )

                CoverSettingCard(
                    coverImageUrl = state.coverImageUrl,
                    onClick = onPickCover
                )
            }
        }

        item {
            SettingSectionTitle(title = "Categorías que te interesan")

            OutlinedTextField(
                value = genreQuery,
                onValueChange = { genreQuery = it },
                label = { Text("Buscar género") },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                filteredGenres.forEach { genre ->
                    FilterChip(
                        selected = state.selectedGenres.contains(genre.id),
                        onClick = { onGenreSelected(genre.id) },
                        label = { Text(genre.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }

            Text(
                text = "Seleccionados: ${state.selectedGenres.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            SettingSectionTitle(title = "Datos demográficos (opcional)")

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DemographicSelector(
                    title = "Rango de edad",
                    options = UserDemographicCatalog.ageRanges,
                    selectedCode = state.ageRange,
                    onSelected = onAgeRangeSelected
                )
                DemographicSelector(
                    title = "Sexo / género",
                    options = UserDemographicCatalog.genders,
                    selectedCode = state.genderCode,
                    onSelected = onGenderSelected
                )
                DemographicSelector(
                    title = "Región",
                    options = UserDemographicCatalog.regions,
                    selectedCode = state.regionCode,
                    onSelected = onRegionSelected
                )
            }
        }

        item {
            SettingSectionTitle(title = "Duraciones preferidas de las series")

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DurationType.values().forEach { duration ->
                    DurationPreferenceChip(
                        label = durationLabel(duration),
                        description = durationDescription(duration),
                        selected = state.preferredDurations.contains(duration),
                        onClick = { onDurationSelected(duration) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = onSavePreferences,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Guardar preferencias")
            }
        }

        item {
            SettingSectionTitle(title = "Datos del perfil")

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.nickname,
                    onValueChange = onNicknameChange,
                    label = { Text("Nombre público o nickname") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onSaveAccountInfo,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Actualizar datos de perfil")
                }
            }
        }

        item {
            SettingSectionTitle(title = "Sesión")

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun AvatarSettingCard(
    avatarUrl: String,
    nickname: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarPreview(
                avatarUrl = avatarUrl,
                fallbackText = nickname.firstOrNull()?.uppercase() ?: "A"
            )

            Column {
                Text(
                    text = "Foto de perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca aquí para cambiar tu foto desde la galería.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CoverSettingCard(
    coverImageUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CoverPreview(
                coverImageUrl = coverImageUrl,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Portada del perfil",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toca aquí para cambiar el fondo del perfil.",
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DemographicSelector(
    title: String,
    options: List<CodedOption>,
    selectedCode: Int,
    onSelected: (Int) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(6.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selectedCode == option.code,
                onClick = { onSelected(option.code) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun SettingsLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Cargando tus preferencias…")
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun DurationPreferenceChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 176.dp)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.medium
            )
            .padding(12.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (selected) "Quitar" else "Agregar")
        }
    }
}

@Composable
private fun AvatarPreview(
    avatarUrl: String,
    fallbackText: String
) {
    val decodedBitmap = remember(avatarUrl) {
        decodeImageBitmapFromDataUrl(avatarUrl)
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        when {
            decodedBitmap != null -> Image(
                bitmap = decodedBitmap,
                contentDescription = "Vista previa de la foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            avatarUrl.isNotBlank() -> AsyncImage(
                model = avatarUrl,
                contentDescription = "Vista previa de la foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            else -> Text(
                text = fallbackText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CoverPreview(
    coverImageUrl: String,
    modifier: Modifier = Modifier
) {
    val decodedBitmap = remember(coverImageUrl) {
        decodeImageBitmapFromDataUrl(coverImageUrl)
    }

    when {
        decodedBitmap != null -> Image(
            bitmap = decodedBitmap,
            contentDescription = "Vista previa de portada",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )

        coverImageUrl.isNotBlank() -> AsyncImage(
            model = coverImageUrl,
            contentDescription = "Vista previa de portada",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )

        else -> Box(
            modifier = modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF6C63FF),
                        Color(0xFF8B5CFF),
                        Color(0xFFFF7AB6)
                    )
                )
            )
        )
    }
}

private fun durationLabel(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Cortos"
    DurationType.MEDIUM -> "Medianos"
    DurationType.LONG -> "Largos"
}

private fun durationDescription(durationType: DurationType) = when (durationType) {
    DurationType.SHORT -> "Menos de 15 episodios, perfectos para maratones rápidos."
    DurationType.MEDIUM -> "Series entre 16 y 40 episodios para equilibrar historia y tiempo."
    DurationType.LONG -> "Sagas extensas, ideales si disfrutas seguir historias épicas."
}

private fun Context.uriToCompressedJpegDataUrl(uri: Uri): String? {
    val bitmap = loadBitmapFromUri(uri) ?: return null
    val scaledBitmap = bitmap.scaleToMaxSide(1200)

    val outputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
    val bytes = outputStream.toByteArray()

    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64"
}

private fun Context.loadBitmapFromUri(uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    } catch (_: Exception) {
        null
    }
}

private fun Bitmap.scaleToMaxSide(maxSide: Int): Bitmap {
    val currentMaxSide = max(width, height)
    if (currentMaxSide <= maxSide) return this

    val ratio = maxSide.toFloat() / currentMaxSide.toFloat()
    val newWidth = (width * ratio).roundToInt()
    val newHeight = (height * ratio).roundToInt()

    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
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
private fun SettingsContentPreview() {
    AnimeDevTheme {
        SettingsContent(
            state = SettingsUiState(
                isLoading = false,
                availableGenres = FakeDataSource.genres,
                selectedGenres = FakeDataSource.preferredGenres.map { it.id }.toSet(),
                preferredDurations = setOf(DurationType.MEDIUM),
                notificationsEnabled = true,
                culturalAlertsEnabled = true,
                autoplayNextEpisode = true,
                hasCompletedOnboarding = true,
                name = "Akira Morales",
                email = "akira@animedev.io",
                nickname = "OtakuSensei",
                avatarUrl = "",
                coverImageUrl = ""
            ),
            onGenreSelected = {},
            onDurationSelected = {},
            onAgeRangeSelected = {},
            onGenderSelected = {},
            onRegionSelected = {},
            onSavePreferences = {},
            onSaveAccountInfo = {},
            onNameChange = {},
            onEmailChange = {},
            onNicknameChange = {},
            onPickAvatar = {},
            onPickCover = {},
            onLogout = {}
        )
    }
}