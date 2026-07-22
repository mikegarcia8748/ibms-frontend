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

    private fun line(id: String, savedRfp: String?) = CompileLineRow(
        id = id,
        accountId = id,
        storeName = "Store $id",
        branchCode = null,
        circuitId = null,
        accountNumber = id,
        proratedAmount = "1000.00",
        fullAmount = "1000.00",
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
    fun tc05_next_month_greys_out_at_the_current_month() {
        val current = com.puregoldgo.ibms.shared.domain.BillingPeriod.current()
        val atCurrent = CompileUIState(billingPeriod = current)
        assertFalse(atCurrent.canGoNextMonth)

        val previous = com.puregoldgo.ibms.shared.domain.BillingPeriod.previous(current)
        assertTrue(CompileUIState(billingPeriod = previous).canGoNextMonth)
    }
}
