package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserStatus
import com.puregoldgo.ibms.shared.validation.Validation

/** Which of the three panels the control panel is showing. */
enum class DashboardTab { Directory, Stores, Accounts }

/** The `All` entry of the A–Z rail — no letter selected. */
const val LETTER_ALL: Char = ' '

@Immutable
data class DashboardUIState(
    // Who is signed in — drawn in the app bar, and the answer to "is this row
    // me?" that the directory's own guards need.
    val currentUserId: String? = null,
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

    // Directory filter.
    val userQuery: String = "",

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
     * Only the search narrows it. Accounts sitting at [Role.PENDING] used to be
     * held back for a separate approval queue, but there is no self-registration
     * — a pending account is one a sysadmin created and has not yet given a role
     * to, and hiding it here would hide the only place that role can be set.
     *
     * The search matches the employee number as well as the name and username:
     * it is the identifier the rest of the company files people under, and it is
     * what an admin has in hand when a request arrives from HR.
     */
    val directoryUsers: List<DirectoryUser>
        get() = users
            .filter { user ->
                userQuery.isBlank() || userQuery.trim().let { q ->
                    user.name.contains(q, ignoreCase = true) ||
                        user.username.contains(q, ignoreCase = true) ||
                        user.employeeNumber?.contains(q, ignoreCase = true) == true
                }
            }
            .sortedBy { it.name }

    /** Whether [user] is the account currently signed in. */
    fun isSelf(user: DirectoryUser): Boolean =
        currentUserId != null && user.id == currentUserId

    /**
     * Why [user] cannot be deactivated, or null when they can be.
     *
     * Two ways to lock the company out of its own user administration: switch
     * off your own account, or switch off the only sysadmin left. The backend
     * guards neither — it refuses to *demote* the last sysadmin but will happily
     * deactivate them — so this is the only thing standing in front of it. It
     * remains a guard rail, not a guarantee: two admins acting at once can still
     * race, and the server's rejection is what settles that.
     */
    fun deactivateBlockedReason(user: DirectoryUser): DeactivateBlock? = when {
        isSelf(user) -> DeactivateBlock.Self
        user.role == Role.SYSADMIN && activeSysadminCount <= 1 -> DeactivateBlock.LastSysadmin
        else -> null
    }

    private val activeSysadminCount: Int
        get() = users.count { it.role == Role.SYSADMIN && it.status == UserStatus.ACTIVE }

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

/** Why a row's *Deactivate* is refused before it is ever sent. */
enum class DeactivateBlock { Self, LastSysadmin }

/**
 * Creating a user, resetting a password, and the per-row role and status edits.
 *
 * [issued] is the sensitive one — see [IssuedCredential] for what may be done
 * with it. It is cleared on dismiss and on sign-out.
 *
 * The four `*Target` fields are mutually exclusive by construction: opening any
 * of them replaces this whole object, so only one dialog can be showing and a
 * credential on screen always belongs to whichever one it is.
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

    /** The user whose role is being changed, and the role picked so far. */
    val roleTarget: DirectoryUser? = null,
    val pendingRole: Role? = null,

    /** The user whose activation is being toggled, awaiting confirmation. */
    val statusTarget: DirectoryUser? = null,
) {
    /**
     * Every field is valid, nothing is in flight, and the credential is not
     * already issued — a second submit would provision a second account.
     *
     * The full validation rather than a blank check: the same rules run again in
     * `ProvisionUserUseCase`, and a button that stays live over a field already
     * showing its own error is inviting a round trip that cannot succeed.
     */
    val canSubmit: Boolean
        get() = !isSubmitting && issued == null && form.validationError == null

    /** True while a reset is confirmed and running, before its credential arrives. */
    val isResetting: Boolean
        get() = resetTarget != null && isSubmitting
}

/**
 * The add-user form.
 *
 * The name is captured in three parts because the backend keeps a column for
 * each — sending only a composed display name, as this form once did, left
 * `first_name`, `middle_initial` and `last_name` null on every account made in
 * the app. [Validation.composeFullName] joins them for the `name` it displays.
 *
 * [middleInitial] is the only optional field. There is no endpoint to correct a
 * name or an employee number afterwards, so everything else has to be right on
 * the first attempt.
 */
@Immutable
data class NewUserForm(
    val firstName: String = "",
    val middleInitial: String = "",
    val lastName: String = "",
    val username: String = "",
    val employeeNumber: String = "",
    val role: Role = Role.PENDING,
) {
    /** The first rule this form breaks, or null when it breaks none. */
    val validationError: String?
        get() = Validation.validateRequired(firstName, "First name")
            ?: Validation.validateRequired(lastName, "Last name")
            ?: Validation.validateUsername(username)
            ?: Validation.validateEmployeeNumber(employeeNumber)

    /**
     * Per-field errors for the inputs that have a rule of their own, suppressed
     * while the field is still untouched — an empty field has not failed
     * anything yet, and a form that greets you in red is telling you off for
     * having just opened it.
     */
    val usernameError: String?
        get() = username.takeIf { it.isNotBlank() }?.let { Validation.validateUsername(it) }

    val employeeNumberError: String?
        get() = employeeNumber.takeIf { it.isNotBlank() }
            ?.let { Validation.validateEmployeeNumber(it) }
}
