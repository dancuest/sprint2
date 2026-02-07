package com.example.animedev20.ui.theme.domain.model.Trivias



data class TriviaQuestion(
                          val id: String,
                          val animeId: Long,
                          val difficulty: TriviaDifficulty,
                          val question: String,
                          val options: List<String>,
                          val correctAnswerIndex: Int,
                          val feedback: String
)