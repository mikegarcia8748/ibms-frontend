package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetUsersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ProvisionUserUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ResetUserPasswordUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserRoleUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserStatusUseCase
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The directory panel's ViewModel, stood up on two fakes.
 *
 * This test is the point of the split. The same assertions against the console's
 * old single ViewModel would have needed a store, an account, a provider and a
 * bulk-import repository first — none of which the staff directory touches.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DirectoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var users: FakeUserRepository
    private lateinit var providers: FakeProviderRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        users = FakeUserRepository().apply {
            this.users = listOf(
                user(id = "u1", username = "arubio", name = "Angel V Rubio"),
                user(id = "u2", username = "mgarcia", name = "Michael Garcia", role = Role.SYSADMIN),
            )
        }
        providers = FakeProviderRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun user(
        id: String,
        username: String,
        name: String,
        role: Role = Role.SECRETARY,
        status: UserStatus = UserStatus.ACTIVE,
    ) = UserProfile(
        id = id,
        username = username,
        name = name,
        role = role,
        status = status,
        mustChangePassword = false,
    )

    private fun viewModel() = DirectoryViewModel(
        getUsers = GetUsersUseCase(users),
        getProviders = GetProvidersUseCase(providers),
        provisionUser = ProvisionUserUseCase(users),
        resetUserPassword = ResetUserPasswordUseCase(users),
        updateUserRole = UpdateUserRoleUseCase(users),
        updateUserStatus = UpdateUserStatusUseCase(users),
    )

    /**
     * The same ViewModel, held in a real [ViewModelStore].
     *
     * Only the tear-down test needs this: `ViewModel.clear()` is internal, so
     * the honest way to run `onCleared` is to clear the store that owns it —
     * which is exactly what replacing the back stack does at sign-out.
     */
    private fun ownedViewModel(): Pair<ViewModelStore, DirectoryViewModel> {
        val store = ViewModelStore()
        val provider = ViewModelProvider.create(
            store = store,
            factory = viewModelFactory { initializer { viewModel() } },
        )
        return store to provider[DirectoryViewModel::class]
    }

    // region Loading

    @Test
    fun tc01_should_load_the_directory_on_creation() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Angel V Rubio", "Michael Garcia"), vm.uiState.value.directoryUsers.map { it.name })
        assertNull(vm.uiState.value.loadError)
    }

    @Test
    fun tc02_should_report_the_servers_own_wording_when_the_load_fails() = runTest(dispatcher) {
        users.shouldFail = true
        users.failureMessage = "invalid cursor"

        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        // Generic copy would throw away the one string that names the bug.
        assertEquals("invalid cursor", vm.uiState.value.loadError)
        assertTrue(!vm.uiState.value.isLoading)
    }

    // endregion

    // region Role changes

    @Test
    fun tc11_should_close_without_a_request_when_the_role_is_unchanged() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val target = vm.uiState.value.directoryUsers.first { it.id == "u1" }
        vm.onChangeRoleClick(target)
        vm.onChangeRoleConfirm()
        testScheduler.advanceUntilIdle()

        // A no-op PATCH changes nothing and revokes nothing, but it costs a
        // round trip and flickers the row into its busy state.
        assertTrue(users.roleChanges.isEmpty())
        assertNull(vm.uiState.value.userAdmin.roleTarget)
    }

    @Test
    fun tc12_should_send_a_real_role_change_and_close() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val target = vm.uiState.value.directoryUsers.first { it.id == "u1" }
        vm.onChangeRoleClick(target)
        vm.onRoleSelectionChange(Role.FINANCE)
        vm.onChangeRoleConfirm()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("u1" to Role.FINANCE), users.roleChanges)
        assertNull(vm.uiState.value.userAdmin.roleTarget)
    }

    @Test
    fun tc13_should_keep_the_dialog_open_when_the_server_refuses() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val target = vm.uiState.value.directoryUsers.first { it.id == "u2" }
        vm.onChangeRoleClick(target)
        vm.onRoleSelectionChange(Role.SECRETARY)
        users.shouldFail = true
        users.failureMessage = "cannot demote the last sysadmin"
        vm.onChangeRoleConfirm()
        testScheduler.advanceUntilIdle()

        // Read in front of the user it concerns, rather than under a list that
        // has already scrolled.
        val admin = vm.uiState.value.userAdmin
        assertNotNull(admin.roleTarget)
        assertEquals("cannot demote the last sysadmin", admin.formError)
        assertTrue(!admin.isSubmitting)
    }

    // endregion

    // region Status changes

    @Test
    fun tc21_should_deactivate_an_active_account_and_reactivate_an_inactive_one() =
        runTest(dispatcher) {
            users.users = listOf(
                user(id = "u1", username = "arubio", name = "Angel V Rubio"),
                user(
                    id = "u3",
                    username = "garciaga",
                    name = "Gilbert Arciaga",
                    status = UserStatus.INACTIVE,
                ),
            )
            val vm = viewModel()
            testScheduler.advanceUntilIdle()

            vm.onUserStatusToggleClick(vm.uiState.value.directoryUsers.first { it.id == "u1" })
            vm.onUserStatusConfirm()
            testScheduler.advanceUntilIdle()

            vm.onUserStatusToggleClick(vm.uiState.value.directoryUsers.first { it.id == "u3" })
            vm.onUserStatusConfirm()
            testScheduler.advanceUntilIdle()

            assertEquals(
                listOf("u1" to UserStatus.INACTIVE, "u3" to UserStatus.ACTIVE),
                users.statusChanges,
            )
        }

    // endregion

    // region The temporary password

    @Test
    fun tc31_should_hold_the_temporary_password_only_until_the_dialog_closes() =
        runTest(dispatcher) {
            val vm = viewModel()
            testScheduler.advanceUntilIdle()

            vm.onResetPasswordClick(vm.uiState.value.directoryUsers.first { it.id == "u1" })
            vm.onResetPasswordConfirm()
            testScheduler.advanceUntilIdle()

            assertNotNull(vm.uiState.value.userAdmin.issued)

            vm.onResetPasswordDismiss()

            assertNull(vm.uiState.value.userAdmin.issued)
        }

    @Test
    fun tc32_should_drop_the_temporary_password_when_the_panel_is_torn_down() =
        runTest(dispatcher) {
            val (store, vm) = ownedViewModel()
            testScheduler.advanceUntilIdle()

            vm.onResetPasswordClick(vm.uiState.value.directoryUsers.first { it.id == "u1" })
            vm.onResetPasswordConfirm()
            testScheduler.advanceUntilIdle()
            assertNotNull(vm.uiState.value.userAdmin.issued)

            // What makes sign-out safe now that the shell's ViewModel can no
            // longer reach in and clear this: signing out replaces the back
            // stack, which clears the store that owns this panel. It also covers
            // the session expiring underneath the screen, which the old
            // arrangement missed.
            store.clear()

            assertNull(vm.uiState.value.userAdmin.issued)
        }

    @Test
    fun tc33_should_not_provision_twice_once_a_credential_is_issued() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onAddUserClick()
        vm.onNewUserFirstNameChange("Rosario")
        vm.onNewUserLastNameChange("Lim")
        vm.onNewUserUsernameChange("rlim")
        vm.onNewUserEmployeeNumberChange("010007422")
        vm.onAddUserSubmit()
        testScheduler.advanceUntilIdle()

        val first = users.lastProvisioned
        assertNotNull(first)

        // A second submit over an issued credential would create a second account.
        users.lastProvisioned = null
        vm.onAddUserSubmit()
        testScheduler.advanceUntilIdle()

        assertNull(users.lastProvisioned)
    }

    @Test
    fun tc34_should_lowercase_a_username_as_it_is_typed() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onAddUserClick()
        vm.onNewUserUsernameChange("M.Garcia")

        // The backend normalises before it checks uniqueness, so a field that
        // let this stand would show one thing and submit another.
        assertEquals("m.garcia", vm.uiState.value.userAdmin.form.username)
    }

    // endregion
}
