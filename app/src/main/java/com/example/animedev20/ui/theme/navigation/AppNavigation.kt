package com.example.animedev20.ui.theme.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object AuthWelcome : Screen("auth/welcome", "Acceso", Icons.Filled.AccountCircle)
    object Login : Screen("auth/login", "Login", Icons.Filled.AccountCircle)
    object Register : Screen("auth/register", "Registro", Icons.Filled.AccountCircle)
    object ForgotPassword : Screen("auth/forgot-password", "Recuperar", Icons.Filled.AccountCircle)
    object ResetPassword : Screen("auth/reset-password", "Restablecer", Icons.Filled.AccountCircle)

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