package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaModerationQuestion

@Composable
fun TriviaModerationScreen(
    appContainer: AppContainer = DefaultAppContainer(),
    onBack: () -> Unit
) {
    val viewModel: TriviaModerationViewModel = viewModel(
        factory = TriviaModerationViewModel.provideFactory(
            triviaModerationRepository = appContainer.triviaModerationRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            // Se mantiene el mensaje visible hasta que el usuario toque refrescar o procese otra acción.
        }
    }

    TriviaModerationContent(
        state = uiState,
        onBack = onBack,
        onRefresh = viewModel::loadPendingQuestions,
        onApprove = viewModel::approveQuestion,
        onReject = viewModel::rejectQuestion,
        onClearMessages = viewModel::clearMessages
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TriviaModerationContent(
    state: TriviaModerationUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Moderación de trivias")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            onClearMessages()
                            onRefresh()
                        },
                        enabled = !state.isLoading && !state.isProcessing
                    ) {
                        Text("Actualizar")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            state.errorMessage != null && state.questions.isEmpty() -> ErrorContent(
                message = state.errorMessage,
                onRetry = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            state.questions.isEmpty() -> EmptyContent(
                message = state.successMessage,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> PendingQuestionsContent(
                state = state,
                onApprove = onApprove,
                onReject = onReject,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun PendingQuestionsContent(
    state: TriviaModerationUiState,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Preguntas pendientes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Revisa que la pregunta sea correcta para el anime, que no invente datos y que las opciones estén bien formuladas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        state.successMessage?.let { message ->
            item {
                StatusCard(
                    message = message,
                    isError = false
                )
            }
        }

        state.errorMessage?.let { message ->
            item {
                StatusCard(
                    message = message,
                    isError = true
                )
            }
        }

        items(
            items = state.questions,
            key = { question -> question.id }
        ) { question ->
            PendingQuestionCard(
                question = question,
                isProcessing = state.isProcessing &&
                        state.processingQuestionId == question.id,
                onApprove = {
                    onApprove(question.id)
                },
                onReject = {
                    onReject(question.id)
                }
            )
        }
    }
}

@Composable
private fun PendingQuestionCard(
    question: TriviaModerationQuestion,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text("Anime ID: ${question.animeId}")
                    }
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text(question.difficulty.displayName)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text(question.category.displayName)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            question.options.forEachIndexed { index, option ->
                val isCorrect = index == question.correctAnswerIndex

                Text(
                    text = "${index + 1}. $option${if (isCorrect) "  ✓" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCorrect) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (isCorrect) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            question.explanation?.let { explanation ->
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Explicación:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Fuente: ${question.source.ifBlank { "No registrada" }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            question.createdAt?.let { createdAt ->
                Text(
                    text = "Creada: $createdAt",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isProcessing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aprobar")
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(12.dp))

        Text("Cargando preguntas pendientes...")
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun EmptyContent(
    message: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay preguntas pendientes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message ?: "Cuando los usuarios envíen nuevas preguntas, aparecerán aquí para revisión.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusCard(
    message: String,
    isError: Boolean
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = if (isError) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}