package com.example.animedev20.ui.theme.data.trivia

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion

object AnimeTriviaQuestionFactory {

    private const val EASY_PLAY_QUESTION_COUNT = 3
    private const val MEDIUM_PLAY_QUESTION_COUNT = 5
    private const val HARD_PLAY_QUESTION_COUNT = 8

    private const val EASY_BANK_TARGET_COUNT = 5
    private const val MEDIUM_BANK_TARGET_COUNT = 7
    private const val HARD_BANK_TARGET_COUNT = 10

    fun questionCountForDifficulty(difficulty: TriviaDifficulty): Int {
        return playQuestionCountForDifficulty(difficulty)
    }

    fun playQuestionCountForDifficulty(difficulty: TriviaDifficulty): Int {
        return when (difficulty) {
            TriviaDifficulty.EASY -> EASY_PLAY_QUESTION_COUNT
            TriviaDifficulty.MEDIUM -> MEDIUM_PLAY_QUESTION_COUNT
            TriviaDifficulty.HARD -> HARD_PLAY_QUESTION_COUNT
        }
    }

    fun bankTargetQuestionCountForDifficulty(difficulty: TriviaDifficulty): Int {
        return when (difficulty) {
            TriviaDifficulty.EASY -> EASY_BANK_TARGET_COUNT
            TriviaDifficulty.MEDIUM -> MEDIUM_BANK_TARGET_COUNT
            TriviaDifficulty.HARD -> HARD_BANK_TARGET_COUNT
        }
    }

    fun build(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion> {
        return emptyList()
    }
}