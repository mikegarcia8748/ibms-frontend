package com.puregoldgo.ibms.ui.screen.secretary.compile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.puregoldgo.ibms.ui.component.AlphabetRail
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.MonthPicker
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.screen.secretary.AccountStatusChip
import com.puregoldgo.ibms.ui.screen.secretary.providerFilterOptions
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_accounts_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_all_providers
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_amount
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_amount_to_bill
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_assign_button
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_assign_end_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_assign_start_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_assign_storecodes
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_assign_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_back
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_batch
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_billing_day
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_button
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_confirm
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_done_invoice
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_done_summary
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_done_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_line_mrc
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_line_prorated
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_lines_no_match
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_lines_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_review_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_rfp_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_rfp_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_rfp_pending
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_rfp_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_rfp_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_select_isp
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_sort_alphabetical
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_sort_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_sort_mrc
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_sort_storecode
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_showing
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_showing_pick
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_start_new
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_remove_line
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_total_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_total_amount
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_retry
import org.jetbrains.compose.resources.stringResource

/**
 * The Compile TopSheet panel: pick an ISP and a billing month, review the
 * accounts that will be billed, create a draft, assign an RFP number per line,
 * and confirm to mint the invoice. Three screens — review, RFP entry, and the
 * compiled result — chosen by [CompileUIState.phase].
 *
 * `internal` and free of any ViewModel dependency so the preview harness can draw
 * any screen from a plain state.
 */
@Composable
internal fun CompileTopSheetTab(
    state: CompileUIState,
    callback: CompileCallback,
    isCompact: Boolean,
) {
    when (state.phase) {
        CompilePhase.Review -> ReviewScreen(state, callback, isCompact)
        CompilePhase.RfpEntry -> RfpEntryScreen(state, callback, isCompact)
        CompilePhase.Compiled -> CompiledScreen(state, callback)
    }
}

// ─── Review ────────────────────────────────────────────────────────────────────

