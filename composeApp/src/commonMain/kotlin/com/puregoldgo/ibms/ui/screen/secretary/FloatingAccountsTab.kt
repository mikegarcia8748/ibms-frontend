package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_accounts_monthly_rate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_floating_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_floating_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_floating_title
import org.jetbrains.compose.resources.stringResource

/**
 * Accounts still billing against a branch that has closed.
 *
 * Every row here is money leaving the company for a line nobody is using, so the
 * panel is outlined in the theme's accent whether or not it has any: an operator
 * who has learned to recognise this card should be able to tell at a glance
 * which state it is in, and a card that only appeared when there was a problem
 * would be a card nobody has learned to recognise.
 *
 * The list itself is derived — see `SecretaryUIState.floatingAccounts`.
 */
@Composable
internal fun FloatingAccountsTab(uiState: SecretaryUIState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = Dimensions.viewStroke1,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(Dimensions.viewRadius16),
            ),
    ) {
        SectionCard(
            title = stringResource(Res.string.secretary_floating_title),
            icon = AppIcons.Wifi,
        ) {
            Text(
                text = stringResource(Res.string.secretary_floating_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            if (uiState.floatingAccounts.isEmpty()) {
                AllClearState()
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
                    uiState.floatingAccounts.forEach { account ->
                        FloatingAccountCard(account)
                    }
                }
            }
        }
    }
}

/**
 * The good outcome, stated rather than left blank.
 *
 * Outlined instead of filled: nothing is wrong, so it should read as an empty
 * tray and not as another row demanding attention.
 */
@Composable
private fun AllClearState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = Dimensions.viewStroke1,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding32),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.secretary_floating_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FloatingAccountCard(account: SecretaryAccountRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = account.accountNumber,
                style = MaterialTheme.typography.bodyMedium
                    .copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
            AccountStatusChip(account.status)
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                Icon(
                    imageVector = AppIcons.Domain,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.viewSize16),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = account.storeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = stringResource(
                    Res.string.secretary_accounts_monthly_rate,
                    account.monthlyRate,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
