package com.puregoldgo.ibms.ui.screen.finance

import androidx.compose.runtime.Immutable

/** Which of the three panels the finance console is showing. */
enum class FinanceTab { Approvals, Payments, PaymentSchedule }

@Immutable
data class FinanceUIState(
    // Who is signed in — drawn in the app bar.
    val userName: String = "",
    val userRole: String = "",

    val selectedTab: FinanceTab = FinanceTab.Approvals,

    // Data. Sample-fed this pass; both lists come from the API once wired.
    val providers: List<FinanceProviderRow> = emptyList(),
    val topSheets: List<FinanceTopSheetRow> = emptyList(),

    // Loading. One flag and one message for both lists: the schedule is a join
    // of the two, so a panel with only one of them in would draw rows that are
    // quietly wrong.
    val isLoading: Boolean = false,
    val loadError: String? = null,

    // Approvals filters.
    val approvalQuery: String = "",
    val approvalProviderId: String? = null,

    // Payments filters.
    val paymentQuery: String = "",
    val paymentProviderId: String? = null,

    /** The sheet whose approval or payment is awaiting confirmation. */
    val pendingAction: TopSheetAction? = null,
    val isSubmitting: Boolean = false,
    /** The server's own wording, shown verbatim — it names what to go fix. */
    val actionError: String? = null,
) {
    /** Only active ISPs are offered as a filter — a dead one matches nothing. */
    val activeProviders: List<FinanceProviderRow>
        get() = providers.filter { it.isActive }

    /**
     * Sheets waiting on an approval, newest period first.
     *
     * Only [TopSheetRecordStatus.Compiled] appears: this tab is a queue, and a
     * sheet already approved has left it. The billing history that keeps
     * everything lives on the secretary console.
     */
    val visibleApprovals: List<FinanceTopSheetRow>
        get() = topSheets
            .filter { it.status == TopSheetRecordStatus.Compiled }
            .filterBy(approvalQuery, approvalProviderId)
            .sortedByDescending { it.period }

    /** Approved sheets not yet released, oldest period first — the oldest is the most overdue. */
    val visiblePayments: List<FinanceTopSheetRow>
        get() = topSheets
            .filter { it.status == TopSheetRecordStatus.Approved }
            .filterBy(paymentQuery, paymentProviderId)
            .sortedBy { it.period }

    /**
     * Every active provider's payment day with its queue against it, ordered by
     * the day itself.
     *
     * Providers with nothing queued are kept rather than dropped: "nothing due
     * on the 5th" is the answer finance is looking for on the 4th, and an empty
     * row states it where a missing row would just look like a load that failed.
     */
    val paymentSchedule: List<PaymentScheduleRow>
        get() = activeProviders
            .map { provider ->
                val queued = topSheets.filter {
                    it.providerId == provider.id && it.status == TopSheetRecordStatus.Approved
                }
                PaymentScheduleRow(
                    providerId = provider.id,
                    providerName = provider.name,
                    paymentScheduleDay = provider.paymentScheduleDay,
                    awaitingPayment = queued.size,
                    awaitingTotal = queued.sumOfPesos(),
                )
            }
            .sortedWith(compareBy({ it.paymentScheduleDay }, { it.providerName }))

    /** Sheets on the approvals queue that do not balance — the count the header leads with. */
    val varianceCount: Int
        get() = visibleApprovals.count { it.varianceCount > 0 }

    /** Nothing in flight, so the confirm button may fire. */
    val canConfirmAction: Boolean
        get() = pendingAction != null && !isSubmitting

    private fun List<FinanceTopSheetRow>.filterBy(
        query: String,
        providerId: String?,
    ): List<FinanceTopSheetRow> = this
        .filter { providerId == null || it.providerId == providerId }
        .filter { sheet ->
            query.isBlank() || query.trim().let { q ->
                sheet.invoiceNumber.contains(q, ignoreCase = true) ||
                    sheet.providerName.contains(q, ignoreCase = true) ||
                    sheet.period.contains(q, ignoreCase = true)
            }
        }
}

/**
 * An approval or a payment, held until it is confirmed.
 *
 * Both are irreversible — the backend models no way back from either — so
 * neither fires off a row button directly. One nullable field rather than two
 * targets: only one of them can be pending, and the [kind] says which.
 */
@Immutable
data class TopSheetAction(
    val sheet: FinanceTopSheetRow,
    val kind: Kind,
) {
    enum class Kind { Approve, Pay }
}

/**
 * Sums peso strings that are already grouped for display, e.g. `"760,172.68"`.
 *
 * Works on the strings rather than parsing to a number, for the reason
 * `formatMoney` states: these are billing figures, and a round trip through
 * `Double` could change one. Kotlin Multiplatform has no `BigDecimal` in common
 * code, so this adds the centavos as `Long` and regroups the result.
 */
internal fun List<FinanceTopSheetRow>.sumOfPesos(): String {
    val centavos = sumOf { row ->
        val digits = row.totalValidated.filter { it.isDigit() || it == '.' }
        val dot = digits.indexOf('.')
        val whole = (if (dot >= 0) digits.substring(0, dot) else digits).toLongOrNull() ?: 0L
        val fraction = if (dot >= 0) digits.substring(dot + 1).take(2).padEnd(2, '0') else "00"
        whole * 100 + (fraction.toLongOrNull() ?: 0L)
    }

    val grouped = (centavos / 100).toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return "$grouped.${(centavos % 100).toString().padStart(2, '0')}"
}
