package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Composable
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.StatusChip
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_active
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_approved
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_closed
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_compiled
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_for_deactivation
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_inactive
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_paid
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_pending
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_status_terminated
import org.jetbrains.compose.resources.stringResource

/**
 * The status pills, defined once so a standing never renders two ways on two
 * tabs — an account that reads `PENDING` amber in the database must not read
 * grey in the archive.
 *
 * Tone carries the meaning: `Accent` is the theme's yellow, which is what makes
 * "needs a human" (pending, awaiting deactivation) legible at a glance without
 * looking like the red of something already dead.
 */

@Composable
internal fun AccountStatusChip(status: AccountRecordStatus) {
    StatusChip(
        label = stringResource(status.labelRes()),
        tone = when (status) {
            AccountRecordStatus.Active -> ChipTone.Positive
            AccountRecordStatus.Pending -> ChipTone.Accent
            AccountRecordStatus.ForDeactivation -> ChipTone.Accent
            AccountRecordStatus.Terminated -> ChipTone.Negative
        },
    )
}

@Composable
internal fun BranchStatusChip(status: BranchRecordStatus) {
    StatusChip(
        label = stringResource(
            when (status) {
                BranchRecordStatus.Active -> Res.string.secretary_status_active
                BranchRecordStatus.Closed -> Res.string.secretary_status_closed
                BranchRecordStatus.Inactive -> Res.string.secretary_status_inactive
            },
        ),
        tone = when (status) {
            BranchRecordStatus.Active -> ChipTone.Positive
            BranchRecordStatus.Closed -> ChipTone.Negative
            BranchRecordStatus.Inactive -> ChipTone.Negative
        },
    )
}

@Composable
internal fun TopSheetStatusChip(status: TopSheetRecordStatus) {
    StatusChip(
        label = stringResource(
            when (status) {
                TopSheetRecordStatus.Compiled -> Res.string.secretary_status_compiled
                TopSheetRecordStatus.Approved -> Res.string.secretary_status_approved
                TopSheetRecordStatus.Paid -> Res.string.secretary_status_paid
            },
        ),
        // Neutral throughout: a topsheet's progress is not good or bad news, and
        // colouring it would compete with the account pills beside it.
        tone = ChipTone.Neutral,
    )
}

/** The status filter's entries, worded exactly as the pills they select. */
@Composable
internal fun accountStatusFilterOptions(): List<FilterOption> =
    AccountRecordStatus.entries.map { status ->
        FilterOption(id = status.name, label = stringResource(status.labelRes()))
    }

private fun AccountRecordStatus.labelRes() = when (this) {
    AccountRecordStatus.Active -> Res.string.secretary_status_active
    AccountRecordStatus.Pending -> Res.string.secretary_status_pending
    AccountRecordStatus.ForDeactivation -> Res.string.secretary_status_for_deactivation
    AccountRecordStatus.Terminated -> Res.string.secretary_status_terminated
}

// Named apart rather than overloaded: both erase to `toFilterOptions(List)` on
// the JVM target, which is a declaration clash.

internal fun List<SecretaryProviderRow>.providerFilterOptions(): List<FilterOption> =
    map { FilterOption(id = it.id, label = it.name) }

internal fun List<SecretaryBranchRow>.branchFilterOptions(): List<FilterOption> =
    map { FilterOption(id = it.id, label = it.displayName) }
