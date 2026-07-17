package com.puregoldgo.ibms

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.puregoldgo.core.storage.TokenStorage
import com.puregoldgo.ibms.di.authModule
import com.puregoldgo.ibms.di.providerModule
import com.puregoldgo.ibms.presentation.auth.LoginScreen
import com.puregoldgo.ibms.presentation.provider.ProviderFormScreen
import com.puregoldgo.ibms.presentation.provider.ProviderListScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/**
 * Root composable — initializes Koin and hosts the app navigation.
 */
@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(authModule, providerModule) }),
        content = {
            MaterialTheme {
                AppNavigation()
            }
        },
    )
}

/**
 * Simple state-based navigation until a proper navigation library is added.
 * Gates access: shows LoginScreen when unauthenticated, ProviderList when authenticated.
 */
@Composable
private fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (val screen = currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginSuccess = { currentScreen = Screen.ProviderList },
            )
        }
        is Screen.ProviderList -> {
            ProviderListScreen(
                onNavigateToForm = { providerId ->
                    currentScreen = Screen.ProviderForm(providerId = providerId)
                },
            )
        }
        is Screen.ProviderForm -> {
            ProviderFormScreen(
                existingProvider = null,
                onNavigateBack = { currentScreen = Screen.ProviderList },
            )
        }
    }
}

private sealed class Screen {
    data object Login : Screen()
    data object ProviderList : Screen()
    data class ProviderForm(val providerId: String?) : Screen()
}
