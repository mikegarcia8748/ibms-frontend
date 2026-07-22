package com.puregoldgo.ibms.ui.screen.finance

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.IspFilterDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_balanced
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_compiled
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_export
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_period
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_total
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_total_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_variance
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The shell both finance queues are drawn in: a searchable, ISP-filtered card
 * over a bounded list of topsheet rows.
 *
 * One composable rather than two near-identical tabs. The queues differ only in
 * which sheets they hold, what the primary action says, and whether an export
 * sits beside it — everything else about them, down to the ordering of the
 * filter controls, has to stay the same or the two tabs read as two features.
 */
@Composable
internal fun TopSheetQueue(
    uiState: FinanceUIState,
    callback: FinanceCallback,
    isCompact: Boolean,
    sheets: List<FinanceTopSheetRow>,
    title: StringResource,
    searchHint: StringResource,
    emptyMessage: StringResource,
    query: String,
    onQueryChange: (String) -> Unit,
    providerId: String?,
    onProviderSelect: (String?) -> Unit,
    actionIcon: ImageVector,
    actionLabel: StringResource,
    onAction: (FinanceTopSheetRow) -> Unit,
    onExport: ((FinanceTopSheetRow) -> Unit)? = null,
    note: (@Composable () -> Unit)? = null,
) {
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = stringResource(searchHint),
            modifier = modifier,
        )
    }
    val ispFilter = @Composable { modifier: Modifier ->
        IspFilterDropdown(
            options = uiState.activeProviders.providerFilterOptions(),
            selectedId = providerId,
            onSelect = onProviderSelect,
            modifier = modifier,
        )
    }

    SectionCard(
        title = stringResource(title),
        icon = AppIcons.Description,
        trailing = if (isCompact) {
            null
        } else {
            {
                search(Modifier.width(Dimensions.viewWidth280))
                Spacer(Modifier.width(Dimensions.viewPadding12))
                ispFilter(Modifier.width(Dimensions.viewWidth180))
            }
        },
    ) {
        if (isCompact) {
            search(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding12))
            ispFilter(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding16))
        }

        note?.let {
            it()
            Spacer(Modifier.height(Dimensions.viewPadding16))
        }

        when {
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.console_retry),
            )

            sheets.isEmpty() -> SectionEmptyState(stringResource(emptyMessage))

            else -> LazyColumn(
                // Bounded so the queue scrolls within the page rather than
                // stretching it to the length of a whole billing year.
                modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                items(sheets, key = { it.id }) { sheet ->
                    TopSheetQueueRow(
                        sheet = sheet,
                        isCompact = isCompact,
                        actionIcon = actionIcon,
                        actionLabel = stringResource(actionLabel),
                        onAction = { onAction(sheet) },
                        onExport = onExport?.let { export -> { export(sheet) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopSheetQueueRow(
    sheet: FinanceTopSheetRow,
    isCompact: Boolean,
    actionIcon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit,
    onExport: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        @Composable
        fun identity() {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    Text(
                        text = sheet.invoiceNumber,
                        // Monospaced: invoice numbers get read aloud and
                        // compared character by character against the vendor's.
                        style = MaterialTheme.typography.bodyLarge
                            .copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusChip(label = sheet.providerName.uppercase(), tone = ChipTone.Neutral)
                    VarianceChip(sheet.varianceCount)
                }

                Spacer(Modifier.height(Dimensions.viewPadding8))

                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
                    MetaText(stringResource(Res.string.finance_period, sheet.period))
                    MetaText(stringResource(Res.string.finance_compiled, sheet.compiledOn))
                    MetaText(stringResource(Res.string.finance_accounts, sheet.accountCount))
                }
            }
        }

        @Composable
        fun totalAndActions(alignment: Alignment.Horizontal) {
            Column(
                horizontalAlignment = alignment,
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                Text(
                    text = stringResource(Res.string.finance_total_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.finance_total, sheet.totalValidated),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
                    if (onExport != null) {
                        OutlinedButton(
                            onClick = onExport,
                            shape = RoundedCornerShape(Dimensions.viewRadius8),
                        ) {
                            Icon(
                                imageVector = AppIcons.Download,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.viewSize18),
                            )
                            Spacer(Modifier.width(Dimensions.viewPadding8))
                            Text(stringResource(Res.string.finance_export))
                        }
                    }
                    Button(onClick = onAction, shape = RoundedCornerShape(Dimensions.viewRadius8)) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.viewSize18),
                        )
                        Spacer(Modifier.width(Dimensions.viewPadding8))
                        Text(actionLabel)
                    }
                }
            }
        }

        if (isCompact) {
            // The metadata line alone is wider than a phone; stacking keeps the
            // total and its action readable instead of squeezing both.
            identity()
            Spacer(Modifier.height(Dimensions.viewPadding12))
            totalAndActions(Alignment.Start)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                identity()
                totalAndActions(Alignment.End)
            }
        }
    }
}

/**
 * How far the sheet is off contract.
 *
 * Balanced is stated rather than left blank: an absent chip would be
 * indistinguishable from a sheet whose variance has not been computed, and
 * "nothing to check here" is the thing finance is looking for.
 */
@Composable
private fun VarianceChip(varianceCount: Int) {
    if (varianceCount > 0) {
        StatusChip(
            label = stringResource(Res.string.finance_variance, varianceCount),
            tone = ChipTone.Accent,
        )
    } else {
        StatusChip(label = stringResource(Res.string.finance_balanced), tone = ChipTone.Positive)
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** The ISP filter's entries — active providers only, by the id the rows carry. */
internal fun List<FinanceProviderRow>.providerFilterOptions() =
    map { FilterOption(id = it.id, label = it.name) }
