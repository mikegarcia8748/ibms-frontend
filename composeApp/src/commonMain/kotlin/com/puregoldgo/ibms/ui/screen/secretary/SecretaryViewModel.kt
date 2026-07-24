package com.puregoldgo.ibms.ui.screen.secretary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.domain.usecase.CreateAccountUseCase
import com.puregoldgo.ibms.shared.model.CreateAccountRequest
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the secretary console.
 *
 * **Renders sample data** for the browse lists; the create-account path is fully
 * wired to the backend so the new Add ISP Account dialog can actually submit.
 * Filtering already lives in [SecretaryUIState] rather than in the composables,
 * so the rules are testable now and the previews can show a filtered list
 * without running a ViewModel.
 */
class SecretaryViewModel(
    private val createAccount: CreateAccountUseCase,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        SecretaryUIState(
            userName = CurrentUserStore.name.orEmpty(),
            userRole = CurrentUserStore.role.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<SecretaryUiEvent>(extraBufferCapacity = 1)
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    init {
        loadPanel()
    }

    // region Public API

    /** Seeds the console. Synchronous today; a network call once wired. */
    fun loadPanel() {
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                providers = SecretarySampleData.providers,
                branches = SecretarySampleData.branches,
                accounts = SecretarySampleData.accounts,
                topSheets = SecretarySampleData.topSheets,
            )
        }
    }

    fun onTabSelect(tab: SecretaryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // endregion

    // region Branch locations

    fun onBranchQueryChange(query: String) {
        _uiState.update { it.copy(branchQuery = query) }
    }

    fun onBranchLetterSelect(letter: Char) {
        _uiState.update { it.copy(branchLetter = letter) }
    }

    fun onBranchProviderSelect(providerId: String?) {
        _uiState.update { it.copy(branchProviderId = providerId) }
    }

    // endregion

    // region ISP accounts

    fun onAccountQueryChange(query: String) {
        _uiState.update { it.copy(accountQuery = query) }
    }

    fun onAccountLetterSelect(letter: Char) {
        _uiState.update { it.copy(accountLetter = letter) }
    }

    fun onAccountProviderSelect(providerId: String?) {
        _uiState.update { it.copy(accountProviderId = providerId) }
    }

    fun onAccountStatusSelect(status: AccountRecordStatus?) {
        _uiState.update { it.copy(accountStatus = status) }
    }

    /** TODO: `GET /exports/topsheet/{id}.xlsx`, then hand the bytes to the platform. */
    fun onExportAccounts() = Unit

    // endregion

    fun onInvoiceQueryChange(query: String) {
        _uiState.update { it.copy(invoiceQuery = query) }
    }

    // region Add branch

    /** Opens on a clean slate — a stale form would submit yesterday's branch. */
    fun onAddBranchClick() {
        _uiState.update { it.copy(addBranch = NewBranchForm(), addAccount = null) }
    }

    fun onNewBranchCodeChange(code: String) = updateBranchForm { it.copy(branchCode = code) }

    fun onNewBranchNameChange(name: String) = updateBranchForm { it.copy(name = name) }

    fun onNewBranchCityChange(city: String) = updateBranchForm { it.copy(city = city) }

    fun onNewBranchProviderChange(providerId: String?) =
        updateBranchForm { it.copy(providerId = providerId) }

    /** TODO: `POST /stores`. Inert until then — see the dialog's own banner. */
    fun onAddBranchSubmit() = Unit

    fun onAddBranchDismiss() {
        _uiState.update { it.copy(addBranch = null) }
    }

    // endregion

    // region Add account

    fun onAddAccountClick() {
        _uiState.update { it.copy(addAccount = NewAccountForm(), addBranch = null) }
    }

    fun onNewAccountNumberChange(number: String) =
        updateAccountForm { it.copy(accountNumber = number) }

    fun onNewAccountStoreChange(storeId: String?) =
        updateAccountForm { it.copy(storeId = storeId) }

    fun onNewAccountProviderChange(providerId: String?) =
        updateAccountForm { it.copy(providerId = providerId) }

    /**
     * Keeps digits, one decimal point and grouping commas.
     *
     * Filtered as typed rather than validated on submit: this becomes a decimal
     * on the wire, and a stray letter would be rejected by the backend long
     * after the secretary has moved on from the field.
     */
    fun onNewAccountRateChange(rate: String) = updateAccountForm { form ->
        form.copy(
            monthlyRate = rate.filter { it.isDigit() || it == '.' || it == ',' },
        )
    }

    fun onNewAccountInstallationDateChange(date: String) =
        updateAccountForm { it.copy(installationDate = date) }

    fun onNewAccountCircuitIdChange(circuitId: String) =
        updateAccountForm { it.copy(circuitId = circuitId) }

    fun onNewAccountBillingPeriodChange(billingPeriod: String) =
        updateAccountForm { it.copy(billingPeriodLabel = billingPeriod) }

    fun onNewAccountPlanChange(plan: String) =
        updateAccountForm { it.copy(planName = plan) }

    /**
     * Uploads a proof file as soon as it is picked, then stores only the
     * attachment id. Holding the bytes in Compose state would recompose on
     * every equality check because ByteArray compares by identity.
     */
    fun onNewAccountProofPicked(file: PickedFile) {
        viewModelScope.launch {
            updateAccountForm { it.copy(proofUploadError = null) }
            try {
                val attachmentId = accountRepository.uploadAttachment(
                    fileName = file.name,
                    mimeType = file.mimeType,
                    bytes = file.bytes,
                )
                updateAccountForm { form ->
                    form.copy(proofAttachmentIds = form.proofAttachmentIds + attachmentId)
                }
            } catch (e: Exception) {
                updateAccountForm { it.copy(proofUploadError = e.message ?: "Upload failed") }
                _uiEvent.emit(
                    SecretaryUiEvent.ShowSnackbar(
                        e.message ?: "Proof upload failed. Please try again.",
                    ),
                )
            }
        }
    }

    fun onNewAccountProofRemove(attachmentId: String) {
        updateAccountForm { form ->
            form.copy(proofAttachmentIds = form.proofAttachmentIds - attachmentId)
        }
    }

    /**
     * Validates the form and either proceeds directly or asks for confirmation
     * when the selected store already has an active account.
     */
    fun onAddAccountSubmit() {
        val form = _uiState.value.addAccount ?: return
        val validationError = validateAccountForm(form)
        if (validationError != null) {
            viewModelScope.launch {
                _uiEvent.emit(SecretaryUiEvent.ShowSnackbar(validationError))
            }
            return
        }

        val storeId = form.storeId ?: return
        val storeHasAccount = _uiState.value.accounts.any {
            it.storeId == storeId && it.isLive
        }

        if (storeHasAccount) {
            val storeName = _uiState.value.branches.find { it.id == storeId }?.displayName ?: storeId
            viewModelScope.launch {
                _uiEvent.emit(SecretaryUiEvent.ShowStoreHasAccountConfirmation(storeName))
            }
            return
        }

        createAccount(form)
    }

    /** Called after the user confirms they want to add another account to the store. */
    fun onAddAccountSubmitConfirmed() {
        val form = _uiState.value.addAccount ?: return
        createAccount(form)
    }

    fun onAddAccountDismiss() {
        _uiState.update { it.copy(addAccount = null) }
    }

    // endregion

    /** Drops the stored session. There is no logout endpoint to call. */
    fun onLogout() {
        authRepository.signOut()
        viewModelScope.launch {
            _uiEvent.emit(SecretaryUiEvent.NavigateToLogin)
        }
    }

    // region Internals

    private fun updateBranchForm(transform: (NewBranchForm) -> NewBranchForm) {
        _uiState.update { state ->
            state.addBranch?.let { state.copy(addBranch = transform(it)) } ?: state
        }
    }

    private fun updateAccountForm(transform: (NewAccountForm) -> NewAccountForm) {
        _uiState.update { state ->
            state.addAccount?.let { state.copy(addAccount = transform(it)) } ?: state
        }
    }

    private fun validateAccountForm(form: NewAccountForm): String? {
        if (form.accountNumber.isBlank()) return "Account number is required"
        Validation.validateAccountNumber(form.accountNumber)?.let { return it }
        if (form.storeId == null) return "Store is required"
        if (form.providerId == null) return "ISP provider is required"
        Validation.validateRate(form.monthlyRate)?.let { return it }
        if (form.installationDate.isBlank()) return "Installation date is required"
        if (form.circuitId.isBlank()) return "Circuit ID is required"
        if (form.proofAttachmentIds.isEmpty()) return "At least one proof of installation is required"
        return null
    }

    private fun createAccount(form: NewAccountForm) {
        val request = CreateAccountRequest(
            accountNumber = form.accountNumber.trim(),
            providerId = form.providerId.orEmpty(),
            storeId = form.storeId.orEmpty(),
            rate = form.monthlyRate.trim().replace(",", ""),
            installationDate = form.installationDate.trim(),
            circuitId = form.circuitId.trim(),
            billingPeriodLabel = form.billingPeriodLabel.trim().takeIf { it.isNotBlank() },
            planName = form.planName.trim().takeIf { it.isNotBlank() },
            subscriptionProofIds = form.proofAttachmentIds,
        )

        viewModelScope.launch {
            updateAccountForm { it.copy(isSubmitting = true) }

            createAccount(request).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        updateAccountForm { it.copy(isSubmitting = false) }
                        _uiState.update { it.copy(addAccount = null) }
                        loadPanel()
                        _uiEvent.emit(SecretaryUiEvent.ShowSnackbar("Account registered successfully"))
                    }
                    is Resource.Failed -> {
                        updateAccountForm { it.copy(isSubmitting = false) }
                        _uiEvent.emit(
                            SecretaryUiEvent.ShowSnackbar(
                                resource.message ?: "Failed to register account",
                            ),
                        )
                    }
                    is Resource.Error -> {
                        updateAccountForm { it.copy(isSubmitting = false) }
                        _uiEvent.emit(
                            SecretaryUiEvent.ShowSnackbar(
                                resource.error?.message ?: "Failed to register account",
                            ),
                        )
                    }
                }
            }
        }
    }

    // endregion
}