@Composable
private fun ReviewScreen(
    state: CompileUIState,
    callback: CompileCallback,
    isCompact: Boolean,
) {
    Column {
        ReviewControls(state, callback, isCompact)
        Spacer(Modifier.height(Dimensions.viewPadding16))

        val accountsCard = @Composable { modifier: Modifier ->
            SectionCard(
                title = stringResource(Res.string.secretary_compile_accounts_title),
                icon = AppIcons.Wifi,
                modifier = modifier,
                trailing = if (isCompact) {
                    null
                } else {
                    {
                        SearchField(
                            value = state.query,
                            onValueChange = callback.onQueryChange,
                            placeholder = stringResource(Res.string.secretary_compile_search_hint),
                            modifier = Modifier.width(Dimensions.viewWidth280),
                        )
                    }
                },
            ) {
                if (isCompact) {
                    SearchField(
                        value = state.query,
                        onValueChange = callback.onQueryChange,
                        placeholder = stringResource(Res.string.secretary_compile_search_hint),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                }

                when {
                    state.isLoading -> SectionLoadingState()

                    state.loadError != null -> SectionErrorState(
                        message = state.loadError,
                        onRetry = callback.onRetryLoad,
                        retryLabel = stringResource(Res.string.secretary_retry),
                    )

                    state.visibleRows.isEmpty() ->
                        SectionEmptyState(stringResource(Res.string.secretary_compile_empty))

                    else -> LazyColumn(
                        modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                    ) {
                        items(state.visibleRows, key = { it.accountId }) { row ->
                            ReviewAccountCard(row)
                        }
                    }
                }
            }
        }

        val rail = @Composable {
            AlphabetRail(
                letters = state.letters,
                selected = state.letter,
                onSelect = callback.onLetterSelect,
                isCompact = isCompact,
            )
        }

        if (isCompact) {
            rail()
            Spacer(Modifier.height(Dimensions.viewPadding12))
            accountsCard(Modifier.fillMaxWidth())
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
                rail()
                accountsCard(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReviewControls(
    state: CompileUIState,
    callback: CompileCallback,
    isCompact: Boolean,
) {
    val providerDropdown = @Composable { modifier: Modifier ->
        LabeledDropdown(
            options = state.providers.providerFilterOptions(),
            selectedId = state.selectedProviderId,
            onSelect = callback.onProviderSelect,
            placeholder = stringResource(Res.string.secretary_compile_all_providers),
            clearLabel = stringResource(Res.string.secretary_compile_all_providers),
            modifier = modifier,
        )
    }

    SectionCard(
        title = stringResource(Res.string.secretary_compile_review_title),
        icon = AppIcons.Description,
    ) {
        if (isCompact) {
            MonthPicker(
                period = state.billingPeriod,
                canGoNext = state.canGoNextMonth,
                onPrevious = callback.onPreviousMonth,
                onNext = callback.onNextMonth,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimensions.viewPadding8))
            Text(
                text = stringResource(Res.string.secretary_compile_select_isp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimensions.viewPadding4))
            providerDropdown(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding12))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding24)) {
                TotalBlock(stringResource(Res.string.secretary_compile_total_accounts), state.totalCount.toString())
                TotalBlock(
                    stringResource(Res.string.secretary_compile_total_amount),
                    stringResource(Res.string.secretary_compile_amount, state.totalAmount.groupPeso()),
                )
            }
            Spacer(Modifier.height(Dimensions.viewPadding12))
            CompileButton(state, callback, Modifier.fillMaxWidth())
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MonthPicker(
                    period = state.billingPeriod,
                    canGoNext = state.canGoNextMonth,
                    onPrevious = callback.onPreviousMonth,
                    onNext = callback.onNextMonth,
                )
                Spacer(Modifier.width(Dimensions.viewPadding16))
                providerDropdown(Modifier.width(Dimensions.viewWidth280))
                Box(Modifier.weight(1f))
                TotalBlock(stringResource(Res.string.secretary_compile_total_accounts), state.totalCount.toString())
                Spacer(Modifier.width(Dimensions.viewPadding24))
                TotalBlock(
                    stringResource(Res.string.secretary_compile_total_amount),
                    stringResource(Res.string.secretary_compile_amount, state.totalAmount.groupPeso()),
                )
                Spacer(Modifier.width(Dimensions.viewPadding24))
                CompileButton(state, callback)
            }
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))
        Text(
            text = if (state.selectedProviderId == null) {
                stringResource(Res.string.secretary_compile_showing_pick)
            } else {
                stringResource(Res.string.secretary_compile_showing, state.totalCount)
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.compileError?.let { error ->
            Spacer(Modifier.height(Dimensions.viewPadding8))
            ErrorText(error)
        }
    }
}

@Composable
private fun CompileButton(
    state: CompileUIState,
    callback: CompileCallback,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = callback.onCompileClick,
        enabled = state.canCompile,
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
    ) {
        if (state.isCompiling) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimensions.viewSize18),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = Dimensions.viewStroke2,
            )
        } else {
            Icon(
                imageVector = AppIcons.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.viewSize18),
            )
        }
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(stringResource(Res.string.secretary_compile_button))
    }
}

@Composable
private fun TotalBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ReviewAccountCard(row: CompileAccountRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    Text(
                        text = row.storeName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AccountStatusChip(row.status)
                }
                Spacer(Modifier.height(Dimensions.viewPadding4))
                Text(
                    text = row.metaLine(),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.secretary_compile_amount_to_bill),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.secretary_compile_amount, row.amount.groupPeso()),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CompileAccountRow.metaLine(): String {
    val day = billingDay?.let { stringResource(Res.string.secretary_compile_billing_day, it) }
    return listOfNotNull(accountNumber, providerName, day).joinToString("  •  ")
}

// ─── RFP entry ───────────────────────────────────────────────────────────────

