package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_isp_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_metric_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_metric_awaiting
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_metric_billed
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_metric_paid
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_metric_peso
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_overview_by_isp
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_overview_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_overview_share
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_overview_title
import org.jetbrains.compose.resources.stringResource

/**
 * What the period cost, and where it went.
 *
 * Four figures and a per-ISP breakdown. No chart: the numbers are what get
 * repeated in a meeting, and a bar without them beside it would send the reader
 * looking for the table anyway.
 */
@Composable
internal fun OverviewTab(
    uiState: ManagerUIState,
    callback: ManagerCallback,
    isCompact: Boolean,
) {
    SectionCard(
        title = stringResource(Res.string.manager_overview_title),
        icon = AppIcons.Description,
        trailing = uiState.currentPeriod?.let { period ->
            { StatusChip(label = period, tone = ChipTone.Accent) }
        },
    ) {
        when {
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.console_retry),
            )

            uiState.currentPeriod == null ->
                SectionEmptyState(stringResource(Res.string.manager_overview_empty))

            else -> {
                MetricGrid(uiState, isCompact)

                Spacer(Modifier.height(Dimensions.viewPadding24))

                Text(
                    text = stringResource(Res.string.manager_overview_by_isp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(Dimensions.viewPadding12))

                Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
                    uiState.ispSpend.forEach { row -> IspSpendCard(row, isCompact) }
                }
            }
        }
    }
}

/**
 * The four headline figures.
 *
 * Two rows of two rather than one row of four, and one column on a phone: the
 * peso figures are long enough that four across a laptop would truncate them,
 * and a truncated total is worse than no total.
 */
@Composable
private fun MetricGrid(uiState: ManagerUIState, isCompact: Boolean) {
    val metrics = listOf(
        stringResource(Res.string.manager_metric_billed) to
            stringResource(Res.string.manager_metric_peso, uiState.periodBilled),
        stringResource(Res.string.manager_metric_awaiting) to
            stringResource(Res.string.manager_metric_peso, uiState.periodAwaiting),
        stringResource(Res.string.manager_metric_paid) to
            stringResource(Res.string.manager_metric_peso, uiState.periodPaid),
        stringResource(Res.string.manager_metric_accounts) to
            uiState.periodAccountCount.toString(),
    )

    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
            metrics.forEach { (label, value) -> MetricTile(label, value, Modifier.fillMaxWidth()) }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
            metrics.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
                    pair.forEach { (label, value) ->
                        MetricTile(label, value, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimensions.viewPadding4))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun IspSpendCard(row: IspSpendRow, isCompact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = row.providerName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!isCompact) {
                Text(
                    text = stringResource(Res.string.manager_overview_share, row.share),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(Res.string.manager_metric_peso, row.total),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (isCompact) {
                    row.share
                } else {
                    stringResource(Res.string.manager_isp_accounts, row.accountCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
