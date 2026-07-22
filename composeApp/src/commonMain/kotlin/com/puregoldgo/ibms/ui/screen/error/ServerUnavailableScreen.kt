package com.puregoldgo.ibms.ui.screen.error

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.server_unavailable_message
import ibmsispbillingmanagementsystem.composeapp.generated.resources.server_unavailable_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.server_unavailable_title
import org.jetbrains.compose.resources.stringResource

/**
 * Shown when a request to the backend gets no answer at all during the auth flow.
 *
 * A dropped connection is not a rejection — the session, if any, is still good and
 * the credentials are still valid. So this is a full-screen pause, not an error to
 * recover from: it explains the outage and offers the one action that helps, a
 * [onRetry] that re-runs whatever call could not reach the server.
 */
@Composable
fun ServerUnavailableContent(
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(Dimensions.viewPadding24),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = Dimensions.viewWidth420),
        ) {
            Text(
                text = stringResource(Res.string.server_unavailable_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimensions.viewPadding12))

            Text(
                text = stringResource(Res.string.server_unavailable_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimensions.viewPadding24))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.viewHeight48),
                onClick = onRetry,
            ) {
                Text(stringResource(Res.string.server_unavailable_retry))
            }
        }
    }
}

@Preview(name = "Server unavailable", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun ServerUnavailableContentWidePreview() {
    AppTheme { ServerUnavailableContent(onRetry = {}) }
}

@Preview(name = "Server unavailable", group = "MobileApp")
@Composable
private fun ServerUnavailableContentPreview() {
    AppTheme { ServerUnavailableContent(onRetry = {}) }
}
