package com.puregoldgo.ibms.ui.screen.dashboard

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
