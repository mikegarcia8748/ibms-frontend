package com.puregoldgo.ibms.ui.screen.secretary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.domain.usecase.CreateStoreUseCase
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the secretary console.
 *
 * **Renders sample data.** The screen is a UI pass: no repositories, no HTTP.
 * Filtering already lives in [SecretaryUIState] rather than in the composables,
 * so the rules are testable now and the previews can show a filtered list
 * without running a ViewModel — and swapping [SecretarySampleData] for the API
 * moves nothing in the composables.
 *
 * TODO: the wiring pass replaces [loadPanel] with concurrent calls to
 *  `GET /stores`, `GET /accounts`, `GET /providers` and `GET /topsheets`,
 *  awaited together the way `SysadminViewModel.loadPanel` does — the rows are
 *  cross-joined, so a panel with only some lists in would draw rows that are
 *  quietly wrong. The two submits then call `POST /stores` and `POST /accounts`,
 *  and export calls `GET /exports/topsheet/{id}.xlsx`.
 */
class SecretaryViewModel(
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

    private val _uiEvent = MutableSharedFlow<SecretaryUiEvent>()
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

    /** TODO: `POST /accounts`. Inert until then — see the dialog's own banner. */
    fun onAddAccountSubmit() = Unit

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

    // endregion
}
