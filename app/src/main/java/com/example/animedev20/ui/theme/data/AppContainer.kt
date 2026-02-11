package com.example.animedev20.ui.theme.data

import com.example.animedev20.ui.theme.data.remote.AnimeApiFactory
import com.example.animedev20.ui.theme.data.remote.ApiConfig
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeTriviaRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteAnimeRepositoryImpl
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository

const val USE_REMOTE = true

interface AppContainer {
    val animeRepository: AnimeRepository
    val favoritesRepository: FavoritesRepository
    val triviaRepository: TriviaRepository
    val userRepository: UserRepository
}

class DefaultAppContainer(
    baseUrl: String = ApiConfig.baseUrl,
    useRemote: Boolean = USE_REMOTE
) : AppContainer {
    private val animeApi = AnimeApiFactory.create(baseUrl)

    override val animeRepository: AnimeRepository = if (useRemote) {
        RemoteAnimeRepositoryImpl(animeApi)
    } else {
        FakeAnimeRepositoryImpl()
    }

    override val favoritesRepository: FavoritesRepository = FakeFavoritesRepositoryImpl
    override val triviaRepository: TriviaRepository = FakeTriviaRepositoryImpl
    override val userRepository: UserRepository = FakeUserRepositoryImpl
}
