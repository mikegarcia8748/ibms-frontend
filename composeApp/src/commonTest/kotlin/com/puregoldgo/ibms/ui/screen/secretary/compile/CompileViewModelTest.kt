package com.puregoldgo.ibms.ui.screen.secretary.compile

import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.CompilePreviewLine
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.domain.usecase.AssignRfpUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ConfirmTopSheetUseCase
import com.puregoldgo.ibms.shared.domain.usecase.CreateTopSheetDraftUseCase
import com.puregoldgo.ibms.shared.domain.usecase.DeleteTopSheetLineUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetLinesUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.PreviewTopSheetUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateTopSheetLineUseCase
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreType
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CompileViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var topsheets: FakeTopSheetRepository
    private lateinit var providers: FakeCompileProviderRepository
    private lateinit var accounts: FakeCompileAccountRepository
    private lateinit var stores: FakeCompileStoreRepository

    private val globe = Provider(id = "p-globe", name = "Globe", paymentScheduleDay = 5)

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        topsheets = FakeTopSheetRepository()
        providers = FakeCompileProviderRepository(listOf(globe))
        accounts = FakeCompileAccountRepository(
            listOf(
                account("a1", "111", "s1", "1000.00"),
                account("a2", "222", "s2", "2000.00"),
            ),
        )
        stores = FakeCompileStoreRepository(
            listOf(
                store("s1", "8039", "ALAPAN"),
                store("s2", "8231", "BALIBAGO"),
            ),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun account(id: String, number: String, storeId: String, rate: String) = Account(
        id = id,
        accountNumber = number,
        providerId = "p-globe",
        storeId = storeId,
        rate = rate,
        status = AccountStatus.ACTIVE,
    )

    private fun store(id: String, code: String, name: String) = Store(
        id = id,
        storeType = StoreType.PUREGOLD,
        branchCode = code,
        name = name,
    )

    private fun viewModel() = CompileViewModel(
        getProviders = GetProvidersUseCase(providers),
        getAccounts = GetAccountsUseCase(accounts),
        getStores = GetStoresUseCase(stores),
        preview = PreviewTopSheetUseCase(topsheets),
        createDraft = CreateTopSheetDraftUseCase(topsheets),
        getLines = GetTopSheetLinesUseCase(topsheets),
        updateLine = UpdateTopSheetLineUseCase(topsheets),
        assignRfp = AssignRfpUseCase(topsheets),
        deleteLine = DeleteTopSheetLineUseCase(topsheets),
        confirm = ConfirmTopSheetUseCase(topsheets),
        getDrafts = GetTopSheetsUseCase(topsheets),
    )

    @Test
    fun tc01_browse_lists_all_accounts_with_a_summed_total() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.totalCount)
        assertEquals("3000.00", state.totalAmount)
        assertNull(state.loadError)
        assertTrue(!state.canCompile) // no provider chosen yet
    }

    @Test
    fun tc02_selecting_a_provider_switches_to_the_preview() = runTest(dispatcher) {
        topsheets.previewResult = CompilePreview(
            providerId = "p-globe",
            billingPeriod = "2026-07",
            lines = listOf(
                CompilePreviewLine(accountId = "a1", accountNumber = "111", fullAmount = "1000.00", proratedAmount = "500.00"),
            ),
            totalAmount = "500.00",
        )

        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.totalCount)
        assertEquals("500.00", state.totalAmount)
        assertTrue(state.canCompile)
    }

    @Test
    fun tc03_full_flow_from_compile_through_rfp_entry_to_confirm() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1", "a2")
        topsheets.draftResult = summary(id = "t1", status = "draft", invoice = null, batch = "GLOB-202607-B001")
        topsheets.lines += line("l1", rfp = null, order = 1)
        topsheets.lines += line("l2", rfp = null, order = 2)
        topsheets.confirmResult = summary(id = "t1", status = "compiled", invoice = "GLOB-202607-0012", batch = "GLOB-202607-B001")

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()

        vm.onCompileClick()
        testScheduler.advanceUntilIdle()
        assertEquals(CompilePhase.RfpEntry, vm.uiState.value.phase)
        assertEquals(2, vm.uiState.value.lines.size)
        assertTrue(!vm.uiState.value.canConfirm) // RFP numbers still missing

        vm.onRfpChange("l1", "010001")
        vm.onRfpCommit("l1")
        vm.onRfpChange("l2", "010002")
        vm.onRfpCommit("l2")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<Pair<String, String?>>("l1" to "010001", "l2" to "010002"), topsheets.patchedRfp)
        assertTrue(vm.uiState.value.canConfirm)

        vm.onConfirmClick()
        testScheduler.advanceUntilIdle()

        assertEquals(CompilePhase.Compiled, vm.uiState.value.phase)
        assertEquals("GLOB-202607-0012", vm.uiState.value.compiled?.invoiceNumber)
    }

    @Test
    fun tc04_non_numeric_rfp_input_is_filtered_to_digits() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1")
        topsheets.draftResult = summary("t1", "draft", null, "B1")
        topsheets.lines += line("l1", rfp = null, order = 1)

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()
        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        vm.onRfpChange("l1", "01A-0!01")
        assertEquals("01001", vm.uiState.value.lines.single().rfpInput)
    }

    @Test
    fun tc05_the_last_line_cannot_be_removed() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1")
        topsheets.draftResult = summary("t1", "draft", null, "B1")
        topsheets.lines += line("l1", rfp = null, order = 1)

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()
        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        vm.onRemoveLine("l1")
        testScheduler.advanceUntilIdle()

        assertTrue(topsheets.removedLines.isEmpty())
        assertEquals(1, vm.uiState.value.lines.size)
        assertTrue(vm.uiState.value.linesError != null)
    }

    @Test
    fun tc06_a_failed_compile_surfaces_the_servers_message() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1")
        topsheets.createError = "a draft already exists for this provider/period"

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()

        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        assertEquals(CompilePhase.Review, vm.uiState.value.phase)
        assertEquals("a draft already exists for this provider/period", vm.uiState.value.compileError)
    }

    @Test
    fun tc07_assign_rfp_range_numbers_every_line_and_enables_confirm() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1", "a2")
        topsheets.draftResult = summary("t1", "draft", null, "B1")
        topsheets.lines += line("l1", rfp = null, order = 1, branch = "118")
        topsheets.lines += line("l2", rfp = null, order = 2, branch = "119")

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()
        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        assertEquals(2, vm.uiState.value.distinctStoreCodeCount)
        assertTrue(!vm.uiState.value.canConfirm) // no RFP numbers yet

        vm.onRfpRangeStartChange("0100021")
        vm.onRfpRangeEndChange("0100022")
        assertTrue(vm.uiState.value.canAssignRfp)

        vm.onAssignRfpClick()
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("0100021" to "0100022"), topsheets.assignedRanges)
        assertTrue(vm.uiState.value.lines.all { it.hasSavedRfp })
        assertEquals("", vm.uiState.value.rfpRangeStart) // inputs cleared on success
        assertEquals("", vm.uiState.value.rfpRangeEnd)
        assertTrue(vm.uiState.value.canConfirm)
    }

    @Test
    fun tc08_a_failed_assign_surfaces_the_servers_message() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1", "a2")
        topsheets.draftResult = summary("t1", "draft", null, "B1")
        topsheets.lines += line("l1", rfp = null, order = 1, branch = "118")
        topsheets.lines += line("l2", rfp = null, order = 2, branch = "119")
        topsheets.assignRfpError = "RFP range covers 1 number(s) but there are 2 store code(s) to number"

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()
        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        vm.onRfpRangeStartChange("0100021")
        vm.onRfpRangeEndChange("0100021")
        vm.onAssignRfpClick()
        testScheduler.advanceUntilIdle()

        assertEquals(
            "RFP range covers 1 number(s) but there are 2 store code(s) to number",
            vm.uiState.value.assignRfpError,
        )
        assertTrue(vm.uiState.value.lines.none { it.hasSavedRfp })
    }

    @Test
    fun tc09_drafted_list_filter_handlers_update_state() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1", "a2")
        topsheets.draftResult = summary("t1", "draft", null, "B1")
        topsheets.lines += line("l1", rfp = null, order = 1, branch = "118")
        topsheets.lines += line("l2", rfp = null, order = 2, branch = "119")

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()
        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        vm.onLinesQueryChange("118")
        vm.onLinesLetterSelect('A')
        vm.onLinesSortSelect(LineSortOrder.MonthlyRecurringCharge)

        val state = vm.uiState.value
        assertEquals("118", state.linesQuery)
        assertEquals('A', state.linesLetter)
        assertEquals(LineSortOrder.MonthlyRecurringCharge, state.linesSort)
    }

    @Test
    fun tc10_back_to_review_resets_the_drafted_list_filters() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1")
        topsheets.draftResult = summary("t1", "draft", null, "B1")
        topsheets.lines += line("l1", rfp = null, order = 1, branch = "118")

        val vm = viewModel()
        testScheduler.advanceUntilIdle()
        vm.onProviderSelect("p-globe")
        testScheduler.advanceUntilIdle()
        vm.onCompileClick()
        testScheduler.advanceUntilIdle()

        vm.onLinesQueryChange("bali")
        vm.onLinesLetterSelect('B')
        vm.onLinesSortSelect(LineSortOrder.Alphabetical)

        vm.onBackToReview()

        val state = vm.uiState.value
        assertEquals(CompilePhase.Review, state.phase)
        assertEquals("", state.linesQuery)
        assertEquals(com.puregoldgo.ibms.ui.component.LETTER_ALL, state.linesLetter)
        assertEquals(LineSortOrder.StoreCode, state.linesSort)
    }

    @Test
    fun tc11_resume_draft_lists_only_drafts_and_loads_the_chosen_one_into_rfp_entry() = runTest(dispatcher) {
        topsheets.previewResult = previewWith("a1", "a2")
        topsheets.topSheets = listOf(
            summary("t1", "draft", null, "GLOB-202607-B001"),
            summary("t2", "compiled", "GLOB-202607-0012", "GLOB-202607-B001"),
        )
        topsheets.lines += line("l1", rfp = null, order = 1, branch = "118")
        topsheets.lines += line("l2", rfp = "010001", order = 2, branch = "119")

        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onResumeDraftClick()
        testScheduler.advanceUntilIdle()

        val listing = vm.uiState.value
        assertTrue(listing.showResumeDialog)
        assertTrue(!listing.isLoadingDrafts)
        assertNull(listing.draftsError)
        assertEquals(1, listing.drafts.size) // the COMPILED sheet is filtered out
        assertEquals("t1", listing.drafts.single().id)

        vm.onResumeDraft("t1")
        testScheduler.advanceUntilIdle()

        val resumed = vm.uiState.value
        assertEquals(CompilePhase.RfpEntry, resumed.phase)
        assertEquals("t1", resumed.draft?.id)
        assertTrue(!resumed.showResumeDialog)
        assertEquals("p-globe", resumed.selectedProviderId)
        assertEquals("2026-07", resumed.billingPeriod)
        assertEquals(2, resumed.lines.size)
    }

    private fun previewWith(vararg ids: String) = CompilePreview(
        providerId = "p-globe",
        billingPeriod = "2026-07",
        lines = ids.map {
            CompilePreviewLine(accountId = it, accountNumber = it, fullAmount = "1000.00", proratedAmount = "1000.00")
        },
        totalAmount = "1000.00",
    )

    private fun summary(id: String, status: String, invoice: String?, batch: String) = TopSheetSummary(
        id = id,
        invoiceNumber = invoice,
        batchNumber = batch,
        billingPeriod = "2026-07",
        providerId = "p-globe",
        providerName = "Globe",
        accountCount = 2,
        totalAmount = "3000.00",
        status = com.puregoldgo.ibms.shared.model.TopsheetStatus.valueOf(status.uppercase()),
        compilerId = "u1",
        compilationDate = "2026-07-22T08:00:00Z",
    )

    private fun line(id: String, rfp: String?, order: Int, branch: String? = null) = TopSheetLine(
        id = id,
        topsheetId = "t1",
        accountId = id,
        billingPeriod = "2026-07",
        proratedAmount = "1500.00",
        fullAmount = "1500.00",
        branchCode = branch,
        rfpNumber = rfp,
        rfpSortOrder = order,
    )
}
