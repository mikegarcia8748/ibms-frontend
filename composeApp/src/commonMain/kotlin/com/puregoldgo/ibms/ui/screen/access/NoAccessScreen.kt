package com.puregoldgo.ibms.ui.screen.access

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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.no_access_inactive
import ibmsispbillingmanagementsystem.composeapp.generated.resources.no_access_pending
import ibmsispbillingmanagementsystem.composeapp.generated.resources.no_access_sign_out
import ibmsispbillingmanagementsystem.composeapp.generated.resources.no_access_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Where an authenticated account with nothing to do lands.
 *
 * Two ways to get here, both legitimate: a freshly provisioned account still
 * carries the default `pending` role, and an account deactivated mid-session
 * keeps a valid token until it expires. Either way the answer is the same —
 * explain, and offer the one action that helps.
 */
@Composable
fun NoAccessScreen(
    reason: NoAccessReason,
    onSignedOut: () -> Unit,
    viewModel: NoAccessViewModel = koinViewModel(),
) {
    NoAccessContent(
        reason = reason,
        onSignOut = {
            viewModel.onSignOut()
            onSignedOut()
        },
    )
}

@Composable
private fun NoAccessContent(
    reason: NoAccessReason,
    onSignOut: () -> Unit,
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
                text = stringResource(Res.string.no_access_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimensions.viewPadding12))

            Text(
                text = when (reason) {
                    NoAccessReason.PendingRole -> stringResource(Res.string.no_access_pending)
                    NoAccessReason.Deactivated -> stringResource(Res.string.no_access_inactive)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimensions.viewPadding24))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.viewHeight48),
                onClick = onSignOut,
            ) {
                Text(stringResource(Res.string.no_access_sign_out))
            }
        }
    }
}

@Preview(name = "No access", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun NoAccessContentWidePreview() {
    AppTheme { NoAccessContent(reason = NoAccessReason.PendingRole, onSignOut = {}) }
}

@Preview(name = "No access", group = "MobileApp")
@Composable
private fun NoAccessContentPreview() {
    AppTheme { NoAccessContent(reason = NoAccessReason.Deactivated, onSignOut = {}) }
}
