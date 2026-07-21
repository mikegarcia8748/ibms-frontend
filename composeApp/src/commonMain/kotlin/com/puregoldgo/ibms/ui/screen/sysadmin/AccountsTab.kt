package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.IspFilterDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_accounts_db
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_accounts_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_accounts_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_clean_up
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_monthly_rate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_retry
import org.jetbrains.compose.resources.stringResource

/**
 * The ISP accounts database — searchable by account number or by store, and
 * filterable to one ISP.
 */
@Composable
internal fun AccountsTab(
    uiState: SysadminUIState,
    callback: SysadminCallback,
    isCompact: Boolean,
) {
    val filters = @Composable { modifier: Modifier ->
        IspFilterDropdown(
            options = uiState.activeProviders.toFilterOptions(),
            selectedId = uiState.accountProviderId,
            onSelect = callback.onAccountProviderSelect,
            modifier = modifier,
        )
    }
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.accountQuery,
            onValueChange = callback.onAccountQueryChange,
            placeholder = stringResource(Res.string.sysadmin_accounts_search_hint),
            modifier = modifier,
        )
    }

    SectionCard(
        title = stringResource(Res.string.sysadmin_accounts_db),
        trailing = if (isCompact) {
            null
        } else {
            {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12)) {
                    filters(Modifier)
                    search(Modifier.width(Dimensions.viewWidth280))
                    CleanUpButton()
                }
            }
        },
    ) {
        if (isCompact) {
            filters(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding8))
            search(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding8))
            CleanUpButton()
            Spacer(Modifier.height(Dimensions.viewPadding16))
        }

        if (uiState.isLoading) {
            SectionLoadingState()
        } else if (uiState.loadError != null) {
            SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.console_retry),
            )
        } else if (uiState.visibleAccounts.isEmpty()) {
            SectionEmptyState(stringResource(Res.string.sysadmin_accounts_empty))
        } else {
            LazyColumn(
                // Bounded so this list scrolls within the page rather than
                // stretching it to the length of the whole database.
                modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                items(uiState.visibleAccounts, key = { it.id }) { account ->
                    AccountCard(account)
                }
            }
        }
    }
}

/**
 * Disabled on purpose.
 *
 * TODO: no backend endpoint exists for this yet. Whatever "clean up" turns out
 *  to mean — pruning orphaned accounts, de-duplicating an import — it deletes
 *  billing records, so it stays inert until there is a defined operation with a
 *  confirmation behind it.
 */
@Composable
private fun CleanUpButton() {
    OutlinedButton(
        onClick = {},
        enabled = false,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        Icon(
            imageVector = AppIcons.Delete,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize18),
        )
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(stringResource(Res.string.sysadmin_clean_up))
    }
}

@Composable
private fun AccountCard(account: IspAccountRow) {
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
                // Monospaced: these get read aloud and compared character by
                // character against a provider's invoice.
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
            RecordStatusChip(isActive = account.isActive)
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
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
                text = stringResource(Res.string.sysadmin_monthly_rate, account.monthlyRate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
