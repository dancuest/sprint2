package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriviaPlayScreen(
    animeId: Long,
    onBack: () -> Unit,
    onGoToHome: () -> Unit,
    onGoToTrivia: () -> Unit,
    appContainer: AppContainer = DefaultAppContainer(),
    viewModel: TriviaPlayViewModel = viewModel(
        factory = TriviaPlayViewModel.provideFactory(
            animeId = animeId,
            animeRepository = appContainer.animeRepository,
            triviaRepository = appContainer.triviaRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Trivia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is TriviaPlayUiState.Loading -> TriviaPlayLoading(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
            is TriviaPlayUiState.Error -> TriviaPlayError(
                message = state.message,
                onRetry = viewModel::tryAgain,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
            is TriviaPlayUiState.Success -> TriviaPlayContent(
                state = state.state,
                onDifficultySelected = viewModel::selectDifficulty,
                onAnswer = viewModel::answerQuestion,
                onNext = viewModel::goToNextQuestion,
                onRestart = viewModel::restart,
                onGoToHome = onGoToHome,
                onGoToTrivia = onGoToTrivia,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
private fun TriviaPlayContent(
    state: TriviaPlayState,
    onDifficultySelected: (TriviaDifficulty) -> Unit,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onGoToHome: () -> Unit,
    onGoToTrivia: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimeTriviaHeader(anime = state.anime)
        DifficultySelector(
            selectedDifficulty = state.difficulty,
            onDifficultySelected = onDifficultySelected
        )
        when {
            state.questions.isEmpty() -> TriviaInstructions()
            state.finished -> TriviaResultCard(
                state = state,
                onRestart = onRestart,
                onGoToHome = onGoToHome,
                onGoToTrivia = onGoToTrivia
            )
            else -> TriviaQuestionCard(
                state = state,
                onAnswer = onAnswer,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun AnimeTriviaHeader(anime: Anime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(modifier = Modifier.height(220.dp)) {
            AsyncImage(
                model = anime.coverImageUrl,
                contentDescription = "Portada de ${anime.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = anime.genres.joinToString { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySelector(
    selectedDifficulty: TriviaDifficulty?,
    onDifficultySelected: (TriviaDifficulty) -> Unit
) {
    Column {
        Text(
            text = "Elige la dificultad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TriviaDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = selectedDifficulty == difficulty,
                    onClick = { onDifficultySelected(difficulty) },
                    label = {
                        Column {
                            Text(text = difficulty.displayName)
                            Text(
                                text = difficulty.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingIcon = if (selectedDifficulty == difficulty) {
                        {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun TriviaInstructions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "¿Listo para jugar?",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Selecciona una dificultad para desbloquear una trivia cultural con 3 preguntas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TriviaQuestionCard(
    state: TriviaPlayState,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit
) {
    val question = state.currentQuestion ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Pregunta ${state.currentIndex + 1} de ${state.totalQuestions}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium
            )
            question.options.forEachIndexed { index, option ->
                TriviaAnswerOption(
                    text = option,
                    selected = state.selectedAnswer == index,
                    isCorrect = state.selectedAnswer != null && index == question.correctAnswerIndex,
                    isIncorrect = state.selectedAnswer == index && index != question.correctAnswerIndex,
                    enabled = state.selectedAnswer == null,
                    onClick = { onAnswer(index) }
                )
            }
            if (state.selectedAnswer != null) {
                val correct = state.isAnswerCorrect == true
                val feedbackText = if (correct) {
                    "¡Correcto! ${question.feedback}"
                } else {
                    "Respuesta incorrecta. ${question.feedback}"
                }
                Text(
                    text = feedbackText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                val isLast = state.currentIndex >= state.totalQuestions - 1
                Button(
                    onClick = onNext,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLast) "Finalizar" else "Siguiente pregunta")
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Puntuación actual: ${state.score}/${state.totalQuestions}",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TriviaAnswerOption(
    text: String,
    selected: Boolean,
    isCorrect: Boolean,
    isIncorrect: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
        isIncorrect -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isCorrect -> MaterialTheme.colorScheme.onTertiaryContainer
        isIncorrect -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        onClick = { if (enabled) onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun TriviaResultCard(
    state: TriviaPlayState,
    onRestart: () -> Unit,
    onGoToHome: () -> Unit,
    onGoToTrivia: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Resultado final",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${state.score} de ${state.totalQuestions} respuestas correctas",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Tu puntuación se ha guardado para este anime",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onGoToHome, modifier = Modifier.fillMaxWidth()) {
                Text("Volver al Home")
            }
            OutlinedButton(onClick = onGoToTrivia, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a trivias")
            }
            TextButton(onClick = onRestart) {
                Text("Jugar de nuevo")
            }
        }
    }
}

@Composable
private fun TriviaPlayLoading(modifier: Modifier = Modifier) {
    BoxWithCenteredContent(modifier) {
        CircularProgressIndicator()
    }
}

@Composable
private fun TriviaPlayError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithCenteredContent(modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = message, textAlign = TextAlign.Center)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun BoxWithCenteredContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun TriviaPlayPreview() {
    AnimeDevTheme {
        Surface {
            TriviaPlayContent(
                state = TriviaPlayState(
                    anime = FakeDataSource.heroAnime,
                    difficulty = TriviaDifficulty.MEDIUM,
                    questions = FakeDataSource.animeCatalog.take(1).flatMap { anime ->
                        listOf(
                            TriviaQuestion(
                                id = "1",
                                animeId = anime.id,
                                difficulty = TriviaDifficulty.MEDIUM,
                                question = "¿Cuál es el título original?",
                                options = listOf("Kimetsu no Yaiba", "Vinland Saga", "Monster"),
                                correctAnswerIndex = 0,
                                feedback = "Se conoce como Kimetsu no Yaiba"
                            )
                        )
                    },
                    currentIndex = 0,
                    selectedAnswer = 0,
                    isAnswerCorrect = true,
                    score = 1
                ),
                onDifficultySelected = {},
                onAnswer = {},
                onNext = {},
                onRestart = {},
                onGoToHome = {},
                onGoToTrivia = {}
            )
        }
    }
}
