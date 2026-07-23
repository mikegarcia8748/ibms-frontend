package com.puregoldgo.ibms.ui.screen.secretary.compile

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.ui.screen.secretary.AccountRecordStatus

/** Which of the compile panel's three screens is showing. */
enum class CompilePhase { Review, RfpEntry, Compiled }

/** How the drafted-account (RFP-entry) list is ordered for display. */
enum class LineSortOrder { StoreCode, Alphabetical, MonthlyRecurringCharge }

/**
 * One account in the "Accounts for Review" list.
 *
 * Fed from two sources with the same shape: the full accounts DB (browse, "All
 * Providers") and a provider's eligible-account preview. [amount] is the decimal
 * this row contributes — the monthly rate in browse, the prorated amount in
 * preview — and is grouped for display by [groupPeso].
 */
@Immutable
data class CompileAccountRow(
    val accountId: String,
    val accountNumber: String,
    val storeName: String,
    val providerName: String,
    val billingDay: Int?,
    val amount: String, // decimal-as-string
    val status: AccountRecordStatus,
) {
    /** Files under the store it serves; digits and symbols fall under `#`. */
    val indexLetter: Char
        get() = storeName.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#'
}

/**
 * One editable line on the RFP-entry screen.
 *
 * [savedRfpNumber] is what the server holds; [rfpInput] is the edit buffer. They
 * diverge while the secretary is typing and re-converge once a PATCH lands —
 * [hasUnsavedRfp] is that gap, and confirmation is blocked until every line has a
 * saved number.
 */
@Immutable
data class CompileLineRow(
    val id: String,
    val accountId: String,
    val storeName: String,
    val branchCode: String?,
    val circuitId: String?,
    val accountNumber: String?,
    val proratedAmount: String,
    val fullAmount: String,
    val rfpSortOrder: Int?,
    val savedRfpNumber: String?,
    val rfpInput: String,
    val isSavingRfp: Boolean = false,
    val isRemoving: Boolean = false,
    val rowError: String? = null,
) {
    val hasUnsavedRfp: Boolean
        get() = rfpInput != savedRfpNumber.orEmpty()

    val hasSavedRfp: Boolean
        get() = !savedRfpNumber.isNullOrBlank()

    /** Files under the store it serves; digits and symbols fall under `#`. */
    val indexLetter: Char
        get() = storeName.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#'

    /** Prorated when the billed amount differs from the full monthly rate (MRC). */
    val isProrated: Boolean
        get() = proratedAmount.toCents() != fullAmount.toCents()
}

// ─── Mappers ──────────────────────────────────────────────────────────────────

/** Browse rows: every billable account joined to its store and provider. */
internal fun buildBrowseRows(
    accounts: List<Account>,
    stores: List<Store>,
    providers: List<Provider>,
): List<CompileAccountRow> {
    val storesById = stores.associateBy { it.id }
    val providersById = providers.associateBy { it.id }
    return accounts
        .filter { it.status == AccountStatus.ACTIVE || it.status == AccountStatus.TERMINATION_REQUESTED }
        .map { account ->
            val store = storesById[account.storeId]
            val provider = providersById[account.providerId]
            CompileAccountRow(
                accountId = account.id,
                accountNumber = account.accountNumber,
                storeName = store.displayName(account.storeId),
                providerName = provider?.name ?: EM_DASH,
                billingDay = provider?.paymentScheduleDay,
                amount = account.rate,
                status = account.status.toRecordStatus(),
            )
        }
}

/** Preview rows: a provider's eligible accounts, at their prorated amounts. */
internal fun buildPreviewRows(
    preview: CompilePreview,
    provider: Provider?,
): List<CompileAccountRow> = preview.lines.map { line ->
    CompileAccountRow(
        accountId = line.accountId,
        accountNumber = line.accountNumber,
        storeName = line.storeName ?: line.branchCode ?: line.accountNumber,
        providerName = provider?.name ?: EM_DASH,
        billingDay = provider?.paymentScheduleDay,
        amount = line.proratedAmount,
        // Preview returns only eligible (billable) accounts.
        status = AccountRecordStatus.Active,
    )
}

/** Draft lines → editable rows, seeding each edit buffer with the saved RFP number. */
internal fun TopSheetLine.toLineRow(): CompileLineRow = CompileLineRow(
    id = id,
    accountId = accountId,
    storeName = storeName ?: branchCode ?: accountNumber.orEmpty(),
    branchCode = branchCode,
    circuitId = circuitId,
    accountNumber = accountNumber,
    proratedAmount = proratedAmount,
    fullAmount = fullAmount,
    rfpSortOrder = rfpSortOrder,
    savedRfpNumber = rfpNumber,
    rfpInput = rfpNumber.orEmpty(),
)

private fun Store?.displayName(fallback: String): String {
    if (this == null) return fallback
    return if (city.isNullOrBlank()) name else "$name ($city)"
}

private fun AccountStatus.toRecordStatus(): AccountRecordStatus = when (this) {
    AccountStatus.ACTIVE -> AccountRecordStatus.Active
    AccountStatus.TERMINATION_REQUESTED -> AccountRecordStatus.ForDeactivation
    AccountStatus.TERMINATED -> AccountRecordStatus.Terminated
    AccountStatus.TRANSFERRED -> AccountRecordStatus.Terminated
    AccountStatus.INACTIVE -> AccountRecordStatus.Terminated
}

private const val EM_DASH = "—"

// ─── Money ─────────────────────────────────────────────────────────────────────

/**
 * Sums 2dp decimal strings exactly, in integer centavos — no `BigDecimal` exists
 * in common code, and `Double` would drift over a couple hundred rows of billing.
 * Returns a plain `"NNN.NN"` string.
 */
internal fun sumMoney(amounts: List<String>): String =
    formatCents(amounts.sumOf { it.toCents() })

/** `"1998.5"` / `"1998"` / `"1,998.00"` → centavos. Unparseable parts count as zero. */
internal fun String.toCents(): Long {
    val cleaned = replace(",", "").trim()
    if (cleaned.isEmpty()) return 0L
    val negative = cleaned.startsWith("-")
    val digits = cleaned.trimStart('-')
    val parts = digits.split(".")
    val whole = parts[0].toLongOrNull() ?: 0L
    val frac = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2).toLongOrNull() ?: 0L
    val cents = whole * 100 + frac
    return if (negative) -cents else cents
}

private fun formatCents(cents: Long): String {
    val whole = cents / 100
    val frac = (cents % 100).let { if (it < 0) -it else it }
    return "$whole.${frac.toString().padStart(2, '0')}"
}

/** `"760172.68"` → `"760,172.68"` for display. Leaves an unparseable value as-is. */
internal fun String.groupPeso(): String {
    val cleaned = replace(",", "").trim()
    val negative = cleaned.startsWith("-")
    val digits = cleaned.trimStart('-')
    val parts = digits.split(".")
    val whole = parts[0].ifEmpty { "0" }
    val frac = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2)
    if (whole.any { !it.isDigit() }) return this
    val grouped = whole.reversed().chunked(3).joinToString(",").reversed()
    return (if (negative) "-" else "") + "$grouped.$frac"
}
