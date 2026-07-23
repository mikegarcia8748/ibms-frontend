package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.koin.compose.viewmodel.koinViewModel

/**
 * Full-screen topsheet detail — the account lines for a compiled topsheet,
 * searchable and sortable, with room for the whole list.
 *
 * Replaces the former `TopSheetDetailsDialog`, which was capped at 560dp wide
 * and 480dp tall — too cramped for a topsheet with many lines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSheetDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TopSheetDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TopSheetDetailContent(
        uiState = uiState,
        callback = TopSheetDetailCallback(
            onBackClick = onNavigateBack,
            onLineQueryChange = viewModel::onLineQueryChange,
            onLineSortSelect = viewModel::onLineSortSelect,
            onLineSortDirectionToggle = viewModel::onLineSortDirectionToggle,
            onAccountClick = viewModel::onAccountClick,
            onAccountDetailDismiss = viewModel::onAccountDetailDismiss,
            onRetryLines = viewModel::loadLines,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` so preview functions can draw it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopSheetDetailContent(
    uiState: TopSheetDetailUIState,
    callback: TopSheetDetailCallback,
) {
    val header = uiState.header

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
                ),
                navigationIcon = {
                    IconButton(onClick = callback.onBackClick) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.secretary_detail_close),
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(Res.string.secretary_topsheet_detail_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimensions.viewPadding24),
        ) {
            if (header != null) {
                Text(
                    text = stringResource(
                        Res.string.secretary_topsheet_detail_subtitle,
                        header.reference,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Dimensions.viewPadding16))

                TopSheetSummaryHeader(header)

                Spacer(Modifier.height(Dimensions.viewPadding24))
            }

            DetailCard(modifier = Modifier.fillMaxWidth()) {
                DetailSectionHeader(
                    icon = AppIcons.Wifi,
                    title = stringResource(Res.string.secretary_topsheet_accounts_title),
                )
                Spacer(Modifier.height(Dimensions.viewPadding12))

                // The search and sort controls are only meaningful once lines exist.
                if (uiState.lines.isNotEmpty() && !uiState.isLoadingLines && uiState.linesError == null) {
                    TopSheetLineControls(uiState, callback)
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                }

                when {
                    uiState.isLoadingLines -> SectionLoadingState()

                    uiState.linesError != null -> SectionErrorState(
                        message = uiState.linesError,
                        onRetry = callback.onRetryLines,
                        retryLabel = stringResource(Res.string.secretary_retry),
                    )

                    uiState.lines.isEmpty() ->
                        SectionEmptyState(stringResource(Res.string.secretary_topsheet_detail_empty))

                    uiState.visibleLines.isEmpty() ->
                        SectionEmptyState(stringResource(Res.string.secretary_topsheet_detail_no_match))

                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                    ) {
                        items(uiState.visibleLines, key = { it.accountId }) { line ->
                            TopSheetLineCard(line, callback)
                        }
                    }
                }
            }
        }

        // Account detail modal — overlay on top of the screen.
        uiState.accountDetail?.let { detail ->
            AccountDetailsDialog(detail = detail, onDismiss = callback.onAccountDetailDismiss)
        }
    }
}

// region Composables relocated from the former TopSheetDetailsDialog

/** The topsheet's identity + totals, drawn from the already-loaded list row. */
@Composable
internal fun TopSheetSummaryHeader(header: TopSheetRow) {
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
    uiState: TopSheetDetailUIState,
    callback: TopSheetDetailCallback,
) {
    Column (verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
        SearchField(
            modifier = Modifier
                .fillMaxWidth(),
            value = uiState.lineQuery,
            onValueChange = callback.onLineQueryChange,
            placeholder = stringResource(Res.string.secretary_topsheet_detail_search_hint),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            LabeledDropdown(
                modifier = Modifier
                    .weight(1f),
                options = topSheetLineSortOptions(),
                selectedId = uiState.lineSort.name,
                onSelect = { id -> id?.let { callback.onLineSortSelect(TopSheetLineSortKey.valueOf(it)) } },
                placeholder = stringResource(Res.string.secretary_topsheet_sort_label),
                label = stringResource(Res.string.secretary_topsheet_sort_label),
            )
            SortDirectionButton(uiState.lineSortAsc, callback.onLineSortDirectionToggle)
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
            text = if (ascending) "Descending" else "Ascending",
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
private fun TopSheetLineCard(line: TopSheetLineRow, callback: TopSheetDetailCallback) {
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

// endregion
