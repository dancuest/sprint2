package com.example.animedev20.ui.theme.data

import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository

interface AppContainer {
    val animeRepository: AnimeRepository
    val favoritesRepository: FavoritesRepository
    val triviaRepository: TriviaRepository
    val userRepository: UserRepository
}