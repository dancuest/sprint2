package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.remote.AdminAnimeSearchResponse
import com.example.animedev20.ui.theme.data.remote.AdminTriviaQuestionResponse
import com.example.animedev20.ui.theme.data.remote.TriviaQuestionReportResponse
import com.example.animedev20.ui.theme.ux.AnimeDevCopy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTriviaScreen(
    onBack: () -> Unit,
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: AdminTriviaViewModel = viewModel(
        factory = AdminTriviaViewModel.provideFactory(
            repository = appContainer.triviaAdminRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var questionPendingDelete by remember {
        mutableStateOf<AdminTriviaQuestionResponse?>(null)
    }

    val isBusy = uiState.isLoading || uiState.isActionLoading

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Panel de trivias")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !isBusy
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = AnimeDevCopy.Actions.goBack
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AdminTriviaSectionSelector(
                selectedSection = uiState.selectedSection,
                enabled = !isBusy,
                onSectionSelected = viewModel::selectSection,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when (uiState.selectedSection) {
                AdminTriviaSection.REQUESTS -> QuestionRequestsSection(
                    requests = uiState.questionRequests,
                    isBusy = isBusy,
                    onApprove = viewModel::approveQuestionRequest,
                    onReject = viewModel::rejectQuestionRequest,
                    onRefresh = viewModel::loadQuestionRequests
                )

                AdminTriviaSection.REPORTS -> QuestionReportsSection(
                    selectedStatus = uiState.reportStatusFilter,
                    reports = uiState.questionReports,
                    isBusy = isBusy,
                    onStatusSelected = viewModel::setReportStatusFilter,
                    onDelete = viewModel::deleteReport,
                    onResolved = viewModel::openResolveReportEditor,
                    onRejected = viewModel::rejectReport,
                    onRefresh = viewModel::loadQuestionReports
                )

                AdminTriviaSection.BANK -> QuestionBankSection(
                    animeQuery = uiState.animeQuery,
                    animeResults = uiState.animeResults,
                    selectedAnime = uiState.selectedAnime,
                    questions = uiState.animeQuestions,
                    isBusy = isBusy,
                    onQueryChange = viewModel::updateAnimeQuery,
                    onAnimeSelected = viewModel::selectAnime,
                    onCreateQuestion = viewModel::openCreateQuestionEditor,
                    onEditQuestion = viewModel::openEditQuestionEditor,
                    onDeleteQuestion = { question ->
                        questionPendingDelete = question
                    },
                    onRefresh = {
                        uiState.selectedAnime?.id?.let(viewModel::loadQuestionsByAnime)
                    }
                )
            }
        }
    }

    if (uiState.editorVisible) {
        AdminQuestionEditorDialog(
            form = uiState.editorForm,
            isSaving = uiState.isActionLoading,
            onQuestionChange = viewModel::updateEditorQuestion,
            onOptionChange = viewModel::updateEditorOption,
            onCorrectAnswerChange = viewModel::updateEditorCorrectAnswer,
            onDifficultyChange = viewModel::updateEditorDifficulty,
            onCategoryChange = viewModel::updateEditorCategory,
            onExplanationChange = viewModel::updateEditorExplanation,
            onStatusChange = viewModel::updateEditorStatus,
            onSave = viewModel::saveEditorQuestion,
            onDismiss = viewModel::closeEditor
        )
    }

    questionPendingDelete?.let { question ->
        DeleteQuestionDialog(
            question = question,
            isDeleting = uiState.isActionLoading,
            onConfirm = {
                viewModel.deleteQuestion(question.id)
                questionPendingDelete = null
            },
            onDismiss = {
                questionPendingDelete = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTriviaSectionSelector(
    selectedSection: AdminTriviaSection,
    enabled: Boolean,
    onSectionSelected: (AdminTriviaSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdminTriviaSection.entries.forEach { section ->
            FilterChip(
                selected = selectedSection == section,
                onClick = {
                    onSectionSelected(section)
                },
                enabled = enabled,
                label = {
                    Text(section.label)
                }
            )
        }
    }
}

@Composable
private fun QuestionRequestsSection(
    requests: List<AdminTriviaQuestionResponse>,
    isBusy: Boolean,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Solicitudes de preguntas",
                subtitle = "Revisa las preguntas propuestas por usuarios antes de agregarlas al banco oficial.",
                isBusy = isBusy,
                onRefresh = onRefresh
            )
        }

        if (requests.isEmpty()) {
            item {
                EmptyAdminState(
                    title = "No hay solicitudes pendientes",
                    message = "Cuando un usuario envíe una pregunta, aparecerá aquí para aprobarla o rechazarla."
                )
            }
        } else {
            items(
                items = requests,
                key = { it.id }
            ) { question ->
                AdminQuestionRequestCard(
                    question = question,
                    isBusy = isBusy,
                    onApprove = { onApprove(question.id) },
                    onReject = { onReject(question.id) }
                )
            }
        }
    }
}

@Composable
private fun QuestionReportsSection(
    selectedStatus: String,
    reports: List<TriviaQuestionReportResponse>,
    isBusy: Boolean,
    onStatusSelected: (String) -> Unit,
    onDelete: (String) -> Unit,
    onResolved: (TriviaQuestionReportResponse) -> Unit,
    onRejected: (String) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Quejas de preguntas",
                subtitle = "Gestiona reportes enviados desde las trivias. Puedes resolver, rechazar o mover una queja a eliminadas.",
                isBusy = isBusy,
                onRefresh = onRefresh
            )

            ReportStatusFilters(
                selectedStatus = selectedStatus,
                enabled = !isBusy,
                onStatusSelected = onStatusSelected
            )
        }

        if (reports.isEmpty()) {
            item {
                EmptyAdminState(
                    title = "No hay quejas en este estado",
                    message = "Cambia el filtro o actualiza la sección para revisar otros reportes."
                )
            }
        } else {
            items(
                items = reports,
                key = { it.id }
            ) { report ->
                QuestionReportCard(
                    report = report,
                    isBusy = isBusy,
                    onDelete = { onDelete(report.id) },
                    onResolved = { onResolved(report) },
                    onRejected = { onRejected(report.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportStatusFilters(
    selectedStatus: String,
    enabled: Boolean,
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf(
        "PENDING" to "Pendientes",
        "RESOLVED" to "Resueltas",
        "DELETED" to "Eliminadas"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { (value, label) ->
            FilterChip(
                selected = selectedStatus == value,
                onClick = {
                    onStatusSelected(value)
                },
                enabled = enabled,
                label = {
                    Text(label)
                }
            )
        }
    }
}

@Composable
private fun QuestionBankSection(
    animeQuery: String,
    animeResults: List<AdminAnimeSearchResponse>,
    selectedAnime: AdminAnimeSearchResponse?,
    questions: List<AdminTriviaQuestionResponse>,
    isBusy: Boolean,
    onQueryChange: (String) -> Unit,
    onAnimeSelected: (AdminAnimeSearchResponse) -> Unit,
    onCreateQuestion: () -> Unit,
    onEditQuestion: (AdminTriviaQuestionResponse) -> Unit,
    onDeleteQuestion: (AdminTriviaQuestionResponse) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Banco de preguntas por anime",
                subtitle = "Busca un anime para crear, editar o eliminar preguntas del banco oficial.",
                isBusy = isBusy,
                onRefresh = onRefresh
            )

            OutlinedTextField(
                value = animeQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy,
                label = {
                    Text("Buscar anime")
                },
                placeholder = {
                    Text("Ejemplo: Naruto, One Piece, Spy x Family")
                },
                supportingText = {
                    Text("Escribe mínimo 2 caracteres para buscar.")
                },
                singleLine = true
            )
        }

        if (animeResults.isNotEmpty()) {
            items(
                items = animeResults,
                key = { it.id }
            ) { anime ->
                AnimeSearchResultCard(
                    anime = anime,
                    enabled = !isBusy,
                    onClick = { onAnimeSelected(anime) }
                )
            }
        }

        selectedAnime?.let { anime ->
            item {
                SelectedAnimeCard(
                    anime = anime,
                    questionsCount = questions.size,
                    isBusy = isBusy,
                    onCreateQuestion = onCreateQuestion
                )
            }

            item {
                DifficultyCountersCard(questions = questions)
            }
        }

        if (selectedAnime != null && questions.isEmpty()) {
            item {
                EmptyAdminState(
                    title = "Este anime no tiene preguntas",
                    message = "Puedes crear la primera pregunta desde el botón superior."
                )
            }
        }

        items(
            items = questions,
            key = { it.id }
        ) { question ->
            AdminBankQuestionCard(
                question = question,
                isBusy = isBusy,
                onEdit = { onEditQuestion(question) },
                onDelete = { onDeleteQuestion(question) }
            )
        }
    }
}

@Composable
private fun SelectedAnimeCard(
    anime: AdminAnimeSearchResponse,
    questionsCount: Int,
    isBusy: Boolean,
    onCreateQuestion: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = "Preguntas cargadas: $questionsCount",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Button(
                onClick = onCreateQuestion,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBusy
            ) {
                Text("Crear pregunta para este anime")
            }
        }
    }
}

@Composable
private fun DifficultyCountersCard(
    questions: List<AdminTriviaQuestionResponse>
) {
    val approvedQuestions = questions.filter {
        it.status.equals("APPROVED", ignoreCase = true)
    }

    val easy = approvedQuestions.count { it.difficulty == "EASY" }
    val medium = approvedQuestions.count { it.difficulty == "MEDIUM" }
    val hard = approvedQuestions.count { it.difficulty == "HARD" }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estado del banco aprobado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Fácil: $easy / 5 ${if (easy >= 5) "✅" else "⚠️"}",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Media: $medium / 7 ${if (medium >= 7) "✅" else "⚠️"}",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Difícil: $hard / 10 ${if (hard >= 10) "✅" else "⚠️"}",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "Total aprobado: ${approvedQuestions.size} / 22",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            if (questions.size != approvedQuestions.size) {
                Text(
                    text = "Total visible incluyendo pendientes/rechazadas: ${questions.size}",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    isBusy: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isBusy
        ) {
            Text("Actualizar")
        }
    }
}

@Composable
private fun AdminQuestionRequestCard(
    question: AdminTriviaQuestionResponse,
    isBusy: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    AdminQuestionBaseCard(question = question) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1f),
                enabled = !isBusy
            ) {
                Text("Aprobar")
            }

            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                enabled = !isBusy
            ) {
                Text("Rechazar")
            }
        }
    }
}

@Composable
private fun AdminBankQuestionCard(
    question: AdminTriviaQuestionResponse,
    isBusy: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdminQuestionBaseCard(question = question) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                enabled = !isBusy
            ) {
                Text("Editar")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                enabled = !isBusy
            ) {
                Text("Borrar")
            }
        }
    }
}

@Composable
private fun AdminQuestionBaseCard(
    question: AdminTriviaQuestionResponse,
    actions: @Composable () -> Unit
) {
    val animeTitle = question.animeTitle
        ?.takeIf { it.isNotBlank() }

    val animeLabel = animeTitle
        ?: question.animeId?.let { animeId ->
            "Anime ID: $animeId"
        }
        ?: "Anime no identificado"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Anime destino",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = animeLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (animeTitle != null && question.animeId != null) {
                        Text(
                            text = "ID: ${question.animeId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(question.difficulty)
                    }
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(question.status ?: "SIN ESTADO")
                    }
                )
            }

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            question.options.forEachIndexed { index, option ->
                val prefix = ('A' + index).toString()
                val marker = if (index == question.correctAnswerIndex) "✓" else "•"

                Text(
                    text = "$marker $prefix. $option",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            question.explanation?.takeIf { it.isNotBlank() }?.let { explanation ->
                Text(
                    text = "Explicación: $explanation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            actions()
        }
    }
}

@Composable
private fun QuestionReportCard(
    report: TriviaQuestionReportResponse,
    isBusy: Boolean,
    onDelete: () -> Unit,
    onResolved: () -> Unit,
    onRejected: () -> Unit
) {
    val isPending = report.status.equals("PENDING", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(report.status)
                    }
                )

                report.animeTitle?.takeIf { it.isNotBlank() }?.let { title ->
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            Text(
                text = "Pregunta reportada",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = report.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Queja del usuario",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = report.reason,
                style = MaterialTheme.typography.bodyMedium
            )

            report.createdAt?.let { createdAt ->
                Text(
                    text = "Fecha: $createdAt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            if (isPending) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        enabled = !isBusy
                    ) {
                        Text("Enviar a eliminadas")
                    }

                    Button(
                        onClick = onResolved,
                        modifier = Modifier.weight(1f),
                        enabled = !isBusy
                    ) {
                        Text("Resolver")
                    }
                }

                OutlinedButton(
                    onClick = onRejected,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy
                ) {
                    Text("Rechazar queja")
                }
            } else {
                Text(
                    text = "Esta queja ya fue procesada.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AnimeSearchResultCard(
    anime: AdminAnimeSearchResponse,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = anime.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            anime.originalTitle?.takeIf { it.isNotBlank() }?.let { originalTitle ->
                Text(
                    text = originalTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "ID: ${anime.id} · Episodios: ${anime.totalEpisodes ?: "N/D"} · Año: ${anime.releaseYear ?: "N/D"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminQuestionEditorDialog(
    form: AdminQuestionFormState,
    isSaving: Boolean,
    onQuestionChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onCorrectAnswerChange: (Int) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onExplanationChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = when {
                    form.resolveReportId != null -> "Resolver queja"
                    form.id == null -> "Crear pregunta"
                    else -> "Editar pregunta"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (form.resolveReportId != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 1.dp,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Edita la pregunta, las opciones, la respuesta correcta y la explicación. Al guardar, la queja quedará marcada como resuelta.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = form.question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    label = {
                        Text("Pregunta")
                    },
                    placeholder = {
                        Text("Ejemplo: ¿Qué personaje toma esta decisión clave?")
                    },
                    minLines = 2
                )

                Text(
                    text = "Opciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                form.options.forEachIndexed { index, option ->
                    OutlinedTextField(
                        value = option,
                        onValueChange = { value ->
                            onOptionChange(index, value)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        label = {
                            Text("Opción ${'A' + index}")
                        },
                        singleLine = true
                    )
                }

                Text(
                    text = "Respuesta correcta",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { index ->
                        FilterChip(
                            selected = form.correctAnswerIndex == index,
                            onClick = {
                                onCorrectAnswerChange(index)
                            },
                            enabled = !isSaving,
                            label = {
                                Text(('A' + index).toString())
                            }
                        )
                    }
                }

                Text(
                    text = "Dificultad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("EASY", "MEDIUM", "HARD").forEach { difficulty ->
                        FilterChip(
                            selected = form.difficulty == difficulty,
                            onClick = {
                                onDifficultyChange(difficulty)
                            },
                            enabled = !isSaving,
                            label = {
                                Text(difficulty)
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = form.category,
                    onValueChange = onCategoryChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    label = {
                        Text("Categoría")
                    },
                    placeholder = {
                        Text("Ejemplo: CHARACTER, STORY, GENERAL")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = form.explanation,
                    onValueChange = onExplanationChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving,
                    label = {
                        Text("Explicación")
                    },
                    placeholder = {
                        Text("Explica por qué esa opción es correcta.")
                    },
                    minLines = 3
                )

                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("APPROVED", "PENDING", "REJECTED").forEach { status ->
                        FilterChip(
                            selected = form.status == status,
                            onClick = {
                                onStatusChange(status)
                            },
                            enabled = !isSaving,
                            label = {
                                Text(status)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(modifier = Modifier.size(8.dp))
                }

                Text(
                    text = if (form.resolveReportId != null) {
                        "Guardar y resolver"
                    } else {
                        "Guardar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(AnimeDevCopy.Actions.cancel)
            }
        }
    )
}

@Composable
private fun DeleteQuestionDialog(
    question: AdminTriviaQuestionResponse,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        title = {
            Text("Borrar pregunta")
        },
        text = {
            Text(
                text = "Esta acción eliminará la pregunta del banco.\n\n${question.question}"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                Text("Borrar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(AnimeDevCopy.Actions.cancel)
            }
        }
    )
}

@Composable
private fun EmptyAdminState(
    title: String,
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}