package com.puregoldgo.ibms.ui.screen.secretary

import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.shared.domain.usecase.CreateAccountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the secretary console's account-creation path.
 *
 * The panel still renders sample data for the browse lists, but the
 * create-account dialog is fully wired, so these tests focus on validation,
 * the store-with-existing-account confirmation gate, and successful submission.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SecretaryViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var accounts: FakeAccountRepository
    private lateinit var auth: FakeAuthRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
        accounts = FakeAccountRepository()
        auth = FakeAuthRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SecretaryViewModel(
        createAccount = CreateAccountUseCase(accounts),
        accountRepository = accounts,
        authRepository = auth,
    )

    @Test
    fun tc01_submit_without_required_fields_shows_validation_error() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<SecretaryUiEvent>()
        backgroundScope.launch { vm.uiEvent.collect(events::add) }

        vm.onAddAccountClick()
        vm.onAddAccountSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events.first() is SecretaryUiEvent.ShowSnackbar)
        assertTrue(accounts.createdRequests.isEmpty())
    }

    @Test
    fun tc02_store_with_existing_account_emits_confirmation_event() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        val events = mutableListOf<SecretaryUiEvent>()
        backgroundScope.launch { vm.uiEvent.collect(events::add) }

        vm.onAddAccountClick()
        fillValidForm(vm, storeId = "br-alapan-1a")
        testScheduler.advanceUntilIdle()
        vm.onAddAccountSubmit()
        testScheduler.advanceUntilIdle()

        val event = events.single()
        assertTrue(event is SecretaryUiEvent.ShowStoreHasAccountConfirmation)
        assertEquals("ALAPAN 1A (Imus Cavite)", event.storeName)
        assertTrue(accounts.createdRequests.isEmpty())
    }

    @Test
    fun tc03_successful_creation_closes_dialog_refreshes_list_and_emits_snackbar() =
        runTest(dispatcher) {
            val vm = viewModel()
            testScheduler.advanceUntilIdle()

            val events = mutableListOf<SecretaryUiEvent>()
            backgroundScope.launch { vm.uiEvent.collect(events::add) }

            vm.onAddAccountClick()
            fillValidForm(vm, storeId = "br-aguado")
            testScheduler.advanceUntilIdle()
            vm.onAddAccountSubmit()
            testScheduler.advanceUntilIdle()

            assertNull(vm.uiState.value.addAccount)
            assertEquals(1, accounts.createdRequests.size)

            val request = accounts.createdRequests.single()
            assertEquals("5440-123", request.accountNumber)
            assertEquals("br-aguado", request.storeId)
            assertEquals("prv-globe", request.providerId)
            assertEquals("1500.00", request.rate)
            assertEquals("2026-08-01", request.installationDate)
            assertEquals("CRT-987654321", request.circuitId)
            assertEquals(listOf("att-1"), request.subscriptionProofIds)

            assertTrue(
                events.any {
                    it is SecretaryUiEvent.ShowSnackbar && it.message == "Account registered successfully"
                },
            )
        }

    @Test
    fun tc04_confirmation_after_warning_creates_account() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        backgroundScope.launch { vm.uiEvent.collect { } }

        vm.onAddAccountClick()
        fillValidForm(vm, storeId = "br-alapan-1a")
        testScheduler.advanceUntilIdle()
        vm.onAddAccountSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(accounts.createdRequests.isEmpty())

        vm.onAddAccountSubmitConfirmed()
        testScheduler.advanceUntilIdle()

        assertEquals(1, accounts.createdRequests.size)
        assertNull(vm.uiState.value.addAccount)
    }

    @Test
    fun tc05_rate_input_keeps_only_digits_decimal_and_commas() = runTest(dispatcher) {
        val vm = viewModel()
        testScheduler.advanceUntilIdle()

        vm.onAddAccountClick()
        vm.onNewAccountRateChange("1a5b0.0!0")

        assertEquals("150.00", vm.uiState.value.addAccount?.monthlyRate)
    }

    private fun fillValidForm(vm: SecretaryViewModel, storeId: String) {
        vm.onNewAccountNumberChange("5440-123")
        vm.onNewAccountStoreChange(storeId)
        vm.onNewAccountProviderChange("prv-globe")
        vm.onNewAccountInstallationDateChange("2026-08-01")
        vm.onNewAccountCircuitIdChange("CRT-987654321")
        vm.onNewAccountRateChange("1,500.00")
        vm.onNewAccountProofPicked(
            PickedFile(
                name = "contract.pdf",
                mimeType = "application/pdf",
                bytes = byteArrayOf(1, 2, 3),
            ),
        )
    }
}
