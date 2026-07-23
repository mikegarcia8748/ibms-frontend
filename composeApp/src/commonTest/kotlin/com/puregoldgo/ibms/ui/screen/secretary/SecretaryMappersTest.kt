package com.puregoldgo.ibms.ui.screen.secretary

import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.StoreType
import com.puregoldgo.ibms.shared.model.TopsheetStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecretaryMappersTest {

    private fun provider(
        id: String,
        name: String = "Globe",
        status: ProviderStatus = ProviderStatus.ACTIVE,
    ) = Provider(
        id = id,
        name = name,
        paymentScheduleDay = 5,
        status = status,
    )

    private fun store(
        id: String,
        branchCode: String = "8039",
        name: String = "ALAPAN 1A",
        city: String? = null,
        status: StoreStatus = StoreStatus.ACTIVE,
        closedReason: String? = null,
    ) = Store(
        id = id,
        storeType = StoreType.PUREGOLD,
        branchCode = branchCode,
        name = name,
        city = city,
        status = status,
        closedReason = closedReason,
    )

    private fun account(
        id: String,
        accountNumber: String = "877911149",
        providerId: String = "p-globe",
        storeId: String = "s1",
        rate: String = "1500.00",
        status: AccountStatus = AccountStatus.ACTIVE,
        circuitId: String? = null,
        planName: String? = null,
        speed: String? = null,
        contractDurationMonths: Int? = null,
        installationDate: String? = null,
        createdAt: String? = null,
        contractStartDate: String? = null,
        contractEndDate: String? = null,
    ) = Account(
        id = id,
        accountNumber = accountNumber,
        providerId = providerId,
        storeId = storeId,
        rate = rate,
        status = status,
        circuitId = circuitId,
        planName = planName,
        speed = speed,
        contractDurationMonths = contractDurationMonths,
        installationDate = installationDate,
        createdAt = createdAt,
        contractStartDate = contractStartDate,
        contractEndDate = contractEndDate,
    )

    // ── buildProviderRows ────────────────────────────────────────────────────

    @Test
    fun `buildProviderRows maps active and inactive correctly`() {
        val rows = buildProviderRows(listOf(
            provider(id = "p-1", status = ProviderStatus.ACTIVE),
            provider(id = "p-2", name = "PLDT", status = ProviderStatus.INACTIVE),
        ))

        assertEquals(2, rows.size)
        assertTrue(rows[0].isActive)
        assertEquals(false, rows[1].isActive)
        assertEquals("Globe", rows[0].name)
        assertEquals("PLDT", rows[1].name)
    }

    // ── buildBranchRows ──────────────────────────────────────────────────────

    @Test
    fun `buildBranchRows maps status correctly`() {
        val rows = buildBranchRows(
            stores = listOf(
                store(id = "s1", status = StoreStatus.ACTIVE),
                store(id = "s2", status = StoreStatus.CLOSED, closedReason = "Lease ended"),
                store(id = "s3", status = StoreStatus.INACTIVE),
            ),
            accounts = emptyList(),
        )

        assertEquals(3, rows.size)
        assertEquals(BranchRecordStatus.Active, rows[0].status)
        assertEquals(BranchRecordStatus.Closed, rows[1].status)
        assertEquals(BranchRecordStatus.Inactive, rows[2].status)
        assertEquals("Lease ended", rows[1].closureReason)
    }

    @Test
    fun `buildBranchRows joins providerIds from accounts`() {
        val rows = buildBranchRows(
            stores = listOf(store(id = "s1")),
            accounts = listOf(
                account(id = "a1", providerId = "p-globe", storeId = "s1"),
                account(id = "a2", providerId = "p-pldt", storeId = "s1"),
            ),
        )

        assertEquals(setOf("p-globe", "p-pldt"), rows.single().providerIds)
    }

    @Test
    fun `buildBranchRows handles store with no accounts`() {
        val rows = buildBranchRows(
            stores = listOf(store(id = "s1")),
            accounts = emptyList(),
        )

        assertTrue(rows.single().providerIds.isEmpty())
    }

    // ── buildAccountRows ─────────────────────────────────────────────────────

    @Test
    fun `buildAccountRows maps status and rate`() {
        val rows = buildAccountRows(
            accounts = listOf(
                account(id = "a1", rate = "1500.00", status = AccountStatus.ACTIVE),
                account(id = "a2", rate = "2000.00", status = AccountStatus.TERMINATION_REQUESTED),
                account(id = "a3", rate = "3000.00", status = AccountStatus.TERMINATED),
                account(id = "a4", rate = "4000.00", status = AccountStatus.TRANSFERRED),
                account(id = "a5", rate = "5000.00", status = AccountStatus.INACTIVE),
            ),
            stores = listOf(store(id = "s1")),
        )

        assertEquals(5, rows.size)
        assertEquals(AccountRecordStatus.Active, rows[0].status)
        assertEquals(AccountRecordStatus.ForDeactivation, rows[1].status)
        assertEquals(AccountRecordStatus.Terminated, rows[2].status)
        assertEquals(AccountRecordStatus.Terminated, rows[3].status)
        assertEquals(AccountRecordStatus.Terminated, rows[4].status)
        assertEquals("1,500.00", rows[0].monthlyRate)
    }

    @Test
    fun `buildAccountRows joins store name`() {
        val rows = buildAccountRows(
            accounts = listOf(account(id = "a1", storeId = "s1")),
            stores = listOf(store(id = "s1", name = "ALAPAN 1A", city = "Imus Cavite")),
        )

        assertEquals("ALAPAN 1A (Imus Cavite)", rows.single().storeName)
    }

    // ── buildStoreDetail ─────────────────────────────────────────────────────

    @Test
    fun `buildStoreDetail includes linked accounts`() {
        val detail = buildStoreDetail(
            store = store(id = "s1"),
            accounts = listOf(
                account(id = "a1", storeId = "s1"),
                account(id = "a2", storeId = "s1"),
                account(id = "a3", storeId = "s2"),
            ),
            providers = emptyList(),
        )

        assertEquals(2, detail.linkedAccounts.size)
        assertEquals(listOf("a1", "a2"), detail.linkedAccounts.map { it.accountId })
    }

    // ── buildAccountDetail ───────────────────────────────────────────────────

    @Test
    fun `buildAccountDetail maps all fields`() {
        val detail = buildAccountDetail(
            account = account(
                id = "a1",
                accountNumber = "877911149",
                providerId = "p-globe",
                storeId = "s1",
                rate = "1500.00",
                status = AccountStatus.ACTIVE,
                circuitId = "CIR-001",
                planName = "Business 200",
                speed = "200Mbps",
                contractDurationMonths = 24,
                installationDate = "2025-01-15",
                createdAt = "2025-01-10T00:00:00Z",
                contractStartDate = "2025-01-01",
                contractEndDate = "2026-12-31",
            ),
            store = store(id = "s1", branchCode = "8039", name = "ALAPAN 1A", city = "Imus Cavite"),
            provider = provider(id = "p-globe", name = "Globe"),
        )

        assertEquals("877911149", detail.accountNumber)
        assertEquals("Globe", detail.providerName)
        assertEquals("Business 200", detail.planName)
        assertEquals("CIR-001", detail.circuitId)
        assertEquals("ALAPAN 1A (Imus Cavite)", detail.storeName)
        assertEquals("8039", detail.branchCode)
        assertEquals("1,500.00", detail.monthlyRate)
        assertEquals("200Mbps", detail.speed)
        assertEquals(24, detail.contractDurationMonths)
        assertEquals("2025-01-15", detail.installationDate)
        assertEquals("2025-01-10T00:00:00Z", detail.createdAt)
        assertEquals("2025-01-01", detail.contractStartDate)
        assertEquals("2026-12-31", detail.contractEndDate)
        assertEquals(AccountRecordStatus.Active, detail.status)
    }

    // ── groupPeso ────────────────────────────────────────────────────────────

    @Test
    fun `groupPeso formats correctly`() {
        assertEquals("1,500.00", "1500.00".groupPeso())
        assertEquals("760,172.68", "760172.68".groupPeso())
        assertEquals("50.50", "50.5".groupPeso())
        assertEquals("0.00", "0".groupPeso())
        assertEquals("-1,500.00", "-1500.00".groupPeso())
    }

    // ── buildTopSheetRows ────────────────────────────────────────────────────

    private fun summary(
        id: String,
        invoiceNumber: String? = "GLOB-202607-0007",
        batchNumber: String? = null,
        providerName: String? = "Globe",
        accountCount: Int = 3,
        totalAmount: String = "760172.68",
        status: TopsheetStatus = TopsheetStatus.COMPILED,
    ) = TopSheetSummary(
        id = id,
        invoiceNumber = invoiceNumber,
        batchNumber = batchNumber,
        billingPeriod = "2026-07",
        providerName = providerName,
        accountCount = accountCount,
        totalAmount = totalAmount,
        status = status,
        compilerId = "u1",
        compilationDate = "2026-07-19T00:00:00Z",
    )

    @Test
    fun `buildTopSheetRows maps status and groups total and formats date`() {
        val rows = buildTopSheetRows(listOf(summary(id = "ts1")))

        val row = rows.single()
        assertEquals("GLOB-202607-0007", row.invoiceNumber)
        assertEquals("Globe", row.providerName)
        assertEquals("760,172.68", row.totalValidated)
        assertEquals("Jul 19, 2026", row.generatedOn)
        assertEquals(TopSheetRecordStatus.Compiled, row.status)
        assertEquals("GLOB-202607-0007", row.reference)
    }

    @Test
    fun `buildTopSheetRows keeps drafts with no invoice and uses batch as reference`() {
        val rows = buildTopSheetRows(
            listOf(
                summary(id = "ts-draft", invoiceNumber = null, batchNumber = "BATCH-42", status = TopsheetStatus.DRAFT),
            ),
        )

        val row = rows.single()
        assertNull(row.invoiceNumber)
        assertEquals(TopSheetRecordStatus.Draft, row.status)
        assertEquals("BATCH-42", row.reference)
    }

    @Test
    fun `buildTopSheetRows maps each status`() {
        val rows = buildTopSheetRows(
            listOf(
                summary(id = "a", status = TopsheetStatus.DRAFT),
                summary(id = "b", status = TopsheetStatus.COMPILED),
                summary(id = "c", status = TopsheetStatus.APPROVED),
                summary(id = "d", status = TopsheetStatus.PAID),
            ),
        )

        assertEquals(
            listOf(
                TopSheetRecordStatus.Draft,
                TopSheetRecordStatus.Compiled,
                TopSheetRecordStatus.Approved,
                TopSheetRecordStatus.Paid,
            ),
            rows.map { it.status },
        )
    }

    // ── buildTopSheetLineRows ─────────────────────────────────────────────────

    private fun line(
        accountId: String,
        branchCode: String? = "8039",
        storeName: String? = "ALAPAN 1A",
        accountNumber: String? = "877911149",
        fullAmount: String = "2000.00",
        proratedAmount: String = "1500.00",
        rfpNumber: String? = "000123",
        rfpSortOrder: Int? = 1,
    ) = TopSheetLine(
        id = "l-$accountId",
        topsheetId = "ts1",
        accountId = accountId,
        billingPeriod = "2026-07",
        proratedAmount = proratedAmount,
        fullAmount = fullAmount,
        branchCode = branchCode,
        storeName = storeName,
        accountNumber = accountNumber,
        rfpNumber = rfpNumber,
        rfpSortOrder = rfpSortOrder,
    )

    @Test
    fun `buildTopSheetLineRows maps fields and groups amounts and parses prorated cents`() {
        val row = buildTopSheetLineRows(listOf(line(accountId = "a1"))).single()

        assertEquals("a1", row.accountId)
        assertEquals("8039", row.storeCode)
        assertEquals("ALAPAN 1A", row.storeName)
        assertEquals("877911149", row.accountNumber)
        assertEquals("2,000.00", row.fullMrc)
        assertEquals("1,500.00", row.prorated)
        assertEquals(150000L, row.proratedCents)
        assertEquals("000123", row.rfpNumber)
    }

    // ── TopSheetDetailUIState.visibleLines ─────────────────────────────────

    private fun stateWithLines(
        lines: List<TopSheetLineRow>,
        query: String = "",
        sort: TopSheetLineSortKey = TopSheetLineSortKey.RfpNumber,
        ascending: Boolean = true,
    ) = TopSheetDetailUIState(
        header = buildTopSheetRows(listOf(summary(id = "ts1"))).single(),
        lines = lines,
        lineQuery = query,
        lineSort = sort,
        lineSortAsc = ascending,
    )

    private val sampleLines = buildTopSheetLineRows(
        listOf(
            line(accountId = "a1", branchCode = "8039", storeName = "ALAPAN", accountNumber = "111", proratedAmount = "3000.00", rfpNumber = "3"),
            line(accountId = "a2", branchCode = "1002", storeName = "BACOOR", accountNumber = "222", proratedAmount = "1000.00", rfpNumber = "1"),
            line(accountId = "a3", branchCode = "5005", storeName = "CAVITE", accountNumber = "333", proratedAmount = "2000.00", rfpNumber = "2"),
        ),
    )

    @Test
    fun `visibleLines filters by store code or name or account number`() {
        assertEquals(listOf("a1"), stateWithLines(sampleLines, query = "8039").visibleLines.map { it.accountId })
        assertEquals(listOf("a2"), stateWithLines(sampleLines, query = "baco").visibleLines.map { it.accountId })
        assertEquals(listOf("a3"), stateWithLines(sampleLines, query = "333").visibleLines.map { it.accountId })
    }

    @Test
    fun `visibleLines sorts by store code both directions`() {
        assertEquals(
            listOf("a2", "a3", "a1"),
            stateWithLines(sampleLines, sort = TopSheetLineSortKey.StoreCode, ascending = true).visibleLines.map { it.accountId },
        )
        assertEquals(
            listOf("a1", "a3", "a2"),
            stateWithLines(sampleLines, sort = TopSheetLineSortKey.StoreCode, ascending = false).visibleLines.map { it.accountId },
        )
    }

    @Test
    fun `visibleLines sorts by prorated amount`() {
        assertEquals(
            listOf("a2", "a3", "a1"),
            stateWithLines(sampleLines, sort = TopSheetLineSortKey.Amount, ascending = true).visibleLines.map { it.accountId },
        )
    }

    @Test
    fun `visibleLines sorts by rfp number`() {
        assertEquals(
            listOf("a2", "a3", "a1"),
            stateWithLines(sampleLines, sort = TopSheetLineSortKey.RfpNumber, ascending = true).visibleLines.map { it.accountId },
        )
    }
}
