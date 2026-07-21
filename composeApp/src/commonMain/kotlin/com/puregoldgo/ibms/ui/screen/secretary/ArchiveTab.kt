package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_accounts_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_branches
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_branches_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_providers
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_providers_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_archive_reason
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_inactive
import org.jetbrains.compose.resources.stringResource

/**
 * What has been retired: accounts no longer billing, branches that have closed,
 * and vendors no longer used.
 *
 * Three columns side by side rather than a fourth set of tabs — they are read
 * together. Closing a branch strands its accounts, and dropping a vendor
 * strands both, so the three lists are almost always consulted at once.
 *
 * Struck through rather than greyed out: these records still matter for the
 * months they were billed in, so they must stay legible while reading
 * unmistakably as over.
 */
@Composable
internal fun ArchiveTab(
    uiState: SecretaryUIState,
    isCompact: Boolean,
) {
    val providerNames = uiState.providers.associate { it.id to it.name }

    val accounts = @Composable { modifier: Modifier ->
        SectionCard(
            title = stringResource(Res.string.secretary_archive_accounts),
            icon = AppIcons.Wifi,
            modifier = modifier,
        ) {
            ArchiveColumn(
                isEmpty = uiState.inactiveAccounts.isEmpty(),
                emptyMessage = stringResource(Res.string.secretary_archive_accounts_empty),
            ) {
                uiState.inactiveAccounts.forEach { account ->
                    ArchivedAccountCard(
                        account = account,
                        providerName = providerNames[account.providerId].orEmpty(),
                    )
                }
            }
        }
    }

    val branches = @Composable { modifier: Modifier ->
        SectionCard(
            title = stringResource(Res.string.secretary_archive_branches),
            icon = AppIcons.Domain,
            modifier = modifier,
        ) {
            ArchiveColumn(
                isEmpty = uiState.closedBranches.isEmpty(),
                emptyMessage = stringResource(Res.string.secretary_archive_branches_empty),
            ) {
                uiState.closedBranches.forEach { branch -> ArchivedBranchCard(branch) }
            }
        }
    }

    val providers = @Composable { modifier: Modifier ->
        SectionCard(
            title = stringResource(Res.string.secretary_archive_providers),
            icon = AppIcons.Description,
            modifier = modifier,
        ) {
            ArchiveColumn(
                isEmpty = uiState.inactiveProviders.isEmpty(),
                emptyMessage = stringResource(Res.string.secretary_archive_providers_empty),
            ) {
                uiState.inactiveProviders.forEach { provider -> ArchivedProviderCard(provider) }
            }
        }
    }

    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            accounts(Modifier.fillMaxWidth())
            branches(Modifier.fillMaxWidth())
            providers(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            // Equal thirds: no column is the important one, and a width that
            // tracked row counts would reshuffle the layout as records retire.
            accounts(Modifier.weight(1f))
            branches(Modifier.weight(1f))
            providers(Modifier.weight(1f))
        }
    }
}

/** A column's rows, or the line it shows when it has none of its own. */
@Composable
private fun ColumnScope.ArchiveColumn(
    isEmpty: Boolean,
    emptyMessage: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isEmpty) {
        SectionEmptyState(emptyMessage)
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            content = content,
        )
    }
}

@Composable
private fun ArchivedAccountCard(account: SecretaryAccountRow, providerName: String) {
    ArchiveCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StruckText(
                text = account.accountNumber,
                style = MaterialTheme.typography.bodyMedium
                    .copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f, fill = false),
            )
            AccountStatusChip(account.status)
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = account.storeName,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = providerName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ArchivedBranchCard(branch: SecretaryBranchRow) {
    ArchiveCard {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            StruckText(
                text = branch.branchCode,
                style = MaterialTheme.typography.bodyMedium
                    .copy(fontFamily = FontFamily.Monospace),
            )
            StruckText(
                text = branch.displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2,
            )
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            BranchStatusChip(branch.status)
            branch.closureReason?.let { reason ->
                Text(
                    text = stringResource(Res.string.secretary_archive_reason, reason),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ArchivedProviderCard(provider: SecretaryProviderRow) {
    ArchiveCard {
        StruckText(
            text = provider.name,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(Dimensions.viewPadding8))
        StatusChip(
            label = stringResource(Res.string.secretary_status_inactive),
            tone = ChipTone.Negative,
        )
    }
}

@Composable
private fun ArchiveCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding12),
        content = content,
    )
}

/** The retired identifier: struck through, and muted so the pill beside it leads. */
@Composable
private fun StruckText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(textDecoration = TextDecoration.LineThrough),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}