@Composable
private fun RfpEntryScreen(
    state: CompileUIState,
    callback: CompileCallback,
    isCompact: Boolean,
) {
    SectionCard(
        title = stringResource(Res.string.secretary_compile_rfp_title),
        icon = AppIcons.Description,
        trailing = {
            state.draft?.batchNumber?.let { batch ->
                Text(
                    text = stringResource(Res.string.secretary_compile_batch, batch),
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Text(
            text = stringResource(Res.string.secretary_compile_rfp_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimensions.viewPadding16))

        // The bulk range assigner and the search/sort controls sit above the per-line
        // list; both are only meaningful once the draft's lines have loaded.
        if (state.lines.isNotEmpty() && !state.isLoadingLines && state.linesError == null) {
            RfpRangeAssigner(state, callback)
            Spacer(Modifier.height(Dimensions.viewPadding16))
            RfpListControls(state, callback, isCompact)
            Spacer(Modifier.height(Dimensions.viewPadding12))
        }

        when {
            state.isLoadingLines -> SectionLoadingState()

            state.linesError != null -> SectionErrorState(
                message = state.linesError,
                onRetry = callback.onRetryLines,
                retryLabel = stringResource(Res.string.secretary_retry),
            )

            state.lines.isEmpty() ->
                SectionEmptyState(stringResource(Res.string.secretary_compile_rfp_empty))

            // The A–Z rail files beside the list when there is width, above it otherwise.
            isCompact -> {
                AlphabetRail(
                    letters = state.lineLetters,
                    selected = state.linesLetter,
                    onSelect = callback.onLinesLetterSelect,
                    isCompact = true,
                )
                Spacer(Modifier.height(Dimensions.viewPadding12))
                RfpLinesList(state, callback, Modifier.fillMaxWidth())
            }

            else -> Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
                AlphabetRail(
                    letters = state.lineLetters,
                    selected = state.linesLetter,
                    onSelect = callback.onLinesLetterSelect,
                    isCompact = false,
                )
                RfpLinesList(state, callback, Modifier.weight(1f))
            }
        }

        state.confirmError?.let { error ->
            Spacer(Modifier.height(Dimensions.viewPadding12))
            ErrorText(error)
        }

        Spacer(Modifier.height(Dimensions.viewPadding16))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = callback.onBackToReview,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.viewSize18),
                )
                Spacer(Modifier.width(Dimensions.viewPadding8))
                Text(stringResource(Res.string.secretary_compile_back))
            }
            Box(Modifier.weight(1f))
            Button(
                onClick = callback.onConfirmClick,
                enabled = state.canConfirm,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                if (state.isConfirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimensions.viewStroke2,
                    )
                    Spacer(Modifier.width(Dimensions.viewPadding8))
                }
                Text(stringResource(Res.string.secretary_compile_confirm))
            }
        }
    }
}

/** Search (store, code, account) and the sort selector for the drafted-account list. */
@Composable
private fun RfpListControls(
    state: CompileUIState,
    callback: CompileCallback,
    isCompact: Boolean,
) {
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = state.linesQuery,
            onValueChange = callback.onLinesQueryChange,
            placeholder = stringResource(Res.string.secretary_compile_lines_search_hint),
            modifier = modifier,
        )
    }
    val sort = @Composable { modifier: Modifier ->
        LabeledDropdown(
            options = lineSortOptions(),
            selectedId = state.linesSort.name,
            onSelect = { id -> id?.let { callback.onLinesSortSelect(LineSortOrder.valueOf(it)) } },
            placeholder = stringResource(Res.string.secretary_compile_sort_label),
            label = stringResource(Res.string.secretary_compile_sort_label),
            modifier = modifier,
        )
    }

    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
            search(Modifier.fillMaxWidth())
            sort(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            search(Modifier.weight(1f))
            sort(Modifier.width(Dimensions.viewWidth180))
        }
    }
}

/** The filtered/sorted drafted-account rows, or a muted note when filters match nothing. */
@Composable
private fun RfpLinesList(
    state: CompileUIState,
    callback: CompileCallback,
    modifier: Modifier = Modifier,
) {
    if (state.visibleLines.isEmpty()) {
        SectionEmptyState(
            message = stringResource(Res.string.secretary_compile_lines_no_match),
            modifier = modifier,
        )
    } else {
        LazyColumn(
            modifier = modifier.heightIn(max = Dimensions.viewHeight480),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            items(state.visibleLines, key = { it.id }) { line ->
                RfpLineCard(line, callback)
            }
        }
    }
}

@Composable
private fun lineSortOptions(): List<FilterOption> = LineSortOrder.entries.map { order ->
    FilterOption(
        id = order.name,
        label = stringResource(
            when (order) {
                LineSortOrder.StoreCode -> Res.string.secretary_compile_sort_storecode
                LineSortOrder.Alphabetical -> Res.string.secretary_compile_sort_alphabetical
                LineSortOrder.MonthlyRecurringCharge -> Res.string.secretary_compile_sort_mrc
            },
        ),
    )
}

/**
 * Bulk RFP numbering: enter a start/end range and the backend assigns one number
 * per distinct store code across the draft, replacing the per-line list with the
 * re-sorted result. The store-code count is shown as a hint so the secretary can
 * size the range; the exact range check is enforced server-side.
 */
