package com.example.animedev20.ui.theme.data

import android.content.Context
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.data.remote.AnimeApiFactory
import com.example.animedev20.ui.theme.data.remote.ApiConfig
import com.example.animedev20.ui.theme.data.remote.AuthApiPlain
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.UsersApi
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FavoritesTriviaRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteUserRepositoryImpl
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
    private val context: Context? = null,
    baseUrl: String = ApiConfig.baseUrl,
    useRemote: Boolean = USE_REMOTE
) : AppContainer {
    private val appContext: Context? = context?.applicationContext

    private val tokenStore: AuthTokenStore? = appContext?.let(::AuthTokenStore)
    private val retrofit = tokenStore?.let { AnimeApiFactory.createRetrofit(baseUrl, it) }
    private val animeApi = retrofit?.create(AnimeApi::class.java)
    private val authApiPlain = retrofit?.create(AuthApiPlain::class.java)
    private val usersApi = retrofit?.create(UsersApi::class.java)

    override val animeRepository: AnimeRepository = if (useRemote && animeApi != null) {
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

    override val userRepository: UserRepository =
        if (useRemote && authApiPlain != null && usersApi != null && tokenStore != null && appContext != null) {
            RemoteUserRepositoryImpl(authApiPlain, usersApi, tokenStore, appContext).apply {
                initialize(appContext)
            }
        } else {
            FakeUserRepositoryImpl.apply {
                context?.let(::initialize)
            }
        }
}
