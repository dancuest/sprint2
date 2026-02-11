package com.example.animedev20.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.animedev20.ui.theme.data.AppContainer
import com.example.animedev20.ui.theme.feature.animeinfo.ui.AnimeDetailScreen
import com.example.animedev20.ui.theme.feature.favorites.ui.FavoritesScreen
import com.example.animedev20.ui.theme.feature.home.ui.HomeScreen
import com.example.animedev20.ui.theme.feature.profile.ui.ProfileScreen
import com.example.animedev20.ui.theme.feature.settings.ui.SettingsScreen
import com.example.animedev20.ui.theme.feature.trivia.ui.TriviaPlayScreen
import com.example.animedev20.ui.theme.feature.trivia.ui.TriviaScreen
import com.example.animedev20.ui.theme.feature.onboarding.ui.OnboardingPreferencesRoute

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
        composable(Screen.Onboarding.route) {
            OnboardingPreferencesRoute(
                appContainer = appContainer,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
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
            FavoritesScreen(onAnimeSelected = { animeId ->
                navController.navigate(Screen.AnimeDetail.createRoute(animeId))
            })
        }
        composable(Screen.Trivia.route) {
            TriviaScreen(onPlayTrivia = { animeId ->
                navController.navigate(Screen.TriviaPlay.createRoute(animeId))
            })
        }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable(
            route = Screen.AnimeDetail.route,
            arguments = listOf(navArgument("animeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId") ?: return@composable
            AnimeDetailScreen(
                animeId = animeId,
                appContainer = appContainer,
                onBack = { navController.popBackStack() },
                onTriviaRequested = { targetAnimeId ->
                    navController.navigate(Screen.TriviaPlay.createRoute(targetAnimeId))
                }
            )
        }
        composable(
            route = Screen.TriviaPlay.route,
            arguments = listOf(navArgument("animeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId") ?: return@composable
            TriviaPlayScreen(
                animeId = animeId,
                appContainer = appContainer,
                onBack = { navController.popBackStack() },
                onGoToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onGoToTrivia = {
                    navController.navigate(Screen.Trivia.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}