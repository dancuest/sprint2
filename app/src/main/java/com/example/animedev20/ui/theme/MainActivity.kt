package com.example.animedev20.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.CircularProgressIndicator
import com.example.animedev20.ui.theme.navigation.AppNavHost
import com.example.animedev20.ui.theme.navigation.BottomNavigationBar
import com.example.animedev20.ui.theme.navigation.Screen
import com.example.animedev20.ui.theme.theme.AnimeDevTheme
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimeDevTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val startDestination by produceState<String?>(initialValue = null) {
        val destination = runCatching { FakeUserRepositoryImpl.getUserSettings() }
            .map { settings ->
                if (settings.hasCompletedOnboarding) Screen.Home.route else Screen.Onboarding.route
            }
            .getOrElse { Screen.Home.route }
        value = destination
    }
    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val shouldShowBottomBar = when (navBackStackEntry?.destination?.route) {
            Screen.Home.route,
            Screen.Favorites.route,
            Screen.Trivia.route,
            Screen.Settings.route,
            Screen.Profile.route -> true

            else -> false
        }
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                startDestination = startDestination!!
            )
        }
    }
}
