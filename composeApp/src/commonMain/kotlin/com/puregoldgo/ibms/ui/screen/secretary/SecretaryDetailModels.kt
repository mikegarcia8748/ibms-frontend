package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Immutable

@Immutable
data class StoreDetail(
    val branchCode: String,
    val name: String,
    val status: BranchRecordStatus,
    val region: String?,
    val province: String?,
    val city: String?,
    val barangay: String?,
    val postal: String?,
    val registeredOn: String?,
    val lastUpdated: String?,
    val linkedAccounts: List<AccountLink>,
)

@Immutable
data class AccountLink(
    val accountId: String,
    val accountNumber: String,
    val circuitId: String?,
    val status: AccountRecordStatus,
)

/**
 * A single account line within a topsheet, as drawn in [TopSheetDetailsDialog].
 *
 * Carries [accountId] so tapping the row can open the account detail modal, and
 * the raw centavo amounts / RFP number so the dialog can sort without re-parsing
 * the grouped display strings.
 */
@Immutable
data class TopSheetLineRow(
    val accountId: String,
    val rfpNumber: String?,
    val storeCode: String?,
    val storeName: String?,
    val accountNumber: String?,
    val circuitId: String?,
    /** Full monthly recurring charge, grouped for display, e.g. `"2,489.99"`. */
    val fullMrc: String,
    /** Billed (prorated) amount, grouped for display. */
    val prorated: String,
    /** Prorated amount in centavos — the sort key for [TopSheetLineSortKey.Amount]. */
    val proratedCents: Long,
    /** Backend display order (store code descending); the default sort. */
    val rfpSortOrder: Int?,
)

/** The columns the topsheet-detail list can be ordered by. */
enum class TopSheetLineSortKey { StoreCode, Amount, RfpNumber }

/**
 * A compiled topsheet's full record: its header (reused from the list row) plus
 * the account lines fetched on demand from `GET /topsheets/{id}/lines`.
 *
 * [lines] is empty while [isLoadingLines] is true; [linesError] holds the message
 * when the fetch fails. The dialog searches and sorts [lines] for display.
 */
@Immutable
data class TopSheetDetail(
    val header: TopSheetRow,
    val lines: List<TopSheetLineRow> = emptyList(),
    val isLoadingLines: Boolean = false,
    val linesError: String? = null,
)

@Immutable
data class AccountDetail(
    val accountNumber: String,
    val providerName: String,
    val planName: String?,
    val circuitId: String?,
    val storeName: String,
    val branchCode: String,
    val monthlyRate: String,
    val speed: String?,
    val contractDurationMonths: Int?,
    val installationDate: String?,
    val createdAt: String?,
    val contractStartDate: String?,
    val contractEndDate: String?,
    val status: AccountRecordStatus,
)
