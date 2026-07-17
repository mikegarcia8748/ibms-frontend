package com.puregoldgo.ibms

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.puregoldgo.ibms.di.providerModule
import com.puregoldgo.ibms.presentation.provider.ProviderFormScreen
import com.puregoldgo.ibms.presentation.provider.ProviderListScreen
import org.koin.compose.KoinApplication
import org.koin.core.KoinApplication
import org.koin.dsl.koinConfiguration

/**
 * Root composable — initializes Koin and hosts the app navigation.
 */
@Composable
fun App() {
    KoinApplication(configuration = koinConfiguration(declaration = { modules(providerModule) }), content = {
        MaterialTheme {
            AppNavigation()
        }
    })
}

/**
 * Simple state-based navigation until a proper navigation library is added.
 */
@Composable
private fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.ProviderList) }

    when (val screen = currentScreen) {
        is Screen.ProviderList -> {
            ProviderListScreen(
                onNavigateToForm = { providerId ->
                    currentScreen = Screen.ProviderForm(providerId = providerId)
                },
            )
        }
        is Screen.ProviderForm -> {
            // For editing, we pass the provider if we have it cached.
            // In a real app, the form ViewModel would fetch by ID.
            ProviderFormScreen(
                existingProvider = null,
                onNavigateBack = { currentScreen = Screen.ProviderList },
            )
        }
    }
}

private sealed class Screen {
    data object ProviderList : Screen()
    data class ProviderForm(val providerId: String?) : Screen()
}
