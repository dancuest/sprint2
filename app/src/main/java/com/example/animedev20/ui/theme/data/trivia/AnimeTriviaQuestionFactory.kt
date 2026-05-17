package com.example.animedev20.ui.theme.data.trivia

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion

object AnimeTriviaQuestionFactory {

    private const val EASY_QUESTION_COUNT = 5
    private const val MEDIUM_QUESTION_COUNT = 7
    private const val HARD_QUESTION_COUNT = 10

    fun questionCountForDifficulty(difficulty: TriviaDifficulty): Int {
        return when (difficulty) {
            TriviaDifficulty.EASY -> EASY_QUESTION_COUNT
            TriviaDifficulty.MEDIUM -> MEDIUM_QUESTION_COUNT
            TriviaDifficulty.HARD -> HARD_QUESTION_COUNT
        }
    }

    fun build(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion> {
        return emptyList()
    }
}