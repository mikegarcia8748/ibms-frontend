package com.puregoldgo.ibms.ui.screen.secretary.compile

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.domain.BillingPeriod
import com.puregoldgo.ibms.ui.component.LETTER_ALL
import com.puregoldgo.ibms.ui.screen.secretary.SecretaryProviderRow

/**
 * State of the Compile TopSheet panel.
 *
 * Filtering and the enable/disable rules are computed here rather than in the
 * composables, so they are testable and a preview can show any screen of the flow
 * without a running ViewModel. [billingPeriod] is `YYYY-MM`.
 */
@Immutable
data class CompileUIState(
    val billingPeriod: String = "",
    val selectedProviderId: String? = null,
    val providers: List<SecretaryProviderRow> = emptyList(),

    val phase: CompilePhase = CompilePhase.Review,

    // Review — the accounts-for-review list and its context total.
    val reviewRows: List<CompileAccountRow> = emptyList(),
    val totalAmount: String = "0.00",
    val query: String = "",
    val letter: Char = LETTER_ALL,
    val isLoading: Boolean = false,
    val loadError: String? = null,

    // Compile action.
    val isCompiling: Boolean = false,
    val compileError: String? = null,

    // RFP entry.
    val draft: TopSheetSummary? = null,
    val lines: List<CompileLineRow> = emptyList(),
    val isLoadingLines: Boolean = false,
    val linesError: String? = null,
    val isConfirming: Boolean = false,
    val confirmError: String? = null,

    // Result.
    val compiled: TopSheetSummary? = null,
) {
    /** Total accounts in the current context — the count before the list filters narrow it. */
    val totalCount: Int
        get() = reviewRows.size

    /** The forward arrow greys out once the next month would be in the future. */
    val canGoNextMonth: Boolean
        get() = billingPeriod.isNotEmpty() && !BillingPeriod.isFuture(BillingPeriod.next(billingPeriod))

    /** The letters the rail offers — only those that file a review row. */
    val letters: List<Char>
        get() = reviewRows.map { it.indexLetter }.distinct().sorted()

    /** Letter and free-text compose; search matches the account number or the store. */
    val visibleRows: List<CompileAccountRow>
        get() = reviewRows
            .filter { letter == LETTER_ALL || it.indexLetter == letter }
            .filter { row ->
                query.isBlank() || query.trim().let { q ->
                    row.accountNumber.contains(q, ignoreCase = true) ||
                        row.storeName.contains(q, ignoreCase = true)
                }
            }
            .sortedBy { it.storeName }

    /** Compile is offered only once an ISP is chosen and it has accounts to bill. */
    val canCompile: Boolean
        get() = selectedProviderId != null &&
            reviewRows.isNotEmpty() &&
            !isCompiling &&
            !isLoading &&
            loadError == null

    /** Confirmation waits until every line has a saved RFP number. */
    val canConfirm: Boolean
        get() = lines.isNotEmpty() &&
            lines.all { it.hasSavedRfp } &&
            !isConfirming &&
            !isLoadingLines
}
