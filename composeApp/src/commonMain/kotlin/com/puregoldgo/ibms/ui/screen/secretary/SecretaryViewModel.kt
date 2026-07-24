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
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetsUseCase
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.Store
import kotlinx.coroutines.async
import com.puregoldgo.ibms.shared.domain.usecase.CreateStoreUseCase
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
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
 * Loads providers, stores, accounts and the compiled topsheets concurrently from
 * the API. Store and account detail modals are built on-click from the cached
 * domain lists (no round-trip). Tapping a topsheet row navigates to the
 * full-screen [TopSheetDetailScreen] via a one-shot event.
 */
class SecretaryViewModel(
    private val getProviders: GetProvidersUseCase,
    private val getStores: GetStoresUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getTopSheets: GetTopSheetsUseCase,
    private val createAccount: CreateAccountUseCase,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val createStore: CreateStoreUseCase,
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

    private var cachedStores: List<Store> = emptyList()
    private var cachedAccounts: List<Account> = emptyList()
    private var cachedProviders: List<Provider> = emptyList()

    init {
        loadPanel()
    }

    // region Public API

    /** Fetches providers, stores, accounts and the compiled topsheets concurrently. */
    fun loadPanel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }

            val providersDeferred = async { getProviders().last() }
            val storesDeferred = async { getStores().last() }
            val accountsDeferred = async { getAccounts().last() }
            val topSheetsDeferred = async { getTopSheets().last() }

            val providersResult = providersDeferred.await()
            val storesResult = storesDeferred.await()
            val accountsResult = accountsDeferred.await()
            val topSheetsResult = topSheetsDeferred.await()

            val providers = providersResult.data
            val stores = storesResult.data
            val accounts = accountsResult.data
            val topSheets = topSheetsResult.data

            if (providers == null || stores == null || accounts == null || topSheets == null) {
                failLoad(
                    firstErrorMessage(providersResult, storesResult, accountsResult, topSheetsResult),
                )
                return@launch
            }

            cachedProviders = providers
            cachedStores = stores
            cachedAccounts = accounts

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadError = null,
                    providers = buildProviderRows(providers),
                    branches = buildBranchRows(stores, accounts),
                    accounts = buildAccountRows(accounts, stores),
                    topSheets = buildTopSheetRows(topSheets),
                )
            }
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

    // region Topsheet navigation

    /**
     * Sets the row in [TopSheetRowHolder] and emits a navigation event so the
     * full-screen detail can show the account lines with room to breathe.
     */
    fun onTopSheetClick(topSheetId: String) {
        val row = _uiState.value.topSheets.find { it.id == topSheetId } ?: return
        TopSheetRowHolder.current = row
        viewModelScope.launch {
            _uiEvent.emit(SecretaryUiEvent.NavigateToTopSheetDetail(topSheetId))
        }
    }

    // endregion

    // region Add branch

    /** Opens on a clean slate — a stale form would submit yesterday's branch. */
    fun onRegisterBranchClick() {
        _uiState.update { it.copy(registerBranchForm = RegisterBranchForm(), addAccount = null) }
    }

    fun onRegisterBranchStoreTypeChange(storeType: com.puregoldgo.ibms.shared.model.StoreType) =
        updateRegisterBranchForm { it.copy(storeType = storeType) }

    fun onRegisterBranchCodeChange(code: String) = updateRegisterBranchForm {
        it.copy(branchCode = code, branchCodeError = null)
    }

    fun onRegisterBranchNameChange(name: String) = updateRegisterBranchForm {
        it.copy(branchName = name, branchNameError = null)
    }

    fun onRegisterBranchRegionChange(region: String) =
        updateRegisterBranchForm { it.copy(region = region) }

    fun onRegisterBranchProvinceChange(province: String) =
        updateRegisterBranchForm { it.copy(province = province) }

    fun onRegisterBranchCityChange(city: String) =
        updateRegisterBranchForm { it.copy(city = city) }

    fun onRegisterBranchBarangayChange(barangay: String) =
        updateRegisterBranchForm { it.copy(barangay = barangay) }

    fun onRegisterBranchPostalCodeChange(postalCode: String) =
        updateRegisterBranchForm { it.copy(postalCode = postalCode) }

    /** Submits the form to `POST /stores` and refreshes the panel on success. */
    fun onRegisterBranchSubmit() {
        val form = _uiState.value.registerBranchForm ?: return
        if (!form.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(registerBranchForm = form.copy(isSaving = true)) }

            createStore(
                storeType = form.storeType,
                branchCode = form.branchCode,
                name = form.branchName,
                region = form.region.takeIf { it.isNotBlank() },
                province = form.province.takeIf { it.isNotBlank() },
                city = form.city.takeIf { it.isNotBlank() },
                barangay = form.barangay.takeIf { it.isNotBlank() },
                postal = form.postalCode.takeIf { it.isNotBlank() },
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        _uiState.update { it.copy(registerBranchForm = null) }
                        loadPanel()
                        _uiEvent.emit(SecretaryUiEvent.ShowSnackbar("Branch registered."))
                    }
                    is Resource.Failed -> {
                        _uiState.update {
                            it.copy(registerBranchForm = form.copy(isSaving = false))
                        }
                        _uiEvent.emit(
                            SecretaryUiEvent.ShowSnackbar(resource.message ?: "Could not register branch."),
                        )
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(registerBranchForm = form.copy(isSaving = false))
                        }
                        _uiEvent.emit(
                            SecretaryUiEvent.ShowSnackbar(resource.error?.message ?: "Could not register branch."),
                        )
                    }
                }
            }
        }
    }

    fun onRegisterBranchDismiss() {
        _uiState.update { it.copy(registerBranchForm = null) }
    }

    // endregion

    // region Add account

    fun onAddAccountClick() {
        _uiState.update { it.copy(addAccount = NewAccountForm()) }
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

    // region Detail modals

    fun onBranchClick(storeId: String) {
        val store = cachedStores.find { it.id == storeId } ?: return
        val detail = buildStoreDetail(store, cachedAccounts, cachedProviders)
        _uiState.update { it.copy(storeDetail = detail) }
    }

    fun onStoreDetailDismiss() {
        _uiState.update { it.copy(storeDetail = null) }
    }

    fun onAccountClick(accountId: String) {
        val account = cachedAccounts.find { it.id == accountId } ?: return
        val store = cachedStores.find { it.id == account.storeId }
        val provider = cachedProviders.find { it.id == account.providerId }
        val detail = buildAccountDetail(account, store, provider)
        _uiState.update { it.copy(accountDetail = detail) }
    }

    fun onAccountDetailDismiss() {
        _uiState.update { it.copy(accountDetail = null) }
    }

    // endregion

    // region Deactivate account

    /** Opens the deactivation dialog seeded from the currently shown account detail. */
    fun onDeactivateAccountClick() {
        val detail = _uiState.value.accountDetail ?: return
        _uiState.update {
            it.copy(
                deactivateAccount = DeactivateAccountForm(
                    accountId = detail.accountId,
                    accountNumber = detail.accountNumber,
                    circuitId = detail.circuitId,
                    branchLabel = "${detail.branchCode} (${detail.storeName})",
                ),
            )
        }
    }

    fun onDeactivateAccountFilePicked(file: PickedFile) {
        updateDeactivateForm { it.copy(proofFile = file, errorMessage = null) }
    }

    /** TODO: `POST /accounts/{id}/deactivate` once the backend contract is ready. */
    fun onDeactivateAccountConfirm() {
        updateDeactivateForm { it.copy(isSubmitting = true, errorMessage = null) }
    }

    fun onDeactivateAccountDismiss() {
        _uiState.update { it.copy(deactivateAccount = null) }
    }

    private fun updateDeactivateForm(transform: (DeactivateAccountForm) -> DeactivateAccountForm) {
        _uiState.update { state ->
            state.deactivateAccount?.let { state.copy(deactivateAccount = transform(it)) } ?: state
        }
    }

    // endregion

    // region Internals

    private fun updateRegisterBranchForm(transform: (RegisterBranchForm) -> RegisterBranchForm) {
        _uiState.update { state ->
            state.registerBranchForm?.let { state.copy(registerBranchForm = transform(it)) } ?: state
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

    private fun failLoad(message: String?) {
        _uiState.update {
            it.copy(isLoading = false, loadError = message ?: DEFAULT_LOAD_ERROR)
        }
    }

    /** The message from the first of [results] that did not succeed. */
    private fun firstErrorMessage(vararg results: Resource<*>): String? =
        results.firstNotNullOfOrNull { result ->
            when (result) {
                is Resource.Failed -> result.message
                is Resource.Error -> result.error?.message
                else -> null
            }
        }

    // endregion
}

private const val DEFAULT_LOAD_ERROR = "The registry could not be loaded."
