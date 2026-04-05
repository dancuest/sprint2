package com.example.animedev20.ui.theme.data

import android.content.Context
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.data.remote.AnimeApiFactory
import com.example.animedev20.ui.theme.data.remote.ApiConfig
import com.example.animedev20.ui.theme.data.remote.AuthApiPlain
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.data.remote.InteractionsApi
import com.example.animedev20.ui.theme.data.remote.UsersApi
import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FavoritesTriviaRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.NoOpInteractionRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteInteractionRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.RemoteUserRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.TrackingFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.TrackingTriviaRepositoryImpl
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

const val USE_REMOTE = true

interface AppContainer {
    val animeRepository: AnimeRepository
    val favoritesRepository: FavoritesRepository
    val triviaRepository: TriviaRepository
    val userRepository: UserRepository
    val interactionRepository: InteractionRepository
    val homeRefreshBus: HomeRefreshBus
    val usersApi: UsersApi? // ← expuesto para SettingsViewModel (cambio de contraseña)
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
    override val usersApi = retrofit?.create(UsersApi::class.java)
    private val interactionsApi = retrofit?.create(InteractionsApi::class.java)

    private val containerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val homeRefreshBus: HomeRefreshBus = HomeRefreshBus()

    override val interactionRepository: InteractionRepository =
        if (useRemote && interactionsApi != null) {
            RemoteInteractionRepositoryImpl(interactionsApi)
        } else {
            NoOpInteractionRepositoryImpl
        }

    override val animeRepository: AnimeRepository =
        if (useRemote && animeApi != null) {
            RemoteAnimeRepositoryImpl(animeApi)
        } else {
            FakeAnimeRepositoryImpl()
        }

    private val localFavoritesRepository: FavoritesRepository =
        if (useRemote && animeApi != null) {
            RemoteFavoritesRepositoryImpl(
                animeApi = animeApi,
                scope = containerScope
            )
        } else {
            FakeFavoritesRepositoryImpl.apply {
                context?.let(::initialize)
            }
        }

    override val favoritesRepository: FavoritesRepository = TrackingFavoritesRepositoryImpl(
        delegate = localFavoritesRepository,
        interactionRepository = interactionRepository,
        homeRefreshBus = homeRefreshBus
    )

    private val localTriviaRepository: TriviaRepository = FavoritesTriviaRepositoryImpl(
        favoritesRepository = favoritesRepository,
        animeRepository = animeRepository,
        context = context
    )

    override val triviaRepository: TriviaRepository = TrackingTriviaRepositoryImpl(
        delegate = localTriviaRepository,
        interactionRepository = interactionRepository,
        homeRefreshBus = homeRefreshBus
    )

    override val userRepository: UserRepository =
        if (
            useRemote &&
            authApiPlain != null &&
            usersApi != null &&
            tokenStore != null &&
            appContext != null &&
            animeApi != null
        ) {
            RemoteUserRepositoryImpl(
                authApiPlain,
                usersApi,
                animeApi,
                tokenStore,
                appContext,
                homeRefreshBus
            ).apply {
                initialize(appContext)
            }
        } else {
            FakeUserRepositoryImpl.apply {
                context?.let(::initialize)
            }
        }
}