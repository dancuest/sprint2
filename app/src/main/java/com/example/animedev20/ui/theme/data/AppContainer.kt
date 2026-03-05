package com.example.animedev20.ui.theme.data

import android.content.Context
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.data.remote.AnimeApiFactory
import com.example.animedev20.ui.theme.data.remote.ApiConfig
import com.example.animedev20.ui.theme.data.remote.auth.AuthApi
import com.example.animedev20.ui.theme.data.remote.session.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.users.UsersApi
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FavoritesTriviaRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
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
    private val tokenStore = context?.let { AuthTokenStore(it) }

    private val retrofit = tokenStore?.let {
        AnimeApiFactory.createRetrofit(tokenStore = it, baseUrl = baseUrl)
    }

    private val animeApi: AnimeApi = retrofit?.create(AnimeApi::class.java)
        ?: AnimeApiFactory.create(baseUrl)

    private val authApi: AuthApi? = retrofit?.create(AuthApi::class.java)
    private val usersApi: UsersApi? = retrofit?.create(UsersApi::class.java)

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

    override val userRepository: UserRepository = if (useRemote && tokenStore != null && authApi != null && usersApi != null) {
        RemoteUserRepositoryImpl(
            authApi = authApi,
            usersApi = usersApi,
            animeApi = animeApi,
            tokenStore = tokenStore
        )
    } else {
        FakeUserRepositoryImpl.apply {
            context?.let(::initialize)
        }
    }
}
