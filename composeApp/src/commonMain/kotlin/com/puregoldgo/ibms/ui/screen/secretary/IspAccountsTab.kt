package com.puregoldgo.ibms.ui.screen.secretary

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import com.puregoldgo.ibms.ui.component.AlphabetRail
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_all_isps
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_accounts_all_status
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_accounts_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_accounts_monthly_rate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_accounts_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_accounts_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_retry
import org.jetbrains.compose.resources.stringResource

/**
 * The ISP accounts database — indexed A–Z by branch, and filtered by ISP, by
 * standing and by free text over the account number or the store.
 *
 * Terminated accounts are not here; they are in the archive.
 */
@Composable
internal fun IspAccountsTab(
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
    isCompact: Boolean,
) {
    val allIspsLabel = stringResource(Res.string.console_all_isps)
    val allStatusLabel = stringResource(Res.string.secretary_accounts_all_status)
    val statusOptions = accountStatusFilterOptions()

    val ispFilter = @Composable { modifier: Modifier ->
        LabeledDropdown(
            options = uiState.activeProviders.providerFilterOptions(),
            selectedId = uiState.accountProviderId,
            onSelect = callback.onAccountProviderSelect,
            placeholder = allIspsLabel,
            clearLabel = allIspsLabel,
            modifier = modifier,
        )
    }
    val statusFilter = @Composable { modifier: Modifier ->
        LabeledDropdown(
            options = statusOptions,
            selectedId = uiState.accountStatus?.name,
            onSelect = { id ->
                callback.onAccountStatusSelect(
                    id?.let { AccountRecordStatus.valueOf(it) },
                )
            },
            placeholder = allStatusLabel,
            clearLabel = allStatusLabel,
            modifier = modifier,
        )
    }
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.accountQuery,
            onValueChange = callback.onAccountQueryChange,
            placeholder = stringResource(Res.string.secretary_accounts_search_hint),
            modifier = modifier,
        )
    }

    val card = @Composable { modifier: Modifier ->
        SectionCard(
            title = stringResource(Res.string.secretary_accounts_title),
            icon = AppIcons.Wifi,
            modifier = modifier,
            trailing = if (isCompact) {
                null
            } else {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                    ) {
                        ispFilter(Modifier.width(Dimensions.viewWidth160))
                        statusFilter(Modifier.width(Dimensions.viewWidth160))
                        search(Modifier.width(Dimensions.viewWidth280))
                        ExportButton(callback.onExportAccounts)
                        AddButton(callback.onAddAccountClick)
                    }
                }
            },
        ) {
            if (isCompact) {
                ispFilter(Modifier.fillMaxWidth())
                Spacer(Modifier.height(Dimensions.viewPadding8))
                statusFilter(Modifier.fillMaxWidth())
                Spacer(Modifier.height(Dimensions.viewPadding8))
                search(Modifier.fillMaxWidth())
                Spacer(Modifier.height(Dimensions.viewPadding8))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
                    ExportButton(callback.onExportAccounts, Modifier.weight(1f))
                    AddButton(callback.onAddAccountClick, Modifier.weight(1f))
                }
                Spacer(Modifier.height(Dimensions.viewPadding16))
            }

            when {
                uiState.isLoading -> SectionLoadingState()

                uiState.loadError != null -> SectionErrorState(
                    message = uiState.loadError,
                    onRetry = callback.onRetryLoad,
                    retryLabel = stringResource(Res.string.secretary_retry),
                )

                uiState.visibleAccounts.isEmpty() ->
                    SectionEmptyState(stringResource(Res.string.secretary_accounts_empty))

                else -> LazyColumn(
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

    val rail = @Composable {
        AlphabetRail(
            letters = uiState.accountLetters,
            selected = uiState.accountLetter,
            onSelect = callback.onAccountLetterSelect,
            isCompact = isCompact,
        )
    }

    if (isCompact) {
        Column {
            rail()
            Spacer(Modifier.height(Dimensions.viewPadding12))
            card(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            rail()
            card(Modifier.weight(1f))
        }
    }
}

@Composable
private fun AccountCard(account: SecretaryAccountRow) {
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
