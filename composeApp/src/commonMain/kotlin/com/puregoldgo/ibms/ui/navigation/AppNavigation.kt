package com.puregoldgo.ibms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.puregoldgo.ibms.ui.screen.auth.LoginScreen
import com.puregoldgo.ibms.ui.screen.provider.ProviderFormScreen
import com.puregoldgo.ibms.ui.screen.provider.ProviderListScreen

/**
 * Root navigation host — defines the app's navigation graph using Navigation 3.
 *
 * The back stack is a user-owned list of [Route] keys. Navigating to a
 * destination is as simple as adding a key; going back removes the last key.
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navSavedStateConfig, Route.Login)

    val navController = rememberNavController()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Route.Login> {
                LoginScreen(
                    onLoginSuccess = {
                        // Replace Login with ProviderList so the user cannot
                        // navigate back to the login screen after authenticating.
                        backStack.clear()
                        backStack.add(Route.ProviderList)
                    },
                )
            }
            entry<Route.ProviderList> {
                ProviderListScreen(
                    onNavigateToForm = { providerId ->
                        backStack.add(Route.ProviderForm(providerId = providerId))
                    },
                )
            }
            entry<Route.ProviderForm> { key ->
                ProviderFormScreen(
                    existingProvider = null,
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
