package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_generated
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_period
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_total
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_total_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_not_available
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_accounts_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_detail_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_detail_no_match
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_detail_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_detail_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_detail_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_line_mrc
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_line_prorated
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_line_rfp
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_line_rfp_none
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_sort_amount
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_sort_ascending
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_sort_descending
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_sort_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_sort_rfp
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_topsheet_sort_storecode
import org.jetbrains.compose.resources.stringResource

/**
 * A read-only detail dialog showing a compiled topsheet's accounts.
 *
 * Reachable from any row in the billing-history tab. The header is drawn from the
 * list row (so it appears instantly), while the account lines are fetched behind a
 * spinner. Each line is searchable and sortable, and tapping one opens
 * [AccountDetailsDialog] — the account seen in full.
 */
@Composable
internal fun TopSheetDetailsDialog(
    detail: TopSheetDetail,
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
    isCompact: Boolean,
) {
    val header = detail.header

    AppDialog(onDismissRequest = callback.onTopSheetDetailDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_topsheet_detail_title),
            icon = AppIcons.Description,
            onClose = callback.onTopSheetDetailDismiss,
            closeDescription = stringResource(Res.string.secretary_detail_close),
            subtitle = stringResource(Res.string.secretary_topsheet_detail_subtitle, header.reference),
        )

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            TopSheetSummaryHeader(header)

            Spacer(Modifier.height(Dimensions.viewPadding16))

            DetailCard(modifier = Modifier.fillMaxWidth()) {
                DetailSectionHeader(
                    icon = AppIcons.Wifi,
                    title = stringResource(Res.string.secretary_topsheet_accounts_title),
                )
                Spacer(Modifier.height(Dimensions.viewPadding12))

                // The search and sort controls are only meaningful once lines exist.
                if (detail.lines.isNotEmpty() && !detail.isLoadingLines && detail.linesError == null) {
                    TopSheetLineControls(uiState, callback, isCompact)
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                }

                when {
                    detail.isLoadingLines -> SectionLoadingState()

                    detail.linesError != null -> SectionErrorState(
                        message = detail.linesError,
                        onRetry = { callback.onTopSheetClick(header.id) },
                        retryLabel = stringResource(Res.string.secretary_retry),
                    )

                    detail.lines.isEmpty() ->
                        SectionEmptyState(stringResource(Res.string.secretary_topsheet_detail_empty))

                    uiState.visibleTopSheetLines.isEmpty() ->
                        SectionEmptyState(stringResource(Res.string.secretary_topsheet_detail_no_match))

                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                    ) {
                        items(uiState.visibleTopSheetLines, key = { it.accountId }) { line ->
                            TopSheetLineCard(line, callback)
                        }
                    }
                }
            }
        }
    }
}

/** The topsheet's identity + totals, drawn from the already-loaded list row. */
@Composable
private fun TopSheetSummaryHeader(header: TopSheetRow) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Text(
                text = header.reference,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            StatusChip(label = header.providerName.uppercase(), tone = ChipTone.Neutral)
            TopSheetStatusChip(header.status)
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                MetaText(stringResource(Res.string.secretary_billing_period, header.period))
                MetaText(stringResource(Res.string.secretary_billing_generated, header.generatedOn))
                MetaText(stringResource(Res.string.secretary_billing_accounts, header.accountCount))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.secretary_billing_total_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.secretary_billing_total, header.totalValidated),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Combined search, the sort-key selector, and the ascending/descending toggle. */
@Composable
private fun TopSheetLineControls(
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
    isCompact: Boolean,
) {
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.topSheetLineQuery,
            onValueChange = callback.onTopSheetLineQueryChange,
            placeholder = stringResource(Res.string.secretary_topsheet_detail_search_hint),
            modifier = modifier,
        )
    }
    val sort = @Composable { modifier: Modifier ->
        LabeledDropdown(
            options = topSheetLineSortOptions(),
            selectedId = uiState.topSheetLineSort.name,
            onSelect = { id -> id?.let { callback.onTopSheetLineSortSelect(TopSheetLineSortKey.valueOf(it)) } },
            placeholder = stringResource(Res.string.secretary_topsheet_sort_label),
            label = stringResource(Res.string.secretary_topsheet_sort_label),
            modifier = modifier,
        )
    }
    val direction = @Composable {
        SortDirectionButton(uiState.topSheetLineSortAsc, callback.onTopSheetLineSortDirectionToggle)
    }

    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
            search(Modifier.fillMaxWidth())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                sort(Modifier.weight(1f))
                direction()
            }
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            search(Modifier.weight(1f))
            sort(Modifier.width(Dimensions.viewWidth180))
            direction()
        }
    }
}

/** A compact arrow toggle flipping the sort between ascending and descending. */
@Composable
private fun SortDirectionButton(ascending: Boolean, onToggle: () -> Unit) {
    val description = stringResource(
        if (ascending) Res.string.secretary_topsheet_sort_ascending
        else Res.string.secretary_topsheet_sort_descending,
    )
    OutlinedButton(
        onClick = onToggle,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Text(
            text = if (ascending) "↑" else "↓",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun topSheetLineSortOptions(): List<FilterOption> =
    TopSheetLineSortKey.entries.map { key ->
        FilterOption(
            id = key.name,
            label = stringResource(
                when (key) {
                    TopSheetLineSortKey.StoreCode -> Res.string.secretary_topsheet_sort_storecode
                    TopSheetLineSortKey.Amount -> Res.string.secretary_topsheet_sort_amount
                    TopSheetLineSortKey.RfpNumber -> Res.string.secretary_topsheet_sort_rfp
                },
            ),
        )
    }

/**
 * A single account line. Tapping it opens [AccountDetailsDialog] for that account
 * — the same circuit, seen in full.
 */
@Composable
private fun TopSheetLineCard(line: TopSheetLineRow, callback: SecretaryCallback) {
    val na = stringResource(Res.string.secretary_detail_not_available)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { callback.onAccountClick(line.accountId) }
            .padding(Dimensions.viewPadding16),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    Text(
                        text = line.storeName ?: na,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusChip(
                        label = line.rfpNumber
                            ?.let { stringResource(Res.string.secretary_topsheet_line_rfp, it) }
                            ?: stringResource(Res.string.secretary_topsheet_line_rfp_none),
                        tone = ChipTone.Neutral,
                    )
                }
                Spacer(Modifier.height(Dimensions.viewPadding4))
                Text(
                    text = line.metaLine(na),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                AmountBlock(
                    label = stringResource(Res.string.secretary_topsheet_line_mrc),
                    value = line.fullMrc,
                    emphasised = false,
                )
                Spacer(Modifier.height(Dimensions.viewPadding4))
                AmountBlock(
                    label = stringResource(Res.string.secretary_topsheet_line_prorated),
                    value = line.prorated,
                    emphasised = true,
                )
            }
        }
    }
}

/** A right-aligned label over a peso amount; the prorated (billed) figure is emphasised. */
@Composable
private fun AmountBlock(label: String, value: String, emphasised: Boolean) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "₱$value",
            style = if (emphasised) {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            } else {
                MaterialTheme.typography.bodySmall
            },
            color = if (emphasised) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

/** Store code · account number · circuit id — the identifiers, or N/A if none are known. */
private fun TopSheetLineRow.metaLine(na: String): String =
    listOfNotNull(storeCode, accountNumber, circuitId)
        .joinToString("  •  ")
        .ifBlank { na }

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
