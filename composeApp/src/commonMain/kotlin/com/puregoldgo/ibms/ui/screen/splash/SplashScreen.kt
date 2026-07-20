package com.puregoldgo.ibms.ui.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.ui.screen.access.NoAccessReason
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.splash_restoring_session
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * What a launch shows while the stored session is being resumed. Brief by
 * design: it exists so the answer to "are you still signed in?" arrives before
 * the UI commits to one, not as a branded pause.
 */
@Composable
fun SplashScreen(
    onAuthenticated: () -> Unit,
    onNoAccess: (NoAccessReason) -> Unit,
    onUnauthenticated: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SplashUiEvent.NavigateToHome -> onAuthenticated()
                is SplashUiEvent.NavigateToNoAccess -> onNoAccess(event.reason)
                is SplashUiEvent.NavigateToLogin -> onUnauthenticated()
            }
        }
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.img_puregold_logo),
                contentDescription = stringResource(Res.string.login_logo_content_description),
                modifier = Modifier.size(Dimensions.viewSize80),
            )

            Spacer(Modifier.height(Dimensions.viewPadding24))

            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.viewSize24),
                strokeWidth = Dimensions.viewStroke2,
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            Text(
                text = stringResource(Res.string.splash_restoring_session),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Splash", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun SplashContentWidePreview() {
    AppTheme { SplashContent() }
}

@Preview(name = "Splash", group = "MobileApp")
@Composable
private fun SplashContentPreview() {
    AppTheme { SplashContent() }
}
