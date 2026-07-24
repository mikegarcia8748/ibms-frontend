package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_offline_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_offline_title
import org.jetbrains.compose.resources.stringResource

/**
 * A banner that explains a dialog is not wired to an endpoint yet.
 *
 * Shown in forms that are finished visually but still awaiting their backend
 * pass, so the user knows the submit button is intentionally inert.
 */
@Composable
internal fun NotConnectedBanner() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(Dimensions.viewPadding16),
    ) {
        Text(
            text = stringResource(Res.string.secretary_dialog_offline_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.secretary_dialog_offline_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
