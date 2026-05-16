package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaCategory
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty

@Composable
fun AddTriviaQuestionScreen(
    animeId: Long,
    appContainer: AppContainer = DefaultAppContainer(),
    onBack: () -> Unit
) {
    val viewModel: AddTriviaQuestionViewModel = viewModel(
        factory = AddTriviaQuestionViewModel.provideFactory(
            animeId = animeId,
            triviaContributionRepository = appContainer.triviaContributionRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    AddTriviaQuestionContent(
        state = uiState,
        onBack = onBack,
        onQuestionChange = viewModel::updateQuestion,
        onOptionChange = viewModel::updateOption,
        onCorrectAnswerChange = viewModel::updateCorrectAnswerIndex,
        onDifficultyChange = viewModel::updateDifficulty,
        onCategoryChange = viewModel::updateCategory,
        onExplanationChange = viewModel::updateExplanation,
        onSubmit = viewModel::submit
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddTriviaQuestionContent(
    state: AddTriviaQuestionUiState,
    onBack: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onCorrectAnswerChange: (Int) -> Unit,
    onDifficultyChange: (TriviaDifficulty) -> Unit,
    onCategoryChange: (TriviaCategory) -> Unit,
    onExplanationChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(text = "Agregar pregunta")
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IntroCard()
            }

            item {
                OutlinedTextField(
                    value = state.question,
                    onValueChange = onQuestionChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                    label = {
                        Text("Pregunta")
                    },
                    placeholder = {
                        Text("Ej: ¿Quién es el protagonista principal de la historia?")
                    },
                    minLines = 2,
                    maxLines = 4
                )
            }

            item {
                Text(
                    text = "Opciones de respuesta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                state.options.forEachIndexed { index, option ->
                    AnswerOptionField(
                        index = index,
                        value = option,
                        selected = state.correctAnswerIndex == index,
                        enabled = !state.isSubmitting,
                        onValueChange = { value ->
                            onOptionChange(index, value)
                        },
                        onSelected = {
                            onCorrectAnswerChange(index)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Marca con el círculo cuál es la respuesta correcta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text(
                    text = "Dificultad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TriviaDifficulty.entries.forEach { difficulty ->
                        FilterChip(
                            selected = state.difficulty == difficulty,
                            onClick = {
                                onDifficultyChange(difficulty)
                            },
                            enabled = !state.isSubmitting,
                            label = {
                                Text(difficulty.displayName)
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TriviaCategory.entries.forEach { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = {
                                onCategoryChange(category)
                            },
                            enabled = !state.isSubmitting,
                            label = {
                                Text(category.displayName)
                            }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.explanation,
                    onValueChange = onExplanationChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                    label = {
                        Text("Explicación opcional")
                    },
                    placeholder = {
                        Text("Ej: El personaje cumple ese rol durante la historia.")
                    },
                    minLines = 2,
                    maxLines = 4
                )
            }

            state.errorMessage?.let { message ->
                item {
                    StatusCard(
                        message = message,
                        isError = true
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

            item {
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator()
                    } else {
                        Text("Enviar pregunta")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
private fun IntroCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Crea una pregunta sobre la serie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Puedes proponer preguntas sobre protagonistas, antagonistas, autor, valores, escenario, razas, grupos, creencias o hechos importantes de la historia. La pregunta quedará pendiente hasta ser revisada.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnswerOptionField(
    index: Int,
    value: String,
    selected: Boolean,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelected,
            enabled = enabled
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            label = {
                Text("Opción ${index + 1}")
            },
            singleLine = true
        )
    }
}

@Composable
private fun StatusCard(
    message: String,
    isError: Boolean
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = textColor,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}