package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetUsersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ProvisionUserUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ResetUserPasswordUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserRoleUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserStatusUseCase
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserStatus
import com.puregoldgo.ibms.ui.screen.sysadmin.activeRows
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the staff directory and the user administration on it.
 *
 * Split out of the console's single ViewModel, which carried this alongside two
 * registries and a bulk import. The directory is joined to nothing — users are
 * fetched and drawn as they arrive — so it stands entirely on its own, and a
 * test for "the last sysadmin cannot be deactivated" no longer has to satisfy
 * four repositories to get there.
 *
 * Filtering lives in [DirectoryUIState] rather than in the composables, so the
 * rules are testable and the previews can show a filtered list without running a
 * ViewModel.
 */
class DirectoryViewModel(
    private val getUsers: GetUsersUseCase,
    private val getProviders: GetProvidersUseCase,
    private val provisionUser: ProvisionUserUseCase,
    private val resetUserPassword: ResetUserPasswordUseCase,
    private val updateUserRole: UpdateUserRoleUseCase,
    private val updateUserStatus: UpdateUserStatusUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        DirectoryUIState(currentUserId = CurrentUserStore.id),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    init {
        loadPanel()
    }

    // region Public API

    /**
     * Fetches the staff list and the ISP list — the two the panel draws.
     *
     * Concurrently, then awaited together: the tab shows both cards at once, and
     * one arriving alone would draw half a panel. A [refresh] keeps the current
     * rows on screen while it runs; a first load has none to keep.
     */
    fun loadPanel(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = !refresh, isRefreshing = refresh, loadError = null)
            }

            val users = async { getUsers().last() }
            val providers = async { getProviders().last() }

            val userResult = users.await()
            val providerResult = providers.await()

            val userList = userResult.data
            val providerList = providerResult.data

            if (userList == null || providerList == null) {
                // Report the first thing that actually went wrong rather than a
                // summary — "invalid cursor" names the bug, "could not load"
                // does not.
                failLoad(firstErrorMessage(userResult, providerResult))
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null,
                    users = userList.map { user -> user.toRow() },
                    activeProviders = providerList.activeRows(),
                )
            }
        }
    }

    fun onUserQueryChange(query: String) {
        _uiState.update { it.copy(userQuery = query) }
    }

    // endregion

    // region User administration

    /** Opens the add-user dialog on a clean slate — no stale form, error or credential. */
    fun onAddUserClick() {
        updateUserAdmin { UserAdminUIState(isAddOpen = true) }
    }

    fun onNewUserFirstNameChange(firstName: String) = updateForm { it.copy(firstName = firstName) }

    /**
     * Holds one letter.
     *
     * Capped here rather than validated after the fact: a middle *initial* field
     * that accepts a whole middle name would collect one, and the column behind
     * it is a single character.
     */
    fun onNewUserMiddleInitialChange(middleInitial: String) =
        updateForm { it.copy(middleInitial = middleInitial.take(MIDDLE_INITIAL_MAX)) }

    fun onNewUserLastNameChange(lastName: String) = updateForm { it.copy(lastName = lastName) }

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
                firstName = form.firstName,
                lastName = form.lastName,
                employeeNumber = form.employeeNumber,
                middleInitial = form.middleInitial,
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

    /**
     * Opens the role dialog on the user's current role.
     *
     * A role used to be changeable from a dropdown on the row itself, which put
     * "grant this person sysadmin" one stray click from "scroll the list". It is
     * a deliberate act now, and the dialog is where it is confirmed.
     */
    fun onChangeRoleClick(user: DirectoryUser) {
        updateUserAdmin { UserAdminUIState(roleTarget = user, pendingRole = user.role) }
    }

    fun onRoleSelectionChange(role: Role) {
        updateUserAdmin { it.copy(pendingRole = role, formError = null) }
    }

    fun onChangeRoleConfirm() {
        val admin = _uiState.value.userAdmin
        val target = admin.roleTarget ?: return
        val role = admin.pendingRole ?: return
        // Confirming the role they already hold is a no-op PATCH; close instead.
        if (role == target.role) {
            updateUserAdmin { UserAdminUIState() }
            return
        }
        if (admin.isSubmitting) return

        viewModelScope.launch {
            updateUserRole(target.id, role).collect(::collectUserAdminEdit)
        }
    }

    fun onChangeRoleDismiss() {
        updateUserAdmin { UserAdminUIState() }
    }

    /**
     * Asks for confirmation before switching an account on or off.
     *
     * Deactivating revokes nothing retroactively but blocks every future
     * sign-in, and it used to fire straight off a menu item. [DirectoryUIState]
     * refuses the cases that would lock the company out of its own user
     * administration before this is ever reached.
     */
    fun onUserStatusToggleClick(user: DirectoryUser) {
        updateUserAdmin { UserAdminUIState(statusTarget = user) }
    }

    fun onUserStatusConfirm() {
        val admin = _uiState.value.userAdmin
        val target = admin.statusTarget ?: return
        if (admin.isSubmitting) return

        val next = if (target.isActive) UserStatus.INACTIVE else UserStatus.ACTIVE
        viewModelScope.launch {
            updateUserStatus(target.id, next).collect(::collectUserAdminEdit)
        }
    }

    fun onUserStatusDismiss() {
        updateUserAdmin { UserAdminUIState() }
    }

    // endregion

    /**
     * Drops any temporary password still held.
     *
     * This is what makes sign-out safe now that the shell's ViewModel can no
     * longer reach in and clear it: signing out replaces the whole back stack,
     * which tears this ViewModel down, which runs this. It also covers the
     * session expiring underneath the screen, which the old arrangement did not.
     */
    override fun onCleared() {
        _uiState.update { it.copy(userAdmin = UserAdminUIState()) }
        super.onCleared()
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
     * [IssuedCredential] and held only there. On success the list behind the
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

    /**
     * Folds a role or status change into state.
     *
     * The dialog that asked for it stays up while the request is in flight and
     * closes only on success, so a rejection — "cannot demote the last sysadmin"
     * — is read in front of the user it concerns rather than under a list that
     * has already scrolled.
     */
    private suspend fun collectUserAdminEdit(resource: Resource<*>) {
        when (resource) {
            is Resource.Loading -> updateUserAdmin {
                it.copy(isSubmitting = true, formError = null)
            }

            is Resource.Success -> {
                updateUserAdmin { UserAdminUIState() }
                // Reload rather than patching the one row: a role change can
                // move a user in the sorted list, and the server is the only
                // party that knows whether it took.
                loadPanel(refresh = true)
            }

            is Resource.Failed -> failUserAdmin(resource.message)

            is Resource.Error -> failUserAdmin(resource.error?.message)
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

    private fun failLoad(message: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                loadError = message ?: "The directory could not be loaded.",
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

/** How much of a middle initial the form will hold — one letter and its dot. */
private const val MIDDLE_INITIAL_MAX = 2
