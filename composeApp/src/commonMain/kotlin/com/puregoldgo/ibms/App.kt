package com.puregoldgo.ibms

import androidx.compose.runtime.Composable
import com.puregoldgo.ibms.di.appModules
import com.puregoldgo.ibms.ui.adaptive.ProvideWindowSizeClass
import com.puregoldgo.ibms.ui.navigation.AppNavigation
import com.puregoldgo.ibms.ui.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

/**
 * Root composable — initializes Koin and hosts the app navigation.
 */
@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(appModules)
            }
        ),
        content = {
            AppTheme {
                ProvideWindowSizeClass {
                    AppNavigation()
                }
            }
        },
    )
}
