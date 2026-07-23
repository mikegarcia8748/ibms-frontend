package com.puregoldgo.ibms.ui.screen.secretary.compile

import com.puregoldgo.ibms.ui.screen.secretary.AccountRecordStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompileUIStateTest {

    private fun row(id: String, store: String, amount: String = "1000.00") = CompileAccountRow(
        accountId = id,
        accountNumber = id,
        storeName = store,
        providerName = "Globe",
        billingDay = 5,
        amount = amount,
        status = AccountRecordStatus.Active,
    )

    private fun line(
        id: String,
        savedRfp: String?,
        branch: String? = null,
        store: String = "Store $id",
        account: String = id,
        prorated: String = "1000.00",
        full: String = "1000.00",
    ) = CompileLineRow(
        id = id,
        accountId = id,
        storeName = store,
        branchCode = branch,
        circuitId = null,
        accountNumber = account,
        proratedAmount = prorated,
        fullAmount = full,
        rfpSortOrder = 1,
        savedRfpNumber = savedRfp,
        rfpInput = savedRfp.orEmpty(),
    )

    @Test
    fun tc01_letter_and_query_compose_and_the_list_sorts_by_store() {
        val state = CompileUIState(
            reviewRows = listOf(
                row("a1", "BALIBAGO"),
                row("a2", "ALAPAN"),
                row("a3", "ANGAT"),
            ),
            letter = 'A',
        )

        // Only the A-stores, alphabetised.
        assertEquals(listOf("ALAPAN", "ANGAT"), state.visibleRows.map { it.storeName })

        val searched = state.copy(letter = ' ', query = "bali")
        assertEquals(listOf("BALIBAGO"), searched.visibleRows.map { it.storeName })
    }

    @Test
    fun tc02_total_count_is_the_whole_context_not_the_filtered_view() {
        val state = CompileUIState(
            reviewRows = listOf(row("a1", "ALAPAN"), row("a2", "BALIBAGO")),
            query = "alap",
        )
        assertEquals(2, state.totalCount)
        assertEquals(1, state.visibleRows.size)
    }

    @Test
    fun tc03_compile_is_disabled_until_a_provider_is_chosen_with_accounts() {
        val noProvider = CompileUIState(selectedProviderId = null, reviewRows = listOf(row("a1", "ALAPAN")))
        assertFalse(noProvider.canCompile)

        val noRows = CompileUIState(selectedProviderId = "p1", reviewRows = emptyList())
        assertFalse(noRows.canCompile)

        val ready = CompileUIState(selectedProviderId = "p1", reviewRows = listOf(row("a1", "ALAPAN")))
        assertTrue(ready.canCompile)

        assertFalse(ready.copy(isCompiling = true).canCompile)
        assertFalse(ready.copy(loadError = "boom").canCompile)
    }

    @Test
    fun tc04_confirm_is_blocked_while_any_line_lacks_a_saved_rfp() {
        val partial = CompileUIState(lines = listOf(line("l1", "010001"), line("l2", null)))
        assertFalse(partial.canConfirm)

        val complete = CompileUIState(lines = listOf(line("l1", "010001"), line("l2", "010002")))
        assertTrue(complete.canConfirm)

        assertFalse(complete.copy(lines = emptyList()).canConfirm)
        assertFalse(complete.copy(isConfirming = true).canConfirm)
    }

    @Test
    fun tc06_assign_rfp_is_gated_on_numeric_range_with_end_ge_start() {
        val lines = listOf(line("l1", null, branch = "118"), line("l2", null, branch = "119"))
        val base = CompileUIState(lines = lines)
        assertEquals(2, base.distinctStoreCodeCount)

        // No range, or only one end filled → disabled.
        assertFalse(base.canAssignRfp)
        assertFalse(base.copy(rfpRangeStart = "0100021").canAssignRfp)

        // Valid numeric range with end >= start → enabled.
        val ready = base.copy(rfpRangeStart = "0100021", rfpRangeEnd = "0100022")
        assertTrue(ready.canAssignRfp)

        // end < start → disabled.
        assertFalse(base.copy(rfpRangeStart = "0100022", rfpRangeEnd = "0100021").canAssignRfp)

        // No lines, or a request in flight → disabled.
        assertFalse(ready.copy(lines = emptyList()).canAssignRfp)
        assertFalse(ready.copy(isAssigningRfp = true).canAssignRfp)
    }

    @Test
    fun tc05_next_month_greys_out_at_the_current_month() {
        val current = com.puregoldgo.ibms.shared.domain.BillingPeriod.current()
        val atCurrent = CompileUIState(billingPeriod = current)
        assertFalse(atCurrent.canGoNextMonth)

        val previous = com.puregoldgo.ibms.shared.domain.BillingPeriod.previous(current)
        assertTrue(CompileUIState(billingPeriod = previous).canGoNextMonth)
    }

    @Test
    fun tc07_drafted_search_matches_store_name_code_and_account_number() {
        val lines = listOf(
            line("l1", null, branch = "118", store = "ALAPAN", account = "ACC-111"),
            line("l2", null, branch = "229", store = "BALIBAGO", account = "ACC-222"),
        )
        val base = CompileUIState(lines = lines)

        assertEquals(listOf("l2"), base.copy(linesQuery = "bali").visibleLines.map { it.id })  // store name
        assertEquals(listOf("l1"), base.copy(linesQuery = "118").visibleLines.map { it.id })   // store code
        assertEquals(listOf("l2"), base.copy(linesQuery = "acc-222").visibleLines.map { it.id }) // account number
        assertEquals(2, base.copy(linesQuery = "  ").visibleLines.size)                         // blank = no filter
    }

    @Test
    fun tc08_drafted_letter_filter_narrows_to_one_initial() {
        val lines = listOf(
            line("l1", null, branch = "1", store = "ALAPAN"),
            line("l2", null, branch = "2", store = "ANGAT"),
            line("l3", null, branch = "3", store = "BALIBAGO"),
        )
        val state = CompileUIState(lines = lines, linesLetter = 'A')

        assertEquals(listOf("ALAPAN", "ANGAT"), state.visibleLines.map { it.storeName })
        assertEquals(listOf('A', 'B'), state.lineLetters)
    }

    @Test
    fun tc09_drafted_sort_orders_by_store_code_name_and_mrc_numerically() {
        val lines = listOf(
            // storeCode desc-ish, names out of order, MRC where lexical != numeric.
            line("l1", null, branch = "229", store = "ZAMORA", full = "900.00"),
            line("l2", null, branch = "118", store = "ALAPAN", full = "1000.00"),
        )
        val base = CompileUIState(lines = lines)

        // Store code (the default): ascending → 118 before 229.
        assertEquals(listOf("l2", "l1"), base.visibleLines.map { it.id })

        // Alphabetical by store name: ALAPAN before ZAMORA.
        assertEquals(
            listOf("l2", "l1"),
            base.copy(linesSort = LineSortOrder.Alphabetical).visibleLines.map { it.id },
        )

        // MRC numeric: 900.00 (l1) before 1000.00 (l2) — lexical would reverse this.
        assertEquals(
            listOf("l1", "l2"),
            base.copy(linesSort = LineSortOrder.MonthlyRecurringCharge).visibleLines.map { it.id },
        )
    }

    @Test
    fun tc10_a_line_is_prorated_only_when_billed_differs_from_the_full_rate() {
        assertTrue(line("l1", null, prorated = "500.00", full = "1000.00").isProrated)
        assertFalse(line("l2", null, prorated = "1000.00", full = "1000.00").isProrated)
        // Same value, different string form — numeric compare must not call this prorated.
        assertFalse(line("l3", null, prorated = "1000.0", full = "1000.00").isProrated)
    }
}
