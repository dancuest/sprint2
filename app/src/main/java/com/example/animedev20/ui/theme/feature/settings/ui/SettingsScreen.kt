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

        if (!state.isGuest) {
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

        if (!state.isGuest) {
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
        }

        if (!state.isGuest) {
            item {
                SettingSectionTitle(title = "Seguridad")
                ChangePasswordCard(
                    isLoading = state.isChangingPassword,
                    onChangePassword = onChangePassword
                )
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

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Confirmar nueva contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (canSubmit) {
                            onChangePassword(currentPassword, newPassword)
                            currentPassword = ""
                            newPassword = ""
                            confirmNewPassword = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                isError = confirmNewPassword.isNotBlank() && newPassword != confirmNewPassword,
                supportingText = {
                    if (confirmNewPassword.isNotBlank() && newPassword != confirmNewPassword) {
                        Text("Las contraseñas no coinciden")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onChangePassword(currentPassword, newPassword)
                    currentPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                    focusManager.clearFocus()
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Actualizar contraseña")
                }
            }
        }
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun AvatarSettingCard(
    avatarUrl: String,
    nickname: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarPreview(
                avatarUrl = avatarUrl,
                nickname = nickname
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(
                    text = "Foto de perfil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Toca para seleccionar una nueva imagen",
                    style = MaterialTheme.typography.bodySmall,
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
            .height(150.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CoverPreview(
                coverImageUrl = coverImageUrl,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
            Text(
                text = "Toca para actualizar tu portada",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun AvatarPreview(
    avatarUrl: String,
    nickname: String
) {
    val decodedBitmap = remember(avatarUrl) {
        decodeImageBitmapFromDataUrl(avatarUrl)
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            decodedBitmap != null -> Image(
                bitmap = decodedBitmap,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            avatarUrl.isNotBlank() -> AsyncImage(
                model = avatarUrl,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            else -> Text(
                text = nickname.take(1).uppercase().ifBlank { "A" },
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
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
            contentDescription = "Portada",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )

        coverImageUrl.isNotBlank() -> AsyncImage(
            model = coverImageUrl,
            contentDescription = "Portada",
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
private fun DurationPreferenceChip(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 92.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selectedCode == option.code,
                    onClick = { onSelected(option.code) },
                    label = { Text(option.label) }
                )
            }
        }
    }
}

@Composable
private fun SettingsLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

private fun durationLabel(duration: DurationType): String {
    return when (duration) {
        DurationType.SHORT -> "Cortas"
        DurationType.MEDIUM -> "Medias"
        DurationType.LONG -> "Largas"
    }
}

private fun durationDescription(duration: DurationType): String {
    return when (duration) {
        DurationType.SHORT -> "Ideal para ver capítulos rápidos o historias compactas."
        DurationType.MEDIUM -> "Equilibrio entre desarrollo de historia y duración."
        DurationType.LONG -> "Series extensas para maratones o tramas más profundas."
    }
}

private fun decodeImageBitmapFromDataUrl(dataUrl: String): ImageBitmap? {
    if (!dataUrl.startsWith("data:image")) return null

    return runCatching {
        val encoded = dataUrl.substringAfter("base64,", missingDelimiterValue = "")
        if (encoded.isBlank()) return null
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }.getOrNull()
}

private suspend fun Context.uriToCompressedJpegDataUrl(uri: Uri): String? = runCatching {
    val bitmap = loadBitmapFromUri(uri) ?: return null
    val resized = bitmap.resizeKeepingAspect(maxSide = 1200)
    val stream = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, 82, stream)
    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    "data:image/jpeg;base64,$base64"
}.getOrNull()

private fun Context.loadBitmapFromUri(uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = false
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }
}

private fun Bitmap.resizeKeepingAspect(maxSide: Int): Bitmap {
    val srcWidth = width
    val srcHeight = height
    val maxCurrentSide = max(srcWidth, srcHeight)

    if (maxCurrentSide <= maxSide) return this

    val scale = maxSide.toFloat() / maxCurrentSide.toFloat()
    val targetWidth = (srcWidth * scale).roundToInt()
    val targetHeight = (srcHeight * scale).roundToInt()

    return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
}