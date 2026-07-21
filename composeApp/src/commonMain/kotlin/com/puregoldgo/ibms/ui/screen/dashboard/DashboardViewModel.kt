package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserStatus
import com.puregoldgo.ibms.shared.domain.usecase.BulkImportAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetUsersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ProvisionUserUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ResetUserPasswordUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserRoleUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserStatusUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the sysadmin control panel.
 *
 * Filtering lives in [DashboardUIState] rather than in the composables, so the
 * rules are testable and the previews can show a filtered list without running a
 * ViewModel.
 *
 * All four lists come from the API. [DashboardSampleData] survives only for the
 * `@Preview`s.
 */
class DashboardViewModel(
    private val authRepository: AuthRepository,
    private val bulkImportAccounts: BulkImportAccountsUseCase,
    private val getProviders: GetProvidersUseCase,
    private val getStores: GetStoresUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getUsers: GetUsersUseCase,
    private val provisionUser: ProvisionUserUseCase,
    private val resetUserPassword: ResetUserPasswordUseCase,
    private val updateUserRole: UpdateUserRoleUseCase,
    private val updateUserStatus: UpdateUserStatusUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        DashboardUIState(
            userName = CurrentUserStore.name.orEmpty(),
            userRole = CurrentUserStore.role.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    /** The spreadsheet awaiting upload. Kept out of the state — see [DashboardUIState]. */
    private var pickedFile: PickedFile? = null

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<DashboardUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    init {
        loadPanel()
    }

    // region Public API

    /**
     * Fetches users, providers, branches and accounts and assembles the panel.
     *
     * All four concurrently, then awaited together: the rows are cross-joined
     * (accounts give each branch its ISPs, stores give each account its name) so
     * there is nothing worth drawing until every list is in. A [refresh] keeps
     * the current rows on screen while it runs; a first load has none to keep.
     */
    fun loadPanel(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !refresh,
                    isRefreshing = refresh,
                    loadError = null,
                )
            }

            val providers = async { getProviders().last() }
            val stores = async { getStores().last() }
            val accounts = async { getAccounts().last() }
            val users = async { getUsers().last() }

            val providerResult = providers.await()
            val storeResult = stores.await()
            val accountResult = accounts.await()
            val userResult = users.await()

            val providerList = providerResult.data
            val storeList = storeResult.data
            val accountList = accountResult.data
            val userList = userResult.data

            if (providerList == null || storeList == null ||
                accountList == null || userList == null
            ) {
                // Report the first thing that actually went wrong rather than a
                // summary — "invalid cursor" names the bug, "could not load"
                // does not.
                failLoad(
                    firstErrorMessage(providerResult, storeResult, accountResult, userResult),
                )
                return@launch
            }

            val storeNames = storeList.associate { it.id to it.name }
            val providerIds = providerIdsByStore(accountList)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null,
                    users = userList.map { user -> user.toRow() },
                    activeProviders = providerList.activeRows(),
                    branches = storeList.map { store ->
                        store.toRow(providerIds[store.id].orEmpty())
                    },
                    accounts = accountList.map { account -> account.toRow(storeNames) },
                )
            }
        }
    }

    fun onTabSelect(tab: DashboardTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onBranchQueryChange(query: String) {
        _uiState.update { it.copy(branchQuery = query) }
    }

    fun onBranchLetterSelect(letter: Char) {
        _uiState.update { it.copy(branchLetter = letter) }
    }

    fun onBranchProviderSelect(providerId: String?) {
        _uiState.update { it.copy(branchProviderId = providerId) }
    }

    fun onAccountQueryChange(query: String) {
        _uiState.update { it.copy(accountQuery = query) }
    }

    fun onAccountProviderSelect(providerId: String?) {
        _uiState.update { it.copy(accountProviderId = providerId) }
    }

    /** Opens the import dialog on a clean slate — no stale file, error or result. */
    fun onBulkUploadClick() {
        pickedFile = null
        _uiState.update {
            it.copy(
                isBulkImportOpen = true,
                bulkImportFileName = null,
                bulkImportFileSize = 0,
                isBulkImporting = false,
                bulkImportError = null,
                bulkImportSummary = null,
            )
        }
    }

    /**
     * Accepts a chosen file. The bytes stay here rather than in [DashboardUIState]
     * — see the note on its bulk-import fields.
     */
    fun onBulkImportFilePicked(file: PickedFile) {
        pickedFile = file
        _uiState.update {
            it.copy(
                bulkImportFileName = file.name,
                bulkImportFileSize = file.size,
                bulkImportError = null,
                bulkImportSummary = null,
            )
        }
    }

    /** Uploads the chosen file to `POST /accounts/bulk-import`. */
    fun onBulkImportStart() {
        val file = pickedFile ?: return
        if (!_uiState.value.canStartImport) return

        viewModelScope.launch {
            bulkImportAccounts(
                fileName = file.name,
                mimeType = file.mimeType,
                bytes = file.bytes,
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isBulkImporting = true, bulkImportError = null)
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isBulkImporting = false,
                                bulkImportSummary = resource.data?.toUiSummary(),
                            )
                        }
                        // The rows behind the dialog are now stale — the import
                        // is the whole reason new ones exist. Refreshing rather
                        // than reloading keeps them on screen meanwhile.
                        loadPanel(refresh = true)
                    }

                    // The backend's own wording — "missing required column 'Store
                    // Code' in header row" and friends name the cell to go fix.
                    // Replacing it with generic copy would throw that away.
                    is Resource.Failed -> failImport(resource.message)

                    is Resource.Error -> failImport(resource.error?.message)
                }
            }
        }
    }

    /** Closes the dialog and forgets everything about the attempt. */
    fun onBulkUploadDismiss() {
        pickedFile = null
        _uiState.update {
            it.copy(
                isBulkImportOpen = false,
                bulkImportFileName = null,
                bulkImportFileSize = 0,
                isBulkImporting = false,
                bulkImportError = null,
                bulkImportSummary = null,
            )
        }
    }

    // endregion

    // region User administration

    /** Opens the add-user dialog on a clean slate — no stale form, error or credential. */
    fun onAddUserClick() {
        updateUserAdmin {
            UserAdminUIState(isAddOpen = true)
        }
    }

    fun onNewUserNameChange(name: String) = updateForm { it.copy(name = name) }

    /**
     * Lowercases as typed.
     *
     * The backend normalises before it checks uniqueness, so a field that let
     * "M.Garcia" stand would show one thing and submit another — and reject on a
     * collision the admin could not see coming.
     */
    fun onNewUserUsernameChange(username: String) =
        updateForm { it.copy(username = username.lowercase()) }

    fun onNewUserEmployeeNumberChange(employeeNumber: String) =
        updateForm { it.copy(employeeNumber = employeeNumber) }

    fun onNewUserRoleChange(role: Role) = updateForm { it.copy(role = role) }

    /** Creates the account. On success the dialog switches to the credential panel. */
    fun onAddUserSubmit() {
        val admin = _uiState.value.userAdmin
        if (!admin.canSubmit) return
        val form = admin.form

        viewModelScope.launch {
            provisionUser(
                username = form.username,
                name = form.name,
                employeeNumber = form.employeeNumber,
                role = form.role,
            ).collect { resource -> collectCredential(resource, isNewUser = true) }
        }
    }

    /** Closes the dialog and forgets the form *and* the temporary password. */
    fun onAddUserDismiss() {
        updateUserAdmin { UserAdminUIState() }
    }

    /** Asks for confirmation. The reset itself signs the user out everywhere. */
    fun onResetPasswordClick(user: DirectoryUser) {
        updateUserAdmin { UserAdminUIState(resetTarget = user) }
    }

    fun onResetPasswordConfirm() {
        val admin = _uiState.value.userAdmin
        val target = admin.resetTarget ?: return
        if (admin.isSubmitting || admin.issued != null) return

        viewModelScope.launch {
            resetUserPassword(target.id).collect { resource ->
                collectCredential(resource, isNewUser = false)
            }
        }
    }

    fun onResetPasswordDismiss() {
        updateUserAdmin { UserAdminUIState() }
    }

    fun onUserRoleChange(userId: String, role: Role) {
        viewModelScope.launch {
            updateUserRole(userId, role).collect { resource -> collectRowEdit(userId, resource) }
        }
    }

    fun onUserStatusToggle(user: DirectoryUser) {
        val next = if (user.isActive) UserStatus.INACTIVE else UserStatus.ACTIVE
        viewModelScope.launch {
            updateUserStatus(user.id, next).collect { resource ->
                collectRowEdit(user.id, resource)
            }
        }
    }

    fun onRowErrorDismiss() {
        updateUserAdmin { it.copy(rowError = null) }
    }

    // endregion

    /**
     * Drops the stored session.
     *
     * Clears the user-admin state first: a temporary password still on screen
     * must not outlive the admin who was shown it. There is no logout endpoint
     * to call.
     */
    fun onLogout() {
        updateUserAdmin { UserAdminUIState() }
        authRepository.signOut()
        viewModelScope.launch {
            _uiEvent.emit(DashboardUiEvent.NavigateToLogin)
        }
    }

    // region Internals

    private fun updateUserAdmin(transform: (UserAdminUIState) -> UserAdminUIState) {
        _uiState.update { it.copy(userAdmin = transform(it.userAdmin)) }
    }

    /** Edits the add-user form and clears the last rejection — it is now stale. */
    private fun updateForm(transform: (NewUserForm) -> NewUserForm) {
        updateUserAdmin { it.copy(form = transform(it.form), formError = null) }
    }

    /**
     * Folds a provision or reset into state.
     *
     * The temporary password is copied out of the response into
     * [IssuedCredential] and held only there. On success the panel behind the
     * dialog is refreshed rather than reloaded — the new row (or the returned
     * `TEMPORARY` chip) has to appear, but the credential must stay on screen
     * while it does.
     */
    private suspend fun collectCredential(
        resource: Resource<ProvisionedUser>,
        isNewUser: Boolean,
    ) {
        when (resource) {
            is Resource.Loading -> updateUserAdmin {
                it.copy(isSubmitting = true, formError = null)
            }

            is Resource.Success -> {
                val provisioned = resource.data
                if (provisioned == null) {
                    failUserAdmin("The server returned no credential.")
                    return
                }
                updateUserAdmin {
                    it.copy(
                        isSubmitting = false,
                        formError = null,
                        issued = IssuedCredential(
                            username = provisioned.user.username,
                            name = provisioned.user.name,
                            temporaryPassword = provisioned.temporaryPassword,
                            expiresAt = provisioned.temporaryPasswordExpiresAt,
                            isNewUser = isNewUser,
                        ),
                    )
                }
                loadPanel(refresh = true)
            }

            // The backend's own wording — "username 'jdoe' is already taken"
            // names the field to go change.
            is Resource.Failed -> failUserAdmin(resource.message)

            is Resource.Error -> failUserAdmin(resource.error?.message)
        }
    }

    /** Folds a role or status change into state, keeping the row marked busy meanwhile. */
    private suspend fun collectRowEdit(userId: String, resource: Resource<*>) {
        when (resource) {
            is Resource.Loading -> updateUserAdmin {
                it.copy(busyUserId = userId, rowError = null)
            }

            is Resource.Success -> {
                updateUserAdmin { it.copy(busyUserId = null, rowError = null) }
                // Reload rather than patching the one row: a role change can
                // move a user in the sorted list, and the server is the only
                // party that knows whether it took.
                loadPanel(refresh = true)
            }

            is Resource.Failed -> failRowEdit(resource.message)

            is Resource.Error -> failRowEdit(resource.error?.message)
        }
    }

    private fun failUserAdmin(message: String?) {
        updateUserAdmin {
            it.copy(
                isSubmitting = false,
                formError = message ?: "The request could not be completed.",
            )
        }
    }

    private fun failRowEdit(message: String?) {
        updateUserAdmin {
            it.copy(
                busyUserId = null,
                rowError = message ?: "The change could not be saved.",
            )
        }
    }

    private fun failImport(message: String?) {
        _uiState.update {
            it.copy(
                isBulkImporting = false,
                bulkImportError = message ?: "The import could not be completed.",
            )
        }
    }

    private fun failLoad(message: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                loadError = message ?: "The control panel could not be loaded.",
            )
        }
    }

    /** The message from the first of [results] that did not succeed. */
    private fun firstErrorMessage(vararg results: Resource<*>): String? =
        results.firstNotNullOfOrNull { result ->
            when (result) {
                is Resource.Failed -> result.message
                is Resource.Error -> result.error?.message
                else -> null
            }
        }

    // endregion
}

/** Narrows the API summary to what the dialog draws. */
private fun BulkImportSummaryResponse.toUiSummary() = BulkImportSummary(
    providers = providers.map {
        ProviderImportSummary(
            name = it.name,
            created = it.created,
            accountsCreated = it.accountsCreated,
            accountsReused = it.accountsReused,
        )
    },
    storesCreated = storesCreated,
    storesReused = storesReused,
    accountsCreated = accountsCreated,
    accountsReused = accountsReused,
    rowsSkipped = rowsSkipped,
    skipReasons = skipReasons,
    totalRows = totalRows,
)
