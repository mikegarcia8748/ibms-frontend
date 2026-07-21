package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.model.Role

/** Which of the three panels the control panel is showing. */
enum class DashboardTab { Directory, Stores, Accounts }

/** The `All` entry of the A–Z rail — no letter selected. */
const val LETTER_ALL: Char = ' '

@Immutable
data class DashboardUIState(
    // Who is signed in — drawn in the app bar.
    val userName: String = "",
    val userRole: String = "",

    val selectedTab: DashboardTab = DashboardTab.Directory,

    // Data. All four lists come from the API.
    val users: List<DirectoryUser> = emptyList(),
    val activeProviders: List<IspProviderRow> = emptyList(),
    val branches: List<BranchRow> = emptyList(),
    val accounts: List<IspAccountRow> = emptyList(),

    // Loading the four lists. One flag and one message for all of them: they
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

    // User administration. Nested rather than flattened like the import fields
    // above: this one drives three dialogs, and a dozen more `isX` / `xError`
    // names at this level would be impossible to tell apart at a glance.
    val userAdmin: UserAdminUIState = UserAdminUIState(),
) {
    /** A file is chosen, nothing is in flight, and the result is not already in. */
    val canStartImport: Boolean
        get() = bulkImportFileName != null && !isBulkImporting && bulkImportSummary == null

    /**
     * The staff directory, ordered by name so the list is scannable.
     *
     * Nothing is filtered out. Accounts sitting at [Role.PENDING] used to be
     * held back for a separate approval queue, but there is no self-registration
     * — a pending account is one a sysadmin created and has not yet given a role
     * to, and hiding it here would hide the only place that role can be set.
     */
    val directoryUsers: List<DirectoryUser>
        get() = users.sortedBy { it.name }

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

/**
 * Creating a user, resetting a password, and the per-row role and status edits.
 *
 * [issued] is the sensitive one — see [IssuedCredential] for what may be done
 * with it. It is cleared on dismiss and on sign-out.
 */
@Immutable
data class UserAdminUIState(
    val isAddOpen: Boolean = false,
    val form: NewUserForm = NewUserForm(),
    /** The server's own wording, shown verbatim — it names the field to fix. */
    val formError: String? = null,
    val isSubmitting: Boolean = false,
    val issued: IssuedCredential? = null,

    /** The user a reset has been asked for, awaiting confirmation. */
    val resetTarget: DirectoryUser? = null,

    /** The row with a role or status change in flight; its controls are disabled. */
    val busyUserId: String? = null,
    val rowError: String? = null,
) {
    /**
     * Both required fields are filled, nothing is in flight, and the credential
     * is not already issued — a second submit would provision a second account.
     */
    val canSubmit: Boolean
        get() = form.username.isNotBlank() &&
            form.name.isNotBlank() &&
            !isSubmitting &&
            issued == null

    /** True while a reset is confirmed and running, before its credential arrives. */
    val isResetting: Boolean
        get() = resetTarget != null && isSubmitting
}

/**
 * The add-user form.
 *
 * Only the four fields the panel asks for. `ProvisionUserRequest` also carries
 * first/middle/last name, but the backend keeps those separate from the [name]
 * it displays, and this directory has never shown them — asking for both would
 * be asking twice for the same thing.
 */
@Immutable
data class NewUserForm(
    val name: String = "",
    val username: String = "",
    val employeeNumber: String = "",
    val role: Role = Role.PENDING,
)
