package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Immutable

/**
 * UI state for the full-screen topsheet detail view.
 *
 * The header is read from [TopSheetRowHolder] on init and may be null if the
 * process was restored without it. Lines are fetched from the API behind a
 * spinner; the search/sort fields drive [visibleLines].
 */
@Immutable
data class TopSheetDetailUIState(
    /** The topsheet's identity + totals, drawn from the holder. */
    val header: TopSheetRow? = null,

    /** Every account line returned by the API, before filtering. */
    val lines: List<TopSheetLineRow> = emptyList(),
    val isLoadingLines: Boolean = false,
    val linesError: String? = null,

    /** Free-text filter over store code, store name and account number. */
    val lineQuery: String = "",

    /** The column the list is ordered by. */
    val lineSort: TopSheetLineSortKey = TopSheetLineSortKey.RfpNumber,

    /** Whether the list is ascending; the backend's default is descending store code. */
    val lineSortAsc: Boolean = true,

    /** Account detail modal — opened when a line card is tapped. */
    val accountDetail: AccountDetail? = null,
) {

    /**
     * The open topsheet's account lines as displayed: filtered by the combined
     * search (store code / store name / account number) and ordered by the chosen
     * key and direction. Amount sorts by the prorated (billed) value.
     */
    val visibleLines: List<TopSheetLineRow>
        get() {
            val query = lineQuery.trim()
            val filtered = lines.filter { line ->
                query.isBlank() ||
                    line.storeCode?.contains(query, ignoreCase = true) == true ||
                    line.storeName?.contains(query, ignoreCase = true) == true ||
                    line.accountNumber?.contains(query, ignoreCase = true) == true
            }
            val ordered = when (lineSort) {
                TopSheetLineSortKey.StoreCode ->
                    filtered.sortedWith(compareBy(nullsLast()) { it.storeCode })
                TopSheetLineSortKey.Amount ->
                    filtered.sortedBy { it.proratedCents }
                // Numeric order of the assigned RFP; unassigned lines fall last,
                // keeping the backend's rfpSortOrder for ties (stable sort).
                TopSheetLineSortKey.RfpNumber ->
                    filtered.sortedWith(compareBy(nullsLast()) { it.rfpNumber?.toLongOrNull() })
            }
            return if (lineSortAsc) ordered else ordered.asReversed()
        }
}
