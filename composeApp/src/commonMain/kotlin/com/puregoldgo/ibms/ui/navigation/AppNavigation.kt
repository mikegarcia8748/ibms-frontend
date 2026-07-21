package com.puregoldgo.ibms.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.puregoldgo.core.network.SessionEvents
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.core.storage.SessionStore
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.wireValue
import com.puregoldgo.ibms.ui.screen.access.NoAccessScreen
import com.puregoldgo.ibms.ui.screen.dashboard.DashboardScreen
import com.puregoldgo.ibms.ui.screen.auth.LoginScreen
import com.puregoldgo.ibms.ui.screen.auth.SetPasswordScreen
import com.puregoldgo.ibms.ui.screen.provider.ProviderFormScreen
import com.puregoldgo.ibms.ui.screen.provider.ProviderListScreen
import com.puregoldgo.ibms.ui.screen.splash.SplashScreen

/**
 * Root navigation host — defines the app's navigation graph using Navigation 3.
 *
 * The back stack is a user-owned list of [Route] keys. Navigating to a
 * destination is as simple as adding a key; going back removes the last key.
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navSavedStateConfig, Route.Splash)

    /** Replaces the whole stack — used whenever session state changes underneath it. */
    fun resetTo(route: NavKey) {
        backStack.clear()
        backStack.add(route)
    }

    /**
     * Where "home" is depends on who just signed in.
     *
     * Read at navigation time rather than captured, because the three callers
     * below all arrive right after `CurrentUserStore` was written. This picks a
     * landing screen and nothing more — every screen it leads to is still
     * subject to the server's own role checks.
     */
    fun homeRoute(): NavKey =
        if (CurrentUserStore.role == Role.SYSADMIN.wireValue) {
            Route.Dashboard
        } else {
            Route.ProviderList
        }

    // A restored back stack can point deep into the app, but the access token
    // only ever lived in memory — so after process death those screens would be
    // drawn with no session behind them. Sending a cold start back through the
    // splash puts the session question first, every time.
    LaunchedEffect(Unit) {
        if (SessionStore.accessToken() == null && backStack.lastOrNull() != Route.Splash) {
            resetTo(Route.Splash)
        }
    }

    // Refresh failures surface from inside the HTTP client, below any screen.
    // Whichever one is showing, the answer is the same: back to sign-in.
    LaunchedEffect(Unit) {
        SessionEvents.expired.collect { resetTo(Route.Login) }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Route.Splash> {
                SplashScreen(
                    onAuthenticated = { resetTo(homeRoute()) },
                    onNoAccess = { reason -> resetTo(Route.NoAccess(reason)) },
                    onUnauthenticated = { resetTo(Route.Login) },
                )
            }
            entry<Route.Login> {
                LoginScreen(
                    // Replace rather than push: an authenticated user has no
                    // business navigating back into the sign-in form.
                    onLoginSuccess = { resetTo(homeRoute()) },
                    onNoAccess = { reason -> resetTo(Route.NoAccess(reason)) },
                    // The challenge stays in ChallengeHolder — this route is a
                    // destination, never a carrier for the token.
                    onPasswordChangeRequired = { resetTo(Route.SetPassword) },
                )
            }
            entry<Route.SetPassword> {
                SetPasswordScreen(
                    onPasswordSet = { resetTo(homeRoute()) },
                    onNoAccess = { reason -> resetTo(Route.NoAccess(reason)) },
                    onBackToLogin = { resetTo(Route.Login) },
                )
            }
            entry<Route.NoAccess> { key ->
                NoAccessScreen(
                    reason = key.reason,
                    onSignedOut = { resetTo(Route.Login) },
                )
            }
            entry<Route.Dashboard> {
                DashboardScreen(
                    onSignedOut = { resetTo(Route.Login) },
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
