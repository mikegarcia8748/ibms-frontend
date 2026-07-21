package com.puregoldgo.ibms.ui.screen.dashboard

import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardUIStateTest {

    private fun branch(
        id: String,
        name: String,
        code: String = "000",
        city: String? = null,
        providerIds: Set<String> = setOf("globe"),
    ) = BranchRow(
        id = id,
        branchCode = code,
        name = name,
        city = city,
        providerIds = providerIds,
        isActive = true,
    )

    private fun directoryUser(
        id: String,
        name: String = "Jane Doe",
        role: Role = Role.SECRETARY,
        status: UserStatus = UserStatus.ACTIVE,
        employeeNumber: String? = null,
        mustChangePassword: Boolean = false,
    ) = DirectoryUser(
        id = id,
        name = name,
        username = id,
        employeeNumber = employeeNumber,
        role = role,
        status = status,
        mustChangePassword = mustChangePassword,
    )

    /** A form that passes every rule, so each test varies exactly one thing. */
    private fun validForm() = NewUserForm(
        firstName = "Jane",
        lastName = "Doe",
        username = "jdoe",
        employeeNumber = "010005529",
    )

    private fun account(
        id: String,
        number: String,
        store: String,
        providerId: String = "globe",
    ) = IspAccountRow(
        id = id,
        accountNumber = number,
        storeName = store,
        providerId = providerId,
        monthlyRate = "1,500.00",
        isActive = true,
    )

    // region Branch filters

    @Test
    fun tc01_should_match_a_branch_on_any_of_its_providers() {
        // A branch with a primary and a backup line must appear under both.
        val state = DashboardUIState(
            branches = listOf(branch("b1", "SHAW", providerIds = setOf("globe", "pldt"))),
            branchProviderId = "pldt",
        )

        assertEquals(1, state.visibleBranches.size)
    }

    @Test
    fun tc02_should_exclude_a_branch_without_the_selected_provider() {
        val state = DashboardUIState(
            branches = listOf(branch("b1", "SHAW", providerIds = setOf("globe"))),
            branchProviderId = "radius",
        )

        assertTrue(state.visibleBranches.isEmpty())
    }

    @Test
    fun tc03_should_compose_letter_and_query_rather_than_override() {
        val state = DashboardUIState(
            branches = listOf(
                branch("b1", "ALPHA"),
                branch("b2", "AMAYA"),
                branch("b3", "BACOOR"),
            ),
            branchLetter = 'A',
            branchQuery = "may",
        )

        assertEquals(listOf("AMAYA"), state.visibleBranches.map { it.name })
    }

    @Test
    fun tc04_should_search_branches_by_code_and_city_too() {
        val state = DashboardUIState(
            branches = listOf(
                branch("b1", "SHAW", code = "155", city = "Mandaluyong City"),
                branch("b2", "IMUS", code = "8512", city = "Cavite"),
            ),
        )

        assertEquals(listOf("SHAW"), state.copy(branchQuery = "155").visibleBranches.map { it.name })
        assertEquals(listOf("IMUS"), state.copy(branchQuery = "cavite").visibleBranches.map { it.name })
    }

    @Test
    fun tc05_should_only_offer_letters_that_file_a_branch() {
        val state = DashboardUIState(
            branches = listOf(
                branch("b1", "SHAW"),
                branch("b2", "888 CHINA TOWN"),
                branch("b3", "ADMIRAL"),
            ),
        )

        assertEquals(listOf('#', 'A', 'S'), state.branchLetters)
    }

    // endregion

    // region Account filters

    @Test
    fun tc11_should_search_accounts_by_number_or_store() {
        val state = DashboardUIState(
            accounts = listOf(
                account("a1", "ZTEGC44534B3", "PARAÑAQUE"),
                account("a2", "IC-AWZ-2200", "BACOOR"),
            ),
        )

        assertEquals(listOf("a1"), state.copy(accountQuery = "zteg").visibleAccounts.map { it.id })
        assertEquals(listOf("a2"), state.copy(accountQuery = "bacoor").visibleAccounts.map { it.id })
    }

    @Test
    fun tc12_should_filter_accounts_by_provider() {
        val state = DashboardUIState(
            accounts = listOf(
                account("a1", "N1", "SHAW", providerId = "globe"),
                account("a2", "N2", "IMUS", providerId = "pldt"),
            ),
            accountProviderId = "pldt",
        )

        assertEquals(listOf("a2"), state.visibleAccounts.map { it.id })
    }

    // endregion

    // region Import gating

    @Test
    fun tc21_should_not_allow_a_second_import_once_a_summary_is_in() {
        val state = DashboardUIState(
            bulkImportFileName = "master.xlsx",
            bulkImportSummary = BulkImportSummary(
                providers = emptyList(),
                storesCreated = 0,
                storesReused = 0,
                accountsCreated = 0,
                accountsReused = 0,
                rowsSkipped = 0,
                skipReasons = emptyList(),
                totalRows = 0,
            ),
        )

        assertTrue(!state.canStartImport)
    }

    @Test
    fun tc22_should_allow_an_import_once_a_file_is_chosen() {
        val state = DashboardUIState(bulkImportFileName = "master.xlsx")

        assertTrue(state.canStartImport)
    }

    // endregion

    // region Directory

    @Test
    fun tc31_should_order_the_directory_by_name() {
        val state = DashboardUIState(
            users = listOf(
                directoryUser(id = "u1", name = "Samuel P Baricaua Jr"),
                directoryUser(id = "u2", name = "Angel V Rubio"),
                directoryUser(id = "u3", name = "Michael Garcia"),
            ),
        )

        assertEquals(
            listOf("Angel V Rubio", "Michael Garcia", "Samuel P Baricaua Jr"),
            state.directoryUsers.map { it.name },
        )
    }

    @Test
    fun tc32_should_keep_a_user_who_has_no_role_yet() {
        // There is no approval queue to hold them back into: PENDING is an
        // account a sysadmin made and has not given a role to, and this list is
        // the only place that role can be set.
        val state = DashboardUIState(
            users = listOf(
                directoryUser(id = "u1", name = "Rosario D Lim", role = Role.PENDING),
                directoryUser(id = "u2", name = "Michael Garcia", role = Role.SYSADMIN),
            ),
        )

        assertEquals(2, state.directoryUsers.size)
    }

    @Test
    fun tc33_should_keep_a_deactivated_user_listed() {
        // A row that is gone cannot be turned back on.
        val state = DashboardUIState(
            users = listOf(directoryUser(id = "u1", status = UserStatus.INACTIVE)),
        )

        assertEquals(1, state.directoryUsers.size)
        assertTrue(!state.directoryUsers.first().isActive)
    }

    @Test
    fun tc34_should_search_the_directory_by_name_username_or_employee_number() {
        val state = DashboardUIState(
            users = listOf(
                directoryUser(id = "arubio", name = "Angel V Rubio", employeeNumber = "010005869"),
                directoryUser(id = "mgarcia", name = "Michael Garcia", employeeNumber = "010005529"),
            ),
        )

        assertEquals(
            listOf("Michael Garcia"),
            state.copy(userQuery = "garc").directoryUsers.map { it.name },
        )
        assertEquals(
            listOf("Angel V Rubio"),
            state.copy(userQuery = "arub").directoryUsers.map { it.name },
        )
        // The employee number is what an admin has in hand when HR asks.
        assertEquals(
            listOf("Michael Garcia"),
            state.copy(userQuery = "010005529").directoryUsers.map { it.name },
        )
    }

    @Test
    fun tc35_should_keep_the_name_order_within_a_search() {
        val state = DashboardUIState(
            users = listOf(
                directoryUser(id = "u1", name = "Samuel P Baricaua Jr"),
                directoryUser(id = "u2", name = "Angel V Rubio"),
                directoryUser(id = "u3", name = "Michael Garcia"),
            ),
            userQuery = "a",
        )

        assertEquals(
            listOf("Angel V Rubio", "Michael Garcia", "Samuel P Baricaua Jr"),
            state.directoryUsers.map { it.name },
        )
    }

    // endregion

    // region Deactivation guards

    @Test
    fun tc36_should_refuse_to_deactivate_your_own_account() {
        // Signing yourself out of user administration permanently.
        val self = directoryUser(id = "u1", role = Role.SYSADMIN)
        val state = DashboardUIState(
            currentUserId = "u1",
            users = listOf(self, directoryUser(id = "u2", role = Role.SYSADMIN)),
        )

        assertEquals(DeactivateBlock.Self, state.deactivateBlockedReason(self))
    }

    @Test
    fun tc37_should_refuse_to_deactivate_the_last_sysadmin() {
        // The backend refuses to *demote* the last one but will deactivate them.
        val lastAdmin = directoryUser(id = "u2", role = Role.SYSADMIN)
        val state = DashboardUIState(
            currentUserId = "u1",
            users = listOf(directoryUser(id = "u1", role = Role.SECRETARY), lastAdmin),
        )

        assertEquals(DeactivateBlock.LastSysadmin, state.deactivateBlockedReason(lastAdmin))
    }

    @Test
    fun tc38_should_allow_deactivating_a_sysadmin_while_another_is_active() {
        val other = directoryUser(id = "u2", role = Role.SYSADMIN)
        val state = DashboardUIState(
            currentUserId = "u1",
            users = listOf(directoryUser(id = "u1", role = Role.SYSADMIN), other),
        )

        assertNull(state.deactivateBlockedReason(other))
    }

    @Test
    fun tc39_should_not_count_a_deactivated_sysadmin_as_cover() {
        // Two sysadmins on the list, only one of them able to sign in.
        val lastActive = directoryUser(id = "u2", role = Role.SYSADMIN)
        val state = DashboardUIState(
            currentUserId = "u1",
            users = listOf(
                directoryUser(id = "u1", role = Role.SYSADMIN, status = UserStatus.INACTIVE),
                lastActive,
            ),
        )

        assertEquals(DeactivateBlock.LastSysadmin, state.deactivateBlockedReason(lastActive))
    }

    // endregion

    // region Add-user gating

    @Test
    fun tc41_should_not_allow_a_submit_while_a_required_field_is_empty() {
        assertTrue(!UserAdminUIState(form = validForm().copy(firstName = "")).canSubmit)
        assertTrue(!UserAdminUIState(form = validForm().copy(lastName = "")).canSubmit)
        assertTrue(!UserAdminUIState(form = validForm().copy(username = "")).canSubmit)
        assertTrue(!UserAdminUIState(form = validForm().copy(employeeNumber = "")).canSubmit)
    }

    @Test
    fun tc42_should_not_allow_a_second_submit_once_a_credential_is_issued() {
        // A second one would provision a second account.
        val state = UserAdminUIState(
            form = validForm(),
            issued = IssuedCredential(
                username = "jdoe",
                name = "Jane Doe",
                temporaryPassword = "TEST-temporary-password",
                expiresAt = "2026-07-24T09:00:00Z",
                isNewUser = true,
            ),
        )

        assertTrue(!state.canSubmit)
    }

    @Test
    fun tc43_should_not_allow_a_submit_while_one_is_in_flight() {
        val state = UserAdminUIState(form = validForm(), isSubmitting = true)

        assertTrue(!state.canSubmit)
    }

    @Test
    fun tc44_should_allow_a_submit_once_every_field_is_valid() {
        assertTrue(UserAdminUIState(form = validForm()).canSubmit)
    }

    @Test
    fun tc45_should_not_allow_a_submit_over_a_field_already_showing_an_error() {
        // The button used to stay live here, and the round trip could only fail.
        val malformedUsername = UserAdminUIState(form = validForm().copy(username = "jane doe"))
        val malformedNumber = UserAdminUIState(form = validForm().copy(employeeNumber = "EMP-1"))

        assertTrue(!malformedUsername.canSubmit)
        assertNotNull(malformedUsername.form.usernameError)
        assertTrue(!malformedNumber.canSubmit)
        assertNotNull(malformedNumber.form.employeeNumberError)
    }

    @Test
    fun tc46_should_not_report_an_untouched_field_as_an_error() {
        // A form that greets you in red is telling you off for opening it.
        val fresh = NewUserForm()

        assertNull(fresh.usernameError)
        assertNull(fresh.employeeNumberError)
    }

    // endregion
}
