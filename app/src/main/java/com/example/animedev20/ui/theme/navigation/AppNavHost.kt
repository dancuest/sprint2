package com.example.animedev20.ui.theme.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.domain.model.canModerateTrivia
import com.example.animedev20.ui.theme.feature.animeinfo.ui.AnimeDetailScreen
import com.example.animedev20.ui.theme.feature.auth.ui.AuthWelcomeScreen
import com.example.animedev20.ui.theme.feature.auth.ui.ForgotPasswordScreen
import com.example.animedev20.ui.theme.feature.auth.ui.LoginScreen
import com.example.animedev20.ui.theme.feature.auth.ui.RegisterScreen
import com.example.animedev20.ui.theme.feature.auth.ui.ResetPasswordScreen
import com.example.animedev20.ui.theme.feature.favorites.ui.FavoritesScreen
import com.example.animedev20.ui.theme.feature.home.ui.HomeScreen
import com.example.animedev20.ui.theme.feature.onboarding.ui.OnboardingPreferencesRoute
import com.example.animedev20.ui.theme.feature.profile.ui.ProfileScreen
import com.example.animedev20.ui.theme.feature.settings.ui.SettingsScreen
import com.example.animedev20.ui.theme.feature.trivia.ui.AddTriviaQuestionScreen
import com.example.animedev20.ui.theme.feature.trivia.ui.AdminTriviaScreen
import com.example.animedev20.ui.theme.feature.trivia.ui.TriviaPlayScreen
import com.example.animedev20.ui.theme.feature.trivia.ui.TriviaScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    appContainer: AppContainer,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.AuthWelcome.route) {
            AuthWelcomeScreen(
                appContainer = appContainer,
                onGoToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onAuthSuccessRoute = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.AuthWelcome.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                appContainer = appContainer,
                onAuthSuccessRoute = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.AuthWelcome.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onGoToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                appContainer = appContainer,
                onAuthSuccessRoute = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.AuthWelcome.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onGoToLogin = {
                    navController.popBackStack()
                },
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                appContainer = appContainer,
                onGoToReset = {
                    navController.navigate(Screen.ResetPassword.route)
                },
                onGoToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ResetPassword.route) {
            ResetPasswordScreen(
                appContainer = appContainer,
                onGoToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AuthWelcome.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingPreferencesRoute(
                appContainer = appContainer,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                appContainer = appContainer,
                onAnimeSelected = { animeId ->
                    navController.navigate(Screen.AnimeDetail.createRoute(animeId))
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                appContainer = appContainer,
                onAnimeSelected = { animeId ->
                    navController.navigate(Screen.AnimeDetail.createRoute(animeId))
                },
                onGoToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Trivia.route) {
            TriviaScreen(
                appContainer = appContainer,
                onPlayTrivia = { animeId ->
                    navController.navigate(Screen.TriviaPlay.createRoute(animeId))
                },
                onAddQuestion = { animeId ->
                    navController.navigate(Screen.AddTriviaQuestion.createRoute(animeId))
                },
                onOpenModeration = {
                    navController.navigate(Screen.AdminTrivia.route)
                },
                onGoToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                appContainer = appContainer,
                onLogoutRequest = {
                    navController.navigate(Screen.AuthWelcome.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                appContainer = appContainer,
                onGoToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onGoToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(
            route = Screen.AnimeDetail.route,
            arguments = listOf(
                navArgument("animeId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId")
                ?: return@composable

            AnimeDetailScreen(
                animeId = animeId,
                appContainer = appContainer,
                onBack = {
                    navController.popBackStack()
                },
                onTriviaRequested = { targetAnimeId ->
                    navController.navigate(Screen.TriviaPlay.createRoute(targetAnimeId))
                }
            )
        }

        composable(
            route = Screen.TriviaPlay.route,
            arguments = listOf(
                navArgument("animeId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId")
                ?: return@composable

            TriviaPlayScreen(
                animeId = animeId,
                appContainer = appContainer,
                onBack = {
                    navController.popBackStack()
                },
                onGoToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                onGoToTrivia = {
                    val returnedToTrivia = navController.popBackStack(
                        Screen.Trivia.route,
                        false
                    )

                    if (!returnedToTrivia) {
                        navController.navigate(Screen.Trivia.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onAddQuestion = {
                    navController.navigate(Screen.AddTriviaQuestion.createRoute(animeId))
                },
                onGoToAnimeInfo = {
                    navController.navigate(Screen.AnimeDetail.createRoute(animeId)) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.AddTriviaQuestion.route,
            arguments = listOf(
                navArgument("animeId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId")
                ?: return@composable

            AddTriviaQuestionScreen(
                animeId = animeId,
                appContainer = appContainer,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdminTrivia.route) {
            AdminTriviaGuardRoute(
                appContainer = appContainer,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun AdminTriviaGuardRoute(
    appContainer: AppContainer,
    onBack: () -> Unit
) {
    val accessState by produceState<AdminRouteAccessState>(
        initialValue = AdminRouteAccessState.Loading,
        key1 = appContainer
    ) {
        value = runCatching {
            appContainer.userRepository.getUserProfile()
        }.fold(
            onSuccess = { profile ->
                if (profile.email.isNotBlank() && profile.canModerateTrivia()) {
                    AdminRouteAccessState.Allowed
                } else {
                    AdminRouteAccessState.Denied
                }
            },
            onFailure = {
                AdminRouteAccessState.Denied
            }
        )
    }

    when (accessState) {
        AdminRouteAccessState.Loading -> AdminRouteLoadingScreen(
            onBack = onBack
        )

        AdminRouteAccessState.Denied -> AdminRouteDeniedScreen(
            onBack = onBack
        )

        AdminRouteAccessState.Allowed -> AdminTriviaScreen(
            appContainer = appContainer,
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRouteLoadingScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Panel de administración")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()

                Text(
                    text = "Validando permisos...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminRouteDeniedScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Panel de administración")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Acceso restringido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Este panel solo está disponible para perfiles administradores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(onClick = onBack) {
                    Text("Volver")
                }
            }
        }
    }
}

private sealed class AdminRouteAccessState {
    data object Loading : AdminRouteAccessState()
    data object Allowed : AdminRouteAccessState()
    data object Denied : AdminRouteAccessState()
}