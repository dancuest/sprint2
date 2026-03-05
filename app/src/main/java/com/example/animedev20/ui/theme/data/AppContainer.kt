package com.example.animedev20.ui.theme.data

import android.content.Context
import com.example.animedev20.ui.theme.data.remote.AnimeApiFactory
import com.example.animedev20.ui.theme.data.remote.ApiConfig
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FavoritesTriviaRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteAnimeRepositoryImpl
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import com.example.animedev20.ui.theme.data.remote.AuthApi
import com.example.animedev20.ui.theme.data.repository.RemoteUserRepositoryImpl

const val USE_REMOTE = true

interface AppContainer {
    val animeRepository: AnimeRepository
    val favoritesRepository: FavoritesRepository
    val triviaRepository: TriviaRepository
    val userRepository: UserRepository
}

class DefaultAppContainer(
    private val context: Context? = null,
    baseUrl: String = ApiConfig.baseUrl,
    useRemote: Boolean = USE_REMOTE
) : AppContainer {
    private val retrofit = com.example.animedev20.ui.theme.data.remote.AnimeApiFactory.createRetrofit(baseUrl)
    private val animeApi = retrofit.create(com.example.animedev20.ui.theme.data.remote.AnimeApi::class.java)
    private val authApi = retrofit.create(AuthApi::class.java)

    override val animeRepository: AnimeRepository = if (useRemote) {
        RemoteAnimeRepositoryImpl(animeApi)
    } else {
        FakeAnimeRepositoryImpl()
    }

    override val favoritesRepository: FavoritesRepository = FakeFavoritesRepositoryImpl.apply {
        context?.let(::initialize)
    }
    override val triviaRepository: TriviaRepository = FavoritesTriviaRepositoryImpl(
        favoritesRepository = favoritesRepository,
        animeRepository = animeRepository,
        context = context
    )
    override val userRepository: UserRepository = if (useRemote) {
        RemoteUserRepositoryImpl(authApi).apply {
            context?.let(::initialize)
        }
    } else {
        FakeUserRepositoryImpl.apply {
            context?.let(::initialize)
        }
    }
}
