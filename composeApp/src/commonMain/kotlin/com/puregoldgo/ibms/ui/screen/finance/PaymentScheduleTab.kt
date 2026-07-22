package com.puregoldgo.ibms.ui.screen.finance

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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_schedule_awaiting
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_schedule_clear
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_schedule_day
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_schedule_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_schedule_title
import org.jetbrains.compose.resources.stringResource

/**
 * Each active ISP's standing payment day, and what is queued against it.
 *
 * Ordered by the day rather than by name: the question this panel answers is
 * "what is due next", and a list alphabetised by vendor makes that a search.
 */
@Composable
internal fun PaymentScheduleTab(
    uiState: FinanceUIState,
    callback: FinanceCallback,
    isCompact: Boolean,
) {
    SectionCard(
        title = stringResource(Res.string.finance_schedule_title),
        icon = AppIcons.Wifi,
    ) {
        when {
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.console_retry),
            )

            uiState.paymentSchedule.isEmpty() ->
                SectionEmptyState(stringResource(Res.string.finance_schedule_empty))

            else -> Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                // Not a LazyColumn: there are as many rows as there are active
                // ISPs — a handful — and the whole point is seeing them at once.
                uiState.paymentSchedule.forEach { row ->
                    ScheduleRow(row, isCompact)
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(row: PaymentScheduleRow, isCompact: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        @Composable
        fun identity() {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                StatusChip(
                    label = stringResource(Res.string.finance_schedule_day, row.paymentScheduleDay),
                    tone = ChipTone.Accent,
                )
                Text(
                    text = row.providerName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        @Composable
        fun queued() {
            Text(
                text = if (row.awaitingPayment == 0) {
                    stringResource(Res.string.finance_schedule_clear)
                } else {
                    stringResource(
                        Res.string.finance_schedule_awaiting,
                        row.awaitingPayment,
                        row.awaitingTotal,
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (row.awaitingPayment == 0) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }

        if (isCompact) {
            identity()
            Spacer(Modifier.height(Dimensions.viewPadding8))
            queued()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                identity()
                queued()
            }
        }
    }
}
