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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.domain.model.CodedOption
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.UserDemographicCatalog
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
            homeRefreshBus = appContainer.homeRefreshBus,
            usersApi = appContainer.usersApi!!
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
            if (imageDataUrl != null) viewModel.onAvatarImageSelected(imageDataUrl)
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
            if (imageDataUrl != null) viewModel.onCoverImageSelected(imageDataUrl)
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
                    onChangePassword = viewModel::changePassword,
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
    onChangePassword: (String, String) -> Unit,
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
        // ── Encabezado ──────────────────────────────────────────────────────
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

        // ── Personalización visual ──────────────────────────────────────────
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

        // ── Géneros ─────────────────────────────────────────────────────────
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

        // ── Datos demográficos ───────────────────────────────────────────────
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

        // ── Duraciones ──────────────────────────────────────────────────────
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

        // ── Datos del perfil ─────────────────────────────────────────────────
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

        // ── Cambiar contraseña ───────────────────────────────────────────────
        item {
            SettingSectionTitle(title = "Seguridad")
            ChangePasswordCard(
                isLoading = state.isChangingPassword,
                onChangePassword = onChangePassword
            )
        }

        // ── Sesión ───────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// Sección de cambio de contraseña
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChangePasswordCard(
    isLoading: Boolean,
    onChangePassword: (String, String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmNewPassword by rememberSaveable { mutableStateOf("") }
    var currentVisible by rememberSaveable { mutableStateOf(false) }
    var newVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }

    val passwordsMatch = newPassword.isBlank() || confirmNewPassword.isBlank()
            || newPassword == confirmNewPassword
    val canSubmit = currentPassword.isNotBlank()
            && newPassword.isNotBlank()
            && confirmNewPassword.isNotBlank()
            && passwordsMatch
            && !isLoading

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cambiar contraseña",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Debes ingresar tu contraseña actual para poder establecer una nueva.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Contraseña actual
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Contraseña actual") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { currentVisible = !currentVisible }) {
                        Icon(
                            imageVector = if (currentVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (currentVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Nueva contraseña
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nueva contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { newVisible = !newVisible }) {
                        Icon(
                            imageVector = if (newVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (newVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Confirmar nueva contraseña
            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Confirmar nueva contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (!passwordsMatch) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(
                            imageVector = if (confirmVisible) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = !passwordsMatch,
                supportingText = if (!passwordsMatch) {
                    { Text("Las contraseñas no coinciden") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canSubmit) {
                            onChangePassword(currentPassword, newPassword)
                            currentPassword = ""
                            newPassword = ""
                            confirmNewPassword = ""
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                Button(
                    onClick = {
                        if (canSubmit) {
                            onChangePassword(currentPassword, newPassword)
                            currentPassword = ""
                            newPassword = ""
                            confirmNewPassword = ""
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar contraseña")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}

// ─────────────────────────────────────────────────────────────────────────────
// Composables de soporte (sin cambios respecto al original)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AvatarSettingCard(avatarUrl: String, nickname: String, onClick: () -> Unit) {
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
private fun CoverSettingCard(coverImageUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CoverPreview(coverImageUrl = coverImageUrl, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)))
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(text = "Portada del perfil", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "Toca aquí para cambiar el fondo del perfil.", color = Color.White)
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
    Text(text = title, style = MaterialTheme.typography.bodyMedium)
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
                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
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
private fun AvatarPreview(avatarUrl: String, fallbackText: String) {
    val decodedBitmap = remember(avatarUrl) { decodeImageBitmapFromDataUrl(avatarUrl) }
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
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            avatarUrl.isNotBlank() -> AsyncImage(
                model = avatarUrl,
                contentDescription = null,
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
private fun CoverPreview(coverImageUrl: String, modifier: Modifier = Modifier) {
    val decodedBitmap = remember(coverImageUrl) { decodeImageBitmapFromDataUrl(coverImageUrl) }
    when {
        decodedBitmap != null -> Image(
            bitmap = decodedBitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        coverImageUrl.isNotBlank() -> AsyncImage(
            model = coverImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> Box(
            modifier = modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF6C63FF), Color(0xFF8B5CFF), Color(0xFFFF7AB6))
                )
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun durationLabel(d: DurationType) = when (d) {
    DurationType.SHORT -> "Cortos"
    DurationType.MEDIUM -> "Medianos"
    DurationType.LONG -> "Largos"
}

private fun durationDescription(d: DurationType) = when (d) {
    DurationType.SHORT -> "Menos de 15 episodios, perfectos para maratones rápidos."
    DurationType.MEDIUM -> "Series entre 16 y 40 episodios para equilibrar historia y tiempo."
    DurationType.LONG -> "Sagas extensas, ideales si disfrutas seguir historias épicas."
}

private fun Context.uriToCompressedJpegDataUrl(uri: Uri): String? {
    val bitmap = loadBitmapFromUri(uri) ?: return null
    val scaledBitmap = bitmap.scaleToMaxSide(1200)
    val outputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
    val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64"
}

private fun Context.loadBitmapFromUri(uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    } catch (_: Exception) { null }
}

private fun Bitmap.scaleToMaxSide(maxSide: Int): Bitmap {
    val currentMax = max(width, height)
    if (currentMax <= maxSide) return this
    val ratio = maxSide.toFloat() / currentMax.toFloat()
    return Bitmap.createScaledBitmap(
        this,
        (width * ratio).roundToInt(),
        (height * ratio).roundToInt(),
        true
    )
}

private fun decodeImageBitmapFromDataUrl(dataUrl: String?): ImageBitmap? {
    if (dataUrl.isNullOrBlank() || !dataUrl.startsWith("data:image")) return null
    return try {
        val base64Part = dataUrl.substringAfter("base64,", "")
        if (base64Part.isBlank()) return null
        val bytes = Base64.decode(base64Part, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) { null }
}
