package com.example.animedev20.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.animedev20.ui.theme.data.DefaultAppContainer
import com.example.animedev20.ui.theme.data.remote.AuthTokenStore
import com.example.animedev20.ui.theme.navigation.AppNavHost
import com.example.animedev20.ui.theme.navigation.BottomNavigationBar
import com.example.animedev20.ui.theme.navigation.Screen
import com.example.animedev20.ui.theme.theme.AnimeDevTheme

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
    val context = LocalContext.current.applicationContext
    val tokenStore = remember(context) { AuthTokenStore(context) }
    val sessionFingerprint by tokenStore.observeSessionFingerprint()
        .collectAsState(initial = tokenStore.getSessionFingerprint())

    key(sessionFingerprint) {
        val navController = rememberNavController()
        val appContainer = remember(context, sessionFingerprint) {
            DefaultAppContainer(context = context)
        }

        val startDestination by produceState<String?>(
            initialValue = null,
            key1 = sessionFingerprint
        ) {
            val destination = if (sessionFingerprint == null) {
                Screen.AuthWelcome.route
            } else {
                runCatching {
                    val settings = appContainer.userRepository.getUserSettings()
                    if (settings.hasCompletedOnboarding) {
                        Screen.Home.route
                    } else {
                        Screen.Onboarding.route
                    }
                }.getOrElse {
                    Screen.AuthWelcome.route
                }
            }

            value = destination
        }

        if (startDestination == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                    startDestination = startDestination!!,
                    appContainer = appContainer
                )
            }
        }
    }
}