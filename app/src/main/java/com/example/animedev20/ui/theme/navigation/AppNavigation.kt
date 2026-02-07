package com.example.animedev20.ui.theme.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Favorites : Screen("favorites", "Favoritos", Icons.Filled.Favorite)
    object Trivia : Screen("trivia", "Trivias", Icons.Outlined.HelpOutline)
    object Settings : Screen("settings", "Ajustes", Icons.Filled.Settings)
    object Profile : Screen("profile", "Perfil", Icons.Filled.AccountCircle)
    object Onboarding : Screen("onboarding/preferences", "Preferencias", Icons.Filled.Home)
    object AnimeDetail : Screen("anime/{animeId}", "Detalle", Icons.Filled.Home) {
        fun createRoute(animeId: Long) = "anime/$animeId"
    }
    object TriviaPlay : Screen("trivia/play/{animeId}", "Jugar Trivia", Icons.Outlined.HelpOutline) {
        fun createRoute(animeId: Long) = "trivia/play/$animeId"
    }
}