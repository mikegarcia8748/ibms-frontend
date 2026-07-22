package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import androidx.compose.runtime.Immutable

/**
 * The rows the registry panels draw. See `SysadminModels.kt` for why these are
 * not the shared domain models.
 */

@Immutable
data class BranchRow(
    val id: String,
    val branchCode: String,
    val name: String,
    val city: String?,
    /**
     * Every ISP this branch holds an account with — what the ISP filter matches.
     *
     * A set, not a single id, because a branch commonly runs more than one line
     * (a primary and a backup on different providers), and the backend has no
     * store→provider relation of its own to pick a winner from.
     */
    val providerIds: Set<String>,
    val isActive: Boolean,
) {
    /**
     * The letter this branch files under in the A–Z rail. Digits and anything
     * else fall under `#` so no row is unreachable from the rail.
     */
    val indexLetter: Char
        get() = name.firstOrNull()?.uppercaseChar()?.takeIf { it in 'A'..'Z' } ?: '#'
}

@Immutable
data class IspAccountRow(
    val id: String,
    val accountNumber: String,
    val storeName: String,
    val providerId: String,
    /** Decimal string, already grouped for display, e.g. `"2,489.99"`. */
    val monthlyRate: String,
    val isActive: Boolean,
)

/** The summary returned by a bulk import, mirroring the backend's payload. */
@Immutable
data class BulkImportSummary(
    val providers: List<ProviderImportSummary>,
    val storesCreated: Int,
    val storesReused: Int,
    val accountsCreated: Int,
    val accountsReused: Int,
    val rowsSkipped: Int,
    val skipReasons: List<String>,
    val totalRows: Int,
)

/**
 * One ISP's share of an import.
 *
 * [created] is kept — unlike in the single-provider summary it replaced, it now
 * carries information a sysadmin can act on: a spreadsheet that silently
 * introduces a *new* ISP usually means a misspelt name in the source file.
 */
@Immutable
data class ProviderImportSummary(
    val name: String,
    val created: Boolean,
    val accountsCreated: Int,
    val accountsReused: Int,
)
