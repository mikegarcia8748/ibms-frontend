package com.puregoldgo.ibms.ui.screen.secretary.compile

import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.CompilePreviewLine
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.StoreType
import com.puregoldgo.ibms.ui.screen.secretary.AccountRecordStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class CompileMappersTest {

    private val globe = Provider(id = "p-globe", name = "Globe", paymentScheduleDay = 5)

    private fun store(id: String, code: String, name: String, city: String? = null) = Store(
        id = id,
        storeType = StoreType.PUREGOLD,
        branchCode = code,
        name = name,
        city = city,
        status = StoreStatus.ACTIVE,
    )

    private fun account(
        id: String,
        number: String,
        storeId: String,
        rate: String,
        status: AccountStatus = AccountStatus.ACTIVE,
    ) = Account(
        id = id,
        accountNumber = number,
        providerId = "p-globe",
        storeId = storeId,
        rate = rate,
        status = status,
    )

    @Test
    fun tc01_browse_rows_join_store_name_and_provider_billing_day() {
        val rows = buildBrowseRows(
            accounts = listOf(account("a1", "877911149", "s1", "1998.00")),
            stores = listOf(store("s1", "8039", "ALAPAN 1A", "Imus Cavite")),
            providers = listOf(globe),
        )

        assertEquals(1, rows.size)
        val row = rows.single()
        assertEquals("ALAPAN 1A (Imus Cavite)", row.storeName)
        assertEquals("Globe", row.providerName)
        assertEquals(5, row.billingDay)
        assertEquals("1998.00", row.amount)
        assertEquals(AccountRecordStatus.Active, row.status)
    }

    @Test
    fun tc02_browse_drops_terminated_accounts_but_keeps_for_deactivation() {
        val rows = buildBrowseRows(
            accounts = listOf(
                account("a1", "111", "s1", "1000.00", AccountStatus.ACTIVE),
                account("a2", "222", "s1", "2000.00", AccountStatus.TERMINATION_REQUESTED),
                account("a3", "333", "s1", "3000.00", AccountStatus.TERMINATED),
                account("a4", "444", "s1", "4000.00", AccountStatus.INACTIVE),
            ),
            stores = listOf(store("s1", "8039", "ALAPAN")),
            providers = listOf(globe),
        )

        assertEquals(listOf("a1", "a2"), rows.map { it.accountId })
        assertEquals(AccountRecordStatus.ForDeactivation, rows.first { it.accountId == "a2" }.status)
    }

    @Test
    fun tc03_preview_rows_use_prorated_amounts_and_are_all_billable() {
        val preview = CompilePreview(
            providerId = "p-globe",
            billingPeriod = "2026-07",
            lines = listOf(
                CompilePreviewLine(
                    accountId = "a1",
                    accountNumber = "877911149",
                    branchCode = "8039",
                    storeName = "ALAPAN 1A",
                    fullAmount = "1998.00",
                    proratedAmount = "999.00",
                    isProrated = true,
                ),
            ),
            totalAmount = "999.00",
        )

        val rows = buildPreviewRows(preview, globe)

        assertEquals(1, rows.size)
        assertEquals("999.00", rows.single().amount)
        assertEquals(AccountRecordStatus.Active, rows.single().status)
        assertEquals(5, rows.single().billingDay)
    }

    @Test
    fun tc04_line_row_seeds_its_edit_buffer_with_the_saved_rfp() {
        val withRfp = TopSheetLine(
            id = "l1",
            topsheetId = "t1",
            accountId = "a1",
            billingPeriod = "2026-07",
            proratedAmount = "1998.00",
            fullAmount = "1998.00",
            rfpNumber = "010001",
            rfpSortOrder = 1,
        ).toLineRow()

        assertEquals("010001", withRfp.rfpInput)
        assertEquals("010001", withRfp.savedRfpNumber)
        assertEquals(false, withRfp.hasUnsavedRfp)
        assertEquals(true, withRfp.hasSavedRfp)

        val withoutRfp = withRfp.copy(savedRfpNumber = null, rfpInput = "")
        assertEquals(false, withoutRfp.hasSavedRfp)
    }

    @Test
    fun tc05_money_helpers_sum_in_cents_and_group_for_display() {
        assertEquals("760172.68", sumMoney(listOf("758174.68", "1998.00")))
        assertEquals("760,172.68", "760172.68".groupPeso())
        assertEquals("999.00", sumMoney(listOf("999")))
        assertEquals("1,998.00", "1998".groupPeso())
    }
}