@Composable
private fun RfpRangeAssigner(
    state: CompileUIState,
    callback: CompileCallback,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        Text(
            text = stringResource(Res.string.secretary_compile_assign_title),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Dimensions.viewPadding4))
        Text(
            text = stringResource(Res.string.secretary_compile_assign_storecodes, state.distinctStoreCodeCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimensions.viewPadding12))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            OutlinedTextField(
                value = state.rfpRangeStart,
                onValueChange = callback.onRfpRangeStartChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(Res.string.secretary_compile_assign_start_hint)) },
                singleLine = true,
                enabled = !state.isAssigningRfp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(Dimensions.viewRadius8),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
            OutlinedTextField(
                value = state.rfpRangeEnd,
                onValueChange = callback.onRfpRangeEndChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(Res.string.secretary_compile_assign_end_hint)) },
                singleLine = true,
                enabled = !state.isAssigningRfp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { callback.onAssignRfpClick() }),
                shape = RoundedCornerShape(Dimensions.viewRadius8),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            )
            Button(
                onClick = callback.onAssignRfpClick,
                enabled = state.canAssignRfp,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                if (state.isAssigningRfp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimensions.viewStroke2,
                    )
                    Spacer(Modifier.width(Dimensions.viewPadding8))
                }
                Text(stringResource(Res.string.secretary_compile_assign_button))
            }
        }
        state.assignRfpError?.let { error ->
            Spacer(Modifier.height(Dimensions.viewPadding8))
            ErrorText(error)
        }
    }
}

@Composable
private fun RfpLineCard(
    line: CompileLineRow,
    callback: CompileCallback,
) {
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
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = line.storeName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(Dimensions.viewPadding4))
                Text(
                    text = line.metaLine(),
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (line.isProrated) {
                        stringResource(Res.string.secretary_compile_line_prorated, line.proratedAmount.groupPeso())
                    } else {
                        stringResource(Res.string.secretary_compile_line_mrc, line.fullAmount.groupPeso())
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // The editable RFP field only appears once the sequence has been assigned;
            // before that, a muted placeholder keeps the row aligned.
            if (line.hasSavedRfp) {
                RfpField(line, callback)
            } else {
                RfpPendingField()
            }

            IconButton(
                onClick = { callback.onRemoveLine(line.id) },
                enabled = !line.isRemoving,
            ) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = stringResource(Res.string.secretary_compile_remove_line),
                    modifier = Modifier.size(Dimensions.viewSize18),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        line.rowError?.let { error ->
            Spacer(Modifier.height(Dimensions.viewPadding8))
            ErrorText(error)
        }
    }
}

@Composable
private fun RfpField(
    line: CompileLineRow,
    callback: CompileCallback,
) {
    // Commit on blur: the RFP number is saved when the field loses focus (or the
    // keyboard's Done is pressed), not on every keystroke.
    var wasFocused by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = line.rfpInput,
        onValueChange = { callback.onRfpChange(line.id, it) },
        modifier = Modifier
            .width(Dimensions.viewWidth160)
            .onFocusChanged { focus ->
                if (wasFocused && !focus.isFocused) callback.onRfpCommit(line.id)
                wasFocused = focus.isFocused
            },
        placeholder = { Text(stringResource(Res.string.secretary_compile_rfp_hint)) },
        singleLine = true,
        isError = line.rowError != null,
        enabled = !line.isSavingRfp,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { callback.onRfpCommit(line.id) }),
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    )
}

/** Placeholder in the RFP slot before a line has been numbered — no field to edit yet. */
@Composable
private fun RfpPendingField() {
    Box(
        modifier = Modifier.width(Dimensions.viewWidth160),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.secretary_compile_rfp_pending),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompileLineRow.metaLine(): String =
    listOfNotNull(accountNumber, branchCode, circuitId).joinToString("  •  ")

// ─── Result ──────────────────────────────────────────────────────────────────

@Composable
private fun CompiledScreen(
    state: CompileUIState,
    callback: CompileCallback,
) {
    val compiled = state.compiled ?: return
    SectionCard(
        title = stringResource(Res.string.secretary_compile_done_title),
        icon = AppIcons.CheckCircle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding16),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Icon(
                imageVector = AppIcons.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.viewSize40),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(
                    Res.string.secretary_compile_done_invoice,
                    compiled.invoiceNumber ?: "—",
                ),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    Res.string.secretary_compile_done_summary,
                    compiled.accountCount,
                    compiled.totalAmount.groupPeso(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Dimensions.viewPadding8))
            Button(
                onClick = callback.onStartNew,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Text(stringResource(Res.string.secretary_compile_start_new))
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}
