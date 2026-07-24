package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.ui.component.LETTER_ALL
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import com.puregoldgo.ibms.ui.screen.sysadmin.IspProviderRow

/**
 * The branch and ISP-account registries, and the bulk import that fills them.
 *
 * The two panels share a state because they share a fetch: accounts supply each
 * branch's ISPs and stores supply each account's display name, so neither list
 * is drawable until both are in. Splitting them further would mean either
 * fetching `/accounts` twice or holding a cache between two ViewModels that has
 * to be cleared on sign-out. They separate cleanly the day the backend models a
 * store→provider relation of its own.
 *
 * Their filters, however, are entirely independent — kept apart below, and each
 * tab reads only its own.
 */
@Immutable
data class RegistryUIState(
    val activeProviders: List<IspProviderRow> = emptyList(),
    val branches: List<BranchRow> = emptyList(),
    val accounts: List<IspAccountRow> = emptyList(),

    // Loading the three lists. One flag and one message for all of them: they
    // are fetched together and assembled together, so a panel with only some of
    // them loaded would draw rows that are quietly wrong.
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadError: String? = null,

    // Branch filters.
    val branchQuery: String = "",
    val branchLetter: Char = LETTER_ALL,
    val branchProviderId: String? = null,

    // Account filters.
    val accountQuery: String = "",
    val accountProviderId: String? = null,

    // Register new branch.
    val registerBranchForm: RegisterBranchForm? = null,

    // Bulk import. The chosen file's bytes are deliberately absent: a ByteArray
    // compares by identity, so holding one here would make every state copy
    // unequal and recompose the whole panel. The ViewModel keeps them instead.
    val isBulkImportOpen: Boolean = false,
    val bulkImportFileName: String? = null,
    val bulkImportFileSize: Long = 0,
    val isBulkImporting: Boolean = false,
    val bulkImportError: String? = null,
    val bulkImportSummary: BulkImportSummary? = null,
) {
    /** A file is chosen, nothing is in flight, and the result is not already in. */
    val canStartImport: Boolean
        get() = bulkImportFileName != null && !isBulkImporting && bulkImportSummary == null

    /**
     * The letters the rail offers — only those that actually file a branch, so
     * the rail never advertises an empty result.
     */
    val branchLetters: List<Char>
        get() = branches.map { it.indexLetter }.distinct().sorted()

    /**
     * Branch filters compose rather than override: picking `A` and then typing
     * narrows within `A` instead of silently dropping the letter.
     */
    val visibleBranches: List<BranchRow>
        get() = branches
            .filter { branchLetter == LETTER_ALL || it.indexLetter == branchLetter }
            .filter { branchProviderId == null || branchProviderId in it.providerIds }
            .filter { branch ->
                branchQuery.isBlank() || branchQuery.trim().let { q ->
                    branch.name.contains(q, ignoreCase = true) ||
                        branch.branchCode.contains(q, ignoreCase = true) ||
                        branch.city?.contains(q, ignoreCase = true) == true
                }
            }
            .sortedBy { it.name }

    /** Account search matches the number or the store — both are how staff look one up. */
    val visibleAccounts: List<IspAccountRow>
        get() = accounts
            .filter { accountProviderId == null || it.providerId == accountProviderId }
            .filter { account ->
                accountQuery.isBlank() || accountQuery.trim().let { q ->
                    account.accountNumber.contains(q, ignoreCase = true) ||
                        account.storeName.contains(q, ignoreCase = true)
                }
            }
}
