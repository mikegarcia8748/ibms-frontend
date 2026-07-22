package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_status_approved
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_status_compiled
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_status_paid
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_isp_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_metric_peso
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_read_only
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_topsheets_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_topsheets_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_topsheets_title
import org.jetbrains.compose.resources.stringResource

/**
 * Every topsheet, whatever period and whatever stage.
 *
 * Read-only, and it says so in a line above the list rather than by having rows
 * that do nothing when clicked. A manager looking for the approve button should
 * find out where it lives, not that this one is broken.
 */
@Composable
internal fun TopSheetsTab(
    uiState: ManagerUIState,
    callback: ManagerCallback,
    isCompact: Boolean,
) {
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.topSheetQuery,
            onValueChange = callback.onTopSheetQueryChange,
            placeholder = stringResource(Res.string.manager_topsheets_search_hint),
            modifier = modifier,
        )
    }

    SectionCard(
        title = stringResource(Res.string.manager_topsheets_title),
        icon = AppIcons.Description,
        trailing = if (isCompact) null else ({ search(Modifier.width(Dimensions.viewWidth280)) }),
    ) {
        if (isCompact) {
            search(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding12))
        }

        Text(
            text = stringResource(Res.string.manager_read_only),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Dimensions.viewPadding16))

        when {
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.console_retry),
            )

            uiState.visibleTopSheets.isEmpty() ->
                SectionEmptyState(stringResource(Res.string.manager_topsheets_empty))

            else -> LazyColumn(
                // Bounded so the list scrolls within the page rather than
                // stretching it to the length of the whole billing history.
                modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                items(uiState.visibleTopSheets, key = { it.id }) { sheet ->
                    TopSheetRow(sheet, isCompact)
                }
            }
        }
    }
}

@Composable
private fun TopSheetRow(sheet: ManagerTopSheetRow, isCompact: Boolean) {
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
                    TopSheetStatusChip(sheet.status)
                }

                Spacer(Modifier.height(Dimensions.viewPadding8))

                Text(
                    text = stringResource(Res.string.manager_isp_accounts, sheet.accountCount) +
                        " · ${sheet.period} · ${sheet.compiledOn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        @Composable
        fun total(alignment: Alignment.Horizontal) {
            Column(horizontalAlignment = alignment) {
                Text(
                    text = stringResource(Res.string.manager_metric_peso, sheet.totalValidated),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        if (isCompact) {
            identity()
            Spacer(Modifier.height(Dimensions.viewPadding12))
            total(Alignment.Start)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                identity()
                total(Alignment.End)
            }
        }
    }
}

/**
 * Where a sheet has got to.
 *
 * Neutral throughout: a topsheet's progress is not good or bad news, and
 * colouring it would compete with the figures beside it. Same call the secretary
 * console makes for the same pill.
 */
@Composable
private fun TopSheetStatusChip(status: TopSheetRecordStatus) {
    StatusChip(
        label = stringResource(
            when (status) {
                TopSheetRecordStatus.Compiled -> Res.string.console_status_compiled
                TopSheetRecordStatus.Approved -> Res.string.console_status_approved
                TopSheetRecordStatus.Paid -> Res.string.console_status_paid
            },
        ),
        tone = ChipTone.Neutral,
    )
}
