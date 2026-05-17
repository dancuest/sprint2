package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.remote.AdminAnimeSearchResponse
import com.example.animedev20.ui.theme.data.remote.AdminTriviaQuestionResponse
import com.example.animedev20.ui.theme.data.remote.TriviaQuestionReportResponse
import com.example.animedev20.ui.theme.data.remote.UpsertTriviaQuestionRequest
import com.example.animedev20.ui.theme.data.repository.TriviaAdminRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminTriviaViewModel(
    private val repository: TriviaAdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminTriviaUiState())
    val uiState: StateFlow<AdminTriviaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refreshCurrentSection()
    }

    fun selectSection(section: AdminTriviaSection) {
        _uiState.value = _uiState.value.copy(selectedSection = section)
        refreshCurrentSection()
    }

    fun refreshCurrentSection() {
        when (_uiState.value.selectedSection) {
            AdminTriviaSection.REQUESTS -> loadQuestionRequests()
            AdminTriviaSection.REPORTS -> loadQuestionReports()
            AdminTriviaSection.BANK -> {
                _uiState.value.selectedAnime?.id?.let { animeId ->
                    loadQuestionsByAnime(animeId)
                }
            }
        }
    }

    fun loadQuestionRequests() {
        viewModelScope.launch {
            setLoading(true)

            runCatching {
                repository.getQuestionRequests()
            }.onSuccess { requests ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    questionRequests = requests
                )
            }.onFailure { error ->
                showError(error, "No fue posible cargar las solicitudes.")
            }
        }
    }

    fun approveQuestionRequest(questionId: String) {
        runAction("Solicitud aprobada.") {
            repository.approveQuestionRequest(questionId)
            val requests = repository.getQuestionRequests()
            _uiState.value = _uiState.value.copy(questionRequests = requests)
        }
    }

    fun rejectQuestionRequest(questionId: String) {
        runAction("Solicitud rechazada.") {
            repository.rejectQuestionRequest(questionId)
            val requests = repository.getQuestionRequests()
            _uiState.value = _uiState.value.copy(questionRequests = requests)
        }
    }

    fun setReportStatusFilter(status: String) {
        _uiState.value = _uiState.value.copy(reportStatusFilter = status)
        loadQuestionReports()
    }

    fun loadQuestionReports() {
        viewModelScope.launch {
            setLoading(true)

            runCatching {
                repository.getQuestionReports(_uiState.value.reportStatusFilter)
            }.onSuccess { reports ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    questionReports = reports
                )
            }.onFailure { error ->
                showError(error, "No fue posible cargar las quejas.")
            }
        }
    }

    fun openResolveReportEditor(report: TriviaQuestionReportResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isActionLoading = true,
                message = null
            )

            runCatching {
                repository.getQuestionById(report.questionId)
            }.onSuccess { question ->
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    editorVisible = true,
                    editorForm = AdminQuestionFormState(
                        id = question.id,
                        animeId = question.animeId ?: report.animeId,
                        externalAnimeId = question.externalAnimeId ?: report.animeId?.toString(),
                        question = question.question,
                        options = normalizeOptions(question.options),
                        correctAnswerIndex = question.correctAnswerIndex.coerceIn(0, 3),
                        difficulty = question.difficulty,
                        category = question.category ?: "GENERAL",
                        explanation = question.explanation ?: "",
                        status = question.status ?: "APPROVED",
                        resolveReportId = report.id
                    )
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    message = error.message ?: "No fue posible abrir la pregunta reportada."
                )
            }
        }
    }

    fun deleteReport(reportId: String) {
        updateReportStatus(
            reportId = reportId,
            status = "DELETED",
            message = "Queja enviada a eliminadas."
        )
    }

    fun rejectReport(reportId: String) {
        updateReportStatus(
            reportId = reportId,
            status = "REJECTED",
            message = "Queja rechazada."
        )
    }

    private fun updateReportStatus(
        reportId: String,
        status: String,
        message: String
    ) {
        runAction(message) {
            repository.updateQuestionReport(
                reportId = reportId,
                status = status
            )

            val reports = repository.getQuestionReports(_uiState.value.reportStatusFilter)
            _uiState.value = _uiState.value.copy(questionReports = reports)
        }
    }

    fun updateAnimeQuery(query: String) {
        _uiState.value = _uiState.value.copy(animeQuery = query)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)

            if (query.trim().length < 2) {
                _uiState.value = _uiState.value.copy(animeResults = emptyList())
                return@launch
            }

            runCatching {
                repository.searchAnime(query)
            }.onSuccess { results ->
                _uiState.value = _uiState.value.copy(animeResults = results)
            }.onFailure { error ->
                showError(error, "No fue posible buscar animes.")
            }
        }
    }

    fun selectAnime(anime: AdminAnimeSearchResponse) {
        _uiState.value = _uiState.value.copy(
            selectedAnime = anime,
            animeQuery = anime.title,
            animeResults = emptyList()
        )

        loadQuestionsByAnime(anime.id)
    }

    fun loadQuestionsByAnime(animeId: Long) {
        viewModelScope.launch {
            setLoading(true)

            runCatching {
                repository.getQuestionsByAnime(animeId)
            }.onSuccess { questions ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    animeQuestions = questions
                )
            }.onFailure { error ->
                showError(error, "No fue posible cargar las preguntas del anime.")
            }
        }
    }

    fun openCreateQuestionEditor() {
        val selectedAnime = _uiState.value.selectedAnime

        if (selectedAnime == null) {
            _uiState.value = _uiState.value.copy(
                message = "Selecciona un anime antes de crear una pregunta."
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            editorVisible = true,
            editorForm = AdminQuestionFormState(
                animeId = selectedAnime.id,
                externalAnimeId = selectedAnime.externalApiId ?: selectedAnime.id.toString(),
                status = "APPROVED"
            )
        )
    }

    fun openEditQuestionEditor(question: AdminTriviaQuestionResponse) {
        _uiState.value = _uiState.value.copy(
            editorVisible = true,
            editorForm = AdminQuestionFormState(
                id = question.id,
                animeId = question.animeId,
                externalAnimeId = question.externalAnimeId,
                question = question.question,
                options = normalizeOptions(question.options),
                correctAnswerIndex = question.correctAnswerIndex.coerceIn(0, 3),
                difficulty = question.difficulty,
                category = question.category ?: "GENERAL",
                explanation = question.explanation ?: "",
                status = question.status ?: "APPROVED"
            )
        )
    }

    fun closeEditor() {
        _uiState.value = _uiState.value.copy(
            editorVisible = false,
            editorForm = AdminQuestionFormState()
        )
    }

    fun updateEditorQuestion(value: String) {
        updateForm { it.copy(question = value) }
    }

    fun updateEditorOption(index: Int, value: String) {
        val current = normalizeOptions(_uiState.value.editorForm.options).toMutableList()
        current[index] = value
        updateForm { it.copy(options = current) }
    }

    fun updateEditorCorrectAnswer(index: Int) {
        updateForm { it.copy(correctAnswerIndex = index.coerceIn(0, 3)) }
    }

    fun updateEditorDifficulty(value: String) {
        updateForm { it.copy(difficulty = value) }
    }

    fun updateEditorCategory(value: String) {
        updateForm { it.copy(category = value) }
    }

    fun updateEditorExplanation(value: String) {
        updateForm { it.copy(explanation = value) }
    }

    fun updateEditorStatus(value: String) {
        updateForm { it.copy(status = value) }
    }

    fun saveEditorQuestion() {
        val form = _uiState.value.editorForm
        val existingQuestionId = form.id
        val reportIdToResolve = form.resolveReportId

        val validationError = validateForm(form)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(message = validationError)
            return
        }

        runAction(
            successMessage = if (reportIdToResolve != null) {
                "Pregunta corregida y queja resuelta."
            } else if (existingQuestionId == null) {
                "Pregunta creada correctamente."
            } else {
                "Pregunta actualizada correctamente."
            }
        ) {
            val request = UpsertTriviaQuestionRequest(
                animeId = form.animeId,
                externalAnimeId = form.externalAnimeId,
                question = form.question.trim(),
                options = normalizeOptions(form.options).map { it.trim() },
                correctAnswerIndex = form.correctAnswerIndex,
                difficulty = form.difficulty,
                category = form.category.trim().ifBlank { "GENERAL" },
                explanation = form.explanation.trim(),
                status = form.status
            )

            if (existingQuestionId == null) {
                repository.createQuestionAsAdmin(request)
            } else {
                repository.updateQuestion(existingQuestionId, request)
            }

            if (reportIdToResolve != null) {
                repository.updateQuestionReport(
                    reportId = reportIdToResolve,
                    status = "RESOLVED",
                    adminNote = "Pregunta corregida desde el panel de administración."
                )

                val reports = repository.getQuestionReports(_uiState.value.reportStatusFilter)
                _uiState.value = _uiState.value.copy(questionReports = reports)
            }

            form.animeId?.let { animeId ->
                val questions = repository.getQuestionsByAnime(animeId)
                _uiState.value = _uiState.value.copy(animeQuestions = questions)
            }

            closeEditor()
        }
    }

    fun deleteQuestion(questionId: String) {
        val animeId = _uiState.value.selectedAnime?.id

        runAction("Pregunta eliminada correctamente.") {
            repository.deleteQuestion(questionId)

            if (animeId != null) {
                val questions = repository.getQuestionsByAnime(animeId)
                _uiState.value = _uiState.value.copy(animeQuestions = questions)
            }
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun updateForm(block: (AdminQuestionFormState) -> AdminQuestionFormState) {
        _uiState.value = _uiState.value.copy(
            editorForm = block(_uiState.value.editorForm)
        )
    }

    private fun validateForm(form: AdminQuestionFormState): String? {
        if (form.animeId == null) return "Selecciona un anime."
        if (form.question.trim().length < 8) return "La pregunta está demasiado corta."

        val options = normalizeOptions(form.options)
        if (options.any { it.trim().isBlank() }) {
            return "Completa las 4 opciones."
        }

        if (form.correctAnswerIndex !in 0..3) {
            return "Selecciona la respuesta correcta."
        }

        if (form.explanation.trim().length < 8) {
            return "Agrega una explicación un poco más completa."
        }

        return null
    }

    private fun normalizeOptions(options: List<String>): List<String> {
        return List(4) { index -> options.getOrNull(index) ?: "" }
    }

    private fun setLoading(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoading = value,
            message = null
        )
    }

    private fun runAction(
        successMessage: String,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isActionLoading = true,
                message = null
            )

            runCatching {
                block()
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    message = successMessage
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    message = error.message ?: "No fue posible completar la acción."
                )
            }
        }
    }

    private fun showError(
        error: Throwable,
        fallback: String
    ) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isActionLoading = false,
            message = error.message ?: fallback
        )
    }

    companion object {
        fun provideFactory(
            repository: TriviaAdminRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminTriviaViewModel(repository) as T
            }
        }
    }
}