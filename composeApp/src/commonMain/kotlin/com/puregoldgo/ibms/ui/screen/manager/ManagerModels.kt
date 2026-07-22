package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.runtime.Immutable

/**
 * The rows the manager console draws.
 *
 * Console-local rather than the shared domain models, for the reason recorded in
 * `SysadminModels.kt`. This screen needs one thing none of them has at all: a
 * period's spend broken down by ISP, which exists only as a sum over topsheets.
 *
 * Everything here is read-only. The role has no write endpoint in the contract —
 * compilation belongs to the secretary, approval and payment to finance — so
 * these types carry no forms and no actions, deliberately.
 */

/** A compiled topsheet's progress, as the oversight view sees it. */
enum class TopSheetRecordStatus { Compiled, Approved, Paid }

@Immutable
data class ManagerTopSheetRow(
    val id: String,
    val invoiceNumber: String,
    val providerName: String,
    /** Billing period as `YYYY-MM`. */
    val period: String,
    val compiledOn: String,
    val accountCount: Int,
    /** Peso total, already grouped, e.g. `"760,172.68"`. */
    val totalValidated: String,
    val status: TopSheetRecordStatus,
)

/**
 * One ISP's share of a period.
 *
 * [share] is a formatted percentage rather than a number: it is only ever drawn,
 * and computing it here would put a rounding rule in the composables.
 */
@Immutable
data class IspSpendRow(
    val providerName: String,
    val total: String,
    val share: String,
    val accountCount: Int,
)

/**
 * One line of the audit trail, from `GET /activities`.
 *
 * [summary] is the server's own phrasing of what happened. Restating it here in
 * the app's words would mean two descriptions of the same event that can drift.
 */
@Immutable
data class ActivityRow(
    val id: String,
    val actorName: String,
    val actorRole: String,
    val summary: String,
    /** Already formatted for display — the API sends an instant, not a label. */
    val occurredAt: String,
)
