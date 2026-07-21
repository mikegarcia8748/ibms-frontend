package com.puregoldgo.ibms.ui.screen.finance

import androidx.compose.runtime.Immutable

/**
 * The rows the finance console draws.
 *
 * Console-local rather than the shared domain models, for the reason recorded in
 * `SysadminModels.kt`: those have drifted from the backend and cannot express
 * what this screen shows — a topsheet's validated total, the variance against
 * what was billed, or which provider is due on which day. The wiring pass
 * supplies mappers into these types and nothing in the composables moves.
 */

/**
 * A compiled topsheet's progress.
 *
 * Finance owns the two transitions out of [Compiled]: `POST /topsheets/{id}/approve`
 * and then `POST /topsheets/{id}/pay`. Nothing here can send one back — the
 * backend models no such move, and a button that looked like it could would be
 * lying about what happens to money already released.
 */
enum class TopSheetRecordStatus { Compiled, Approved, Paid }

@Immutable
data class FinanceProviderRow(
    val id: String,
    val name: String,
    /** Day of month the provider is paid on, 1..31. */
    val paymentScheduleDay: Int,
    val isActive: Boolean,
)

@Immutable
data class FinanceTopSheetRow(
    val id: String,
    val invoiceNumber: String,
    val providerId: String,
    val providerName: String,
    /** Billing period as `YYYY-MM`, the form `POST /topsheets/compile` takes. */
    val period: String,
    /** The day the secretary compiled it, already formatted for display. */
    val compiledOn: String,
    val accountCount: Int,
    /** Peso total, already grouped, e.g. `"760,172.68"`. */
    val totalValidated: String,
    /**
     * Accounts on the sheet whose billed amount did not match the contracted
     * rate. Finance approves against this number, not the total — a sheet that
     * balances is a formality, one that does not is the whole job.
     */
    val varianceCount: Int,
    val status: TopSheetRecordStatus,
)

/**
 * One provider's standing payment day, and what is queued against it.
 *
 * Derived rather than fetched: the backend has no "schedule" resource, only a
 * `paymentScheduleDay` on the provider and the topsheets that name it. The
 * console does that join, the way the sysadmin panel joins stores to accounts.
 */
@Immutable
data class PaymentScheduleRow(
    val providerId: String,
    val providerName: String,
    val paymentScheduleDay: Int,
    /** Approved but not yet paid, for this provider. */
    val awaitingPayment: Int,
    /** Peso total of those sheets, already grouped. */
    val awaitingTotal: String,
)
