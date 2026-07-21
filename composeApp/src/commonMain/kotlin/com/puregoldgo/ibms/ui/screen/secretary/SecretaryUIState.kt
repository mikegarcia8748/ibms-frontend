package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.ui.component.LETTER_ALL

/** Which of the six panels the secretary console is showing. */
enum class SecretaryTab {
    CompileTopSheet,
    BranchLocations,
    IspAccounts,
    BillingHistory,
    Archive,
    FloatingAccounts,
}

@Immutable
data class SecretaryUIState(
    // Who is signed in — drawn in the app bar.
    val userName: String = "",
    val userRole: String = "",

    val selectedTab: SecretaryTab = SecretaryTab.CompileTopSheet,

    // Data. Sample-fed this pass; every list comes from the API once wired.
    val providers: List<SecretaryProviderRow> = emptyList(),
    val branches: List<SecretaryBranchRow> = emptyList(),
    val accounts: List<SecretaryAccountRow> = emptyList(),
    val topSheets: List<TopSheetRow> = emptyList(),

    // Loading. One flag and one message for all four lists: they are assembled
    // together — accounts supply each branch's ISPs, branches decide which
    // accounts are floating — so a panel with only some of them loaded would
    // draw rows that are quietly wrong.
    val isLoading: Boolean = false,
    val loadError: String? = null,

    // Branch filters.
    val branchQuery: String = "",
    val branchLetter: Char = LETTER_ALL,
    val branchProviderId: String? = null,

    // Account filters.
    val accountQuery: String = "",
    val accountLetter: Char = LETTER_ALL,
    val accountProviderId: String? = null,
    val accountStatus: AccountRecordStatus? = null,

    // Billing history.
    val invoiceQuery: String = "",

    // The two creation dialogs. Mutually exclusive at the call site.
    val addBranch: NewBranchForm? = null,
    val addAccount: NewAccountForm? = null,
) {
    /** Only active ISPs are offered as a filter — a dead one matches nothing. */
    val activeProviders: List<SecretaryProviderRow>
        get() = providers.filter { it.isActive }

    // region Branch locations

    /**
     * The letters the rail offers — only those that actually file an open
     * branch, so the rail never advertises an empty result.
     */
    val branchLetters: List<Char>
        get() = openBranches.map { it.indexLetter }.distinct().sorted()

    /**
     * Branch filters compose rather than override: picking `A` and then typing
     * narrows within `A` instead of silently dropping the letter.
     */
    val visibleBranches: List<SecretaryBranchRow>
        get() = openBranches
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

    /** Closed branches live in the archive, not in the working list. */
    private val openBranches: List<SecretaryBranchRow>
        get() = branches.filter { !it.isClosed }

    // endregion

    // region ISP accounts

    val accountLetters: List<Char>
        get() = liveAccounts.map { it.indexLetter }.distinct().sorted()

    /**
     * Four filters at once — letter, ISP, status and free text. Search matches
     * the account number or the branch, both of which are how staff look one up.
     */
    val visibleAccounts: List<SecretaryAccountRow>
        get() = liveAccounts
            .filter { accountLetter == LETTER_ALL || it.indexLetter == accountLetter }
            .filter { accountProviderId == null || it.providerId == accountProviderId }
            .filter { accountStatus == null || it.status == accountStatus }
            .filter { account ->
                accountQuery.isBlank() || accountQuery.trim().let { q ->
                    account.accountNumber.contains(q, ignoreCase = true) ||
                        account.storeName.contains(q, ignoreCase = true)
                }
            }
            .sortedBy { it.storeName }

    /** Everything still billing. The rest is archive material. */
    private val liveAccounts: List<SecretaryAccountRow>
        get() = accounts.filter { it.isLive }

    // endregion

    /** Topsheets are few and already dated; only the invoice number is searched. */
    val visibleTopSheets: List<TopSheetRow>
        get() = topSheets.filter { sheet ->
            invoiceQuery.isBlank() ||
                sheet.invoiceNumber.contains(invoiceQuery.trim(), ignoreCase = true)
        }

    // region Archive

    val inactiveAccounts: List<SecretaryAccountRow>
        get() = accounts.filter { !it.isLive }.sortedBy { it.storeName }

    val closedBranches: List<SecretaryBranchRow>
        get() = branches.filter { it.isClosed }.sortedBy { it.name }

    val inactiveProviders: List<SecretaryProviderRow>
        get() = providers.filter { !it.isActive }.sortedBy { it.name }

    // endregion

    /**
     * Accounts still billing against a branch that has closed.
     *
     * Derived rather than stored: it is a join, and the moment either side moves
     * — a branch closes, an account is transferred — a stored list would be
     * wrong. Every one of these is money leaving for a line nobody is using.
     */
    val floatingAccounts: List<SecretaryAccountRow>
        get() {
            val closedIds = branches.filter { it.isClosed }.map { it.id }.toSet()
            return accounts.filter { it.isLive && it.storeId in closedIds }
        }

    /** A branch code and a name are the minimum `POST /stores` will accept. */
    val canSubmitBranch: Boolean
        get() = addBranch?.let {
            it.branchCode.isNotBlank() && it.name.isNotBlank()
        } == true

    /** Account number, branch and ISP identify the line; the rate is what it bills. */
    val canSubmitAccount: Boolean
        get() = addAccount?.let {
            it.accountNumber.isNotBlank() &&
                it.storeId != null &&
                it.providerId != null &&
                it.monthlyRate.isNotBlank()
        } == true
}
