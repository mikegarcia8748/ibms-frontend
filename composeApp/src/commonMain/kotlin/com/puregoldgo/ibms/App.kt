package com.puregoldgo.ibms

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.di.appModules
import com.puregoldgo.ibms.ui.adaptive.ProvideWindowSizeClass
import com.puregoldgo.ibms.ui.component.GlobalLoadingBar
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
                    Box {
                        AppNavigation()
                        // Ambient in-flight bar, pinned to the top over whatever
                        // screen is showing.
                        GlobalLoadingBar(Modifier.align(Alignment.TopCenter))
                    }
                }
            }
        },
    )
}
