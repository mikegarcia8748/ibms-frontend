package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.model.Role

/** Which of the three panels the control panel is showing. */
enum class DashboardTab { Delegations, Stores, Accounts }

/** The `All` entry of the A–Z rail — no letter selected. */
const val LETTER_ALL: Char = ' '

@Immutable
data class DashboardUIState(
    // Who is signed in — drawn in the app bar.
    val userName: String = "",
    val userRole: String = "",

    val selectedTab: DashboardTab = DashboardTab.Delegations,

    // Data. Providers, branches and accounts come from the API; `users` is still
    // sample-backed — there is a `GET /users`, but the delegations panel has no
    // wiring pass yet.
    val users: List<DirectoryUser> = emptyList(),
    val activeProviders: List<IspProviderRow> = emptyList(),
    val inactiveProviders: List<IspProviderRow> = emptyList(),
    val branches: List<BranchRow> = emptyList(),
    val accounts: List<IspAccountRow> = emptyList(),

    // Loading the three lists. One flag and one message for all of them: they
    // are fetched together and assembled together — accounts supply the branch
    // rows' ISPs, stores supply the account rows' names — so a panel with only
    // some of them loaded would draw rows that are quietly wrong.
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

    /** Accounts still awaiting a role — the only ones a delegation applies to. */
    val pendingUsers: List<DirectoryUser>
        get() = users.filter { it.role == Role.PENDING }

    /** Everyone who already has one. Ordered by name so the list is scannable. */
    val directoryUsers: List<DirectoryUser>
        get() = users.filter { it.role != Role.PENDING }.sortedBy { it.name }

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
