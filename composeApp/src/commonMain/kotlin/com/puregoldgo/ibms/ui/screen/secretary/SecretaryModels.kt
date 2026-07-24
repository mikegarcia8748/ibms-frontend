package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.platform.file.PickedFile

/**
 * The rows the secretary panel draws.
 *
 * Deliberately *not* the shared domain models, for the same reason the sysadmin
 * panel keeps its own — see the header of `SysadminModels.kt`. This screen
 * needs things those models cannot express: a branch's closure reason, a
 * topsheet's validated total, and an account status of `PENDING` that has no
 * counterpart in `AccountStatus` at all. Rather than bend the UI around models
 * that are already scheduled to be replaced, the screen states what it needs and
 * the wiring pass supplies mappers into these types. Nothing in the composables
 * moves when that happens.
 */

/**
 * Where an ISP account stands.
 *
 * [Pending] is a UI-level standing with no wire counterpart yet: an account that
 * has been recorded but whose line the provider has not confirmed. [ForDeactivation]
 * is the 30-day grace `POST /accounts/{id}/deactivate` opens, which the backend
 * calls `termination_requested`.
 */
enum class AccountRecordStatus { Active, Pending, ForDeactivation, Terminated }

/** Where a branch stands. [Closed] is permanent; [Inactive] is not. */
enum class BranchRecordStatus { Active, Closed, Inactive }

/**
 * A topsheet's progress. [Draft] is a topsheet still being compiled — it has no
 * invoice number yet and is edited from the Compile flow, but it appears in
 * Billing History so nothing in flight is invisible.
 */
enum class TopSheetRecordStatus { Draft, Compiled, Approved, Paid }

@Immutable
data class SecretaryProviderRow(
    val id: String,
    val name: String,
    val isActive: Boolean,
)

@Immutable
data class SecretaryBranchRow(
    val id: String,
    val branchCode: String,
    val name: String,
    val city: String?,
    /**
     * Every ISP this branch holds an account with — what the ISP filter matches.
     *
     * A set, not a single id, because a branch commonly runs more than one line
     * (a primary and a backup on different providers).
     */
    val providerIds: Set<String>,
    val status: BranchRecordStatus,
    /** Why it closed — shown parenthesised in the archive. Null while it is open. */
    val closureReason: String? = null,
) {
    /**
     * The letter this branch files under in the A–Z rail. Digits and anything
     * else fall under `#` so no row is unreachable from the rail.
     */
    val indexLetter: Char
        get() = name.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#'

    /** `NAME (City)` when a city is known, plain name otherwise. */
    val displayName: String
        get() = if (city.isNullOrBlank()) name else "$name ($city)"

    val isClosed: Boolean
        get() = status == BranchRecordStatus.Closed
}

@Immutable
data class SecretaryAccountRow(
    val id: String,
    val accountNumber: String,
    /** The branch this line terminates at — what makes an account float when it closes. */
    val storeId: String,
    val storeName: String,
    val providerId: String,
    /** Decimal string, already grouped for display, e.g. `"2,489.99"`. */
    val monthlyRate: String,
    val status: AccountRecordStatus,
) {
    /** Files under the store it serves — the accounts rail indexes by branch, not by number. */
    val indexLetter: Char
        get() = storeName.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#'

    /** Still billing. Only these can float, and only these are kept out of the archive. */
    val isLive: Boolean
        get() = status == AccountRecordStatus.Active || status == AccountRecordStatus.Pending
}

@Immutable
data class TopSheetRow(
    val id: String,
    /** Null while still a draft — the invoice is minted only at confirm. */
    val invoiceNumber: String?,
    /** The compilation batch id, assigned at draft creation. Shown when there is no invoice yet. */
    val batchNumber: String? = null,
    val providerName: String,
    /** Billing period as `YYYY-MM`, the form `POST /topsheets/compile` takes. */
    val period: String,
    /** The day it was compiled, already formatted for display. */
    val generatedOn: String,
    val accountCount: Int,
    /** Peso total, already grouped, e.g. `"760,172.68"`. */
    val totalValidated: String,
    val status: TopSheetRecordStatus,
) {
    /** What identifies the sheet on screen: its invoice, else its batch, else a draft label. */
    val reference: String
        get() = invoiceNumber ?: batchNumber ?: DRAFT_REFERENCE
}

private const val DRAFT_REFERENCE = "DRAFT"

/** The add-branch form. Mirrors what `POST /stores` will take. */
@Immutable
data class NewBranchForm(
    val branchCode: String = "",
    val name: String = "",
    val city: String = "",
    val providerId: String? = null,
)

/** The add-account form. Mirrors what `POST /accounts` will take. */
@Immutable
data class NewAccountForm(
    val accountNumber: String = "",
    val storeId: String? = null,
    val providerId: String? = null,
    val monthlyRate: String = "",
    val installationDate: String = "",
    val circuitId: String = "",
    val billingPeriodLabel: String = "",
    val planName: String = "",
    val proofAttachmentIds: List<String> = emptyList(),
    val proofUploadError: String? = null,
    val isSubmitting: Boolean = false,
)

/**
 * The deactivate-account confirmation form.
 *
 * Holds the account to terminate and the proof PDF the secretary selected.
 * The file is kept in memory only this pass; the actual upload and
 * deactivation request are TODO.
 */
@Immutable
data class DeactivateAccountForm(
    val accountId: String,
    val accountNumber: String,
    val circuitId: String?,
    val branchLabel: String,
    val proofFile: PickedFile? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)
