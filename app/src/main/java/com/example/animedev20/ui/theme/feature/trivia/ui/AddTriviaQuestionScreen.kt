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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaCategory
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.ux.AnimeDevCopy
import com.example.animedev20.ui.theme.ux.AnimeDevInfoCard
import com.example.animedev20.ui.theme.ux.AnimeDevInlineMessage
import com.example.animedev20.ui.theme.ux.AnimeDevMessageType

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
                    Text(
                        text = "Enviar pregunta",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = AnimeDevCopy.Actions.goBack
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IntroCard()
            }

            item {
                QuestionField(
                    value = state.question,
                    enabled = !state.isSubmitting,
                    onValueChange = onQuestionChange
                )
            }

            item {
                AnswerOptionsSection(
                    options = state.options,
                    correctAnswerIndex = state.correctAnswerIndex,
                    enabled = !state.isSubmitting,
                    onOptionChange = onOptionChange,
                    onCorrectAnswerChange = onCorrectAnswerChange
                )
            }

            item {
                DifficultySection(
                    selectedDifficulty = state.difficulty,
                    enabled = !state.isSubmitting,
                    onDifficultyChange = onDifficultyChange
                )
            }

            item {
                CategorySection(
                    selectedCategory = state.category,
                    enabled = !state.isSubmitting,
                    onCategoryChange = onCategoryChange
                )
            }

            item {
                ExplanationField(
                    value = state.explanation,
                    enabled = !state.isSubmitting,
                    onValueChange = onExplanationChange
                )
            }

            state.errorMessage?.let { message ->
                item {
                    AnimeDevInlineMessage(
                        text = message,
                        type = AnimeDevMessageType.ERROR
                    )
                }
            }

            state.successMessage?.let { message ->
                item {
                    AnimeDevInlineMessage(
                        text = message,
                        type = AnimeDevMessageType.SUCCESS
                    )
                }
            }

            item {
                SubmitActions(
                    isSubmitting = state.isSubmitting,
                    hasSuccessMessage = state.successMessage != null,
                    onSubmit = onSubmit,
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun IntroCard() {
    AnimeDevInfoCard(
        title = "Ayuda a mejorar esta trivia",
        message = "Propón una pregunta clara, cuatro opciones distintas y una respuesta correcta. Tu aporte quedará pendiente hasta que sea revisado."
    )
}

@Composable
private fun QuestionField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    val cleanLength = value.trim().length

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = {
            Text("Pregunta")
        },
        placeholder = {
            Text("Ej: ¿Quién es el protagonista principal?")
        },
        supportingText = {
            Text(
                text = if (cleanLength < 10) {
                    "Escribe una pregunta de mínimo 10 caracteres y ciérrala con signo de interrogación."
                } else {
                    "$cleanLength caracteres"
                }
            )
        },
        minLines = 2,
        maxLines = 4
    )
}

@Composable
private fun AnswerOptionsSection(
    options: List<String>,
    correctAnswerIndex: Int,
    enabled: Boolean,
    onOptionChange: (Int, String) -> Unit,
    onCorrectAnswerChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Opciones de respuesta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Marca el círculo de la opción correcta. Las cuatro opciones deben ser diferentes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            options.forEachIndexed { index, option ->
                AnswerOptionField(
                    index = index,
                    value = option,
                    selected = correctAnswerIndex == index,
                    enabled = enabled,
                    onValueChange = { value ->
                        onOptionChange(index, value)
                    },
                    onSelected = {
                        onCorrectAnswerChange(index)
                    }
                )
            }
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
            placeholder = {
                Text("Respuesta posible")
            },
            supportingText = {
                if (selected) {
                    Text("Respuesta correcta")
                }
            },
            singleLine = true
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySection(
    selectedDifficulty: TriviaDifficulty,
    enabled: Boolean,
    onDifficultyChange: (TriviaDifficulty) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Dificultad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Elige qué tan exigente será la pregunta para otros usuarios.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriviaDifficulty.entries.forEach { difficulty ->
                    FilterChip(
                        selected = selectedDifficulty == difficulty,
                        onClick = {
                            onDifficultyChange(difficulty)
                        },
                        enabled = enabled,
                        label = {
                            Text(difficulty.displayName)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CategorySection(
    selectedCategory: TriviaCategory,
    enabled: Boolean,
    onCategoryChange: (TriviaCategory) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Clasifica el tema de la pregunta para que sea más fácil revisarla.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TriviaCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            onCategoryChange(category)
                        },
                        enabled = enabled,
                        label = {
                            Text(category.displayName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplanationField(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = {
            Text("Explicación opcional")
        },
        placeholder = {
            Text("Ej: El personaje cumple ese rol durante la historia.")
        },
        supportingText = {
            Text(
                text = "Ayuda al moderador y al jugador a entender por qué esa opción es correcta."
            )
        },
        minLines = 2,
        maxLines = 4
    )
}

@Composable
private fun SubmitActions(
    isSubmitting: Boolean,
    hasSuccessMessage: Boolean,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text("Enviando...")
            } else {
                Text(
                    text = if (hasSuccessMessage) {
                        "Enviar otra pregunta"
                    } else {
                        "Enviar pregunta"
                    }
                )
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            Text("Volver")
        }
    }
}