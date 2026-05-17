package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
                    Text("Administrar trivias")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
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
        ) {
            if (uiState.isLoading || uiState.isActionLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AdminTriviaSectionSelector(
                selectedSection = uiState.selectedSection,
                onSectionSelected = viewModel::selectSection,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            when (uiState.selectedSection) {
                AdminTriviaSection.REQUESTS -> QuestionRequestsSection(
                    requests = uiState.questionRequests,
                    onApprove = viewModel::approveQuestionRequest,
                    onReject = viewModel::rejectQuestionRequest,
                    onRefresh = viewModel::loadQuestionRequests
                )

                AdminTriviaSection.REPORTS -> QuestionReportsSection(
                    selectedStatus = uiState.reportStatusFilter,
                    reports = uiState.questionReports,
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
        AlertDialog(
            onDismissRequest = {
                questionPendingDelete = null
            },
            title = {
                Text("Borrar pregunta")
            },
            text = {
                Text(
                    text = "¿Seguro que deseas borrar esta pregunta?\n\n${question.question}"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuestion(question.id)
                        questionPendingDelete = null
                    }
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        questionPendingDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminTriviaSectionSelector(
    selectedSection: AdminTriviaSection,
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
                onClick = { onSectionSelected(section) },
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
                subtitle = "Aquí aparecen las preguntas enviadas por usuarios pendientes de aprobación.",
                onRefresh = onRefresh
            )
        }

        if (requests.isEmpty()) {
            item {
                EmptyAdminState("No hay solicitudes pendientes.")
            }
        } else {
            items(
                items = requests,
                key = { it.id }
            ) { question ->
                AdminQuestionRequestCard(
                    question = question,
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
                subtitle = "Reportes enviados desde el botón de error dentro de las trivias.",
                onRefresh = onRefresh
            )

            ReportStatusFilters(
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected
            )
        }

        if (reports.isEmpty()) {
            item {
                EmptyAdminState("No hay quejas con este estado.")
            }
        } else {
            items(
                items = reports,
                key = { it.id }
            ) { report ->
                QuestionReportCard(
                    report = report,
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
    onStatusSelected: (String) -> Unit
) {
    val statuses = listOf(
        "PENDING" to "Pendientes",
        "RESOLVED" to "Resueltas",
        "DELETED" to "Eliminadas",
        "REJECTED" to "Rechazadas",
        "ALL" to "Todas"
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
                onClick = { onStatusSelected(value) },
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
                subtitle = "Busca un anime, revisa sus preguntas, edítalas o elimínalas.",
                onRefresh = onRefresh
            )

            OutlinedTextField(
                value = animeQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Buscar anime")
                },
                placeholder = {
                    Text("Ejemplo: Naruto, One Piece, Spy x Family")
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
                    onClick = { onAnimeSelected(anime) }
                )
            }
        }

        selectedAnime?.let { anime ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Preguntas cargadas: ${questions.size}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Button(
                            onClick = onCreateQuestion,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Crear pregunta para este anime")
                        }
                    }
                }
            }

            item {
                DifficultyCountersCard(questions = questions)
            }
        }

        if (selectedAnime != null && questions.isEmpty()) {
            item {
                EmptyAdminState("Este anime aún no tiene preguntas registradas.")
            }
        }

        items(
            items = questions,
            key = { it.id }
        ) { question ->
            AdminBankQuestionCard(
                question = question,
                onEdit = { onEditQuestion(question) },
                onDelete = { onDeleteQuestion(question) }
            )
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
                text = "EASY: $easy / 5 ${if (easy >= 5) "✅" else "⚠️"}",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "MEDIUM: $medium / 7 ${if (medium >= 7) "✅" else "⚠️"}",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = "HARD: $hard / 10 ${if (hard >= 10) "✅" else "⚠️"}",
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar")
        }
    }
}

@Composable
private fun AdminQuestionRequestCard(
    question: AdminTriviaQuestionResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    AdminQuestionBaseCard(question = question) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onApprove,
                modifier = Modifier.weight(1f)
            ) {
                Text("Aprobar")
            }

            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f)
            ) {
                Text("Rechazar")
            }
        }
    }
}

@Composable
private fun AdminBankQuestionCard(
    question: AdminTriviaQuestionResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AdminQuestionBaseCard(question = question) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            ) {
                Text("Editar")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f)
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

            question.explanation?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Explicación: $it",
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

            report.createdAt?.let {
                Text(
                    text = "Fecha: $it",
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Eliminar")
                    }

                    Button(
                        onClick = onResolved,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Resolver")
                    }
                }

                OutlinedButton(
                    onClick = onRejected,
                    modifier = Modifier.fillMaxWidth()
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
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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

            anime.originalTitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
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
            if (!isSaving) onDismiss()
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
                    label = {
                        Text("Pregunta")
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
                        onValueChange = { value -> onOptionChange(index, value) },
                        modifier = Modifier.fillMaxWidth(),
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
                            onClick = { onCorrectAnswerChange(index) },
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
                            onClick = { onDifficultyChange(difficulty) },
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
                    label = {
                        Text("Categoría")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = form.explanation,
                    onValueChange = onExplanationChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Explicación")
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
                            onClick = { onStatusChange(status) },
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
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EmptyAdminState(
    message: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}