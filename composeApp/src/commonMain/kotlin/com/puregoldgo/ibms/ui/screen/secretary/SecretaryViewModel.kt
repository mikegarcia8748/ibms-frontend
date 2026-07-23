package com.puregoldgo.ibms.ui.screen.secretary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetLinesUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetsUseCase
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.Store
import kotlinx.coroutines.async
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
 * Loads providers, stores, accounts and the compiled topsheets concurrently from
 * the API. Store and account detail modals are built on-click from the cached
 * domain lists (no round-trip); the topsheet detail modal fetches its account
 * lines lazily from `GET /topsheets/{id}/lines` when a row is opened.
 */
class SecretaryViewModel(
    private val getProviders: GetProvidersUseCase,
    private val getStores: GetStoresUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getTopSheets: GetTopSheetsUseCase,
    private val getTopSheetLines: GetTopSheetLinesUseCase,
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

    private val _uiEvent = MutableSharedFlow<SecretaryUiEvent>()
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

    // region Topsheet details

    /**
     * Opens the topsheet detail modal. The header is drawn from the already-loaded
     * row, so it shows instantly; the account lines are fetched from
     * `GET /topsheets/{id}/lines` behind a spinner inside the dialog.
     */
    fun onTopSheetClick(topSheetId: String) {
        val row = _uiState.value.topSheets.find { it.id == topSheetId } ?: return
        _uiState.update {
            it.copy(
                topSheetDetail = TopSheetDetail(header = row, isLoadingLines = true),
                // A fresh open starts from the default order, not the last one's.
                topSheetLineQuery = "",
                topSheetLineSort = TopSheetLineSortKey.RfpNumber,
                topSheetLineSortAsc = true,
            )
        }
        loadTopSheetLines(topSheetId)
    }

    private fun loadTopSheetLines(topSheetId: String) {
        viewModelScope.launch {
            getTopSheetLines(topSheetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> updateTopSheetDetail {
                        it.copy(isLoadingLines = true, linesError = null)
                    }

                    is Resource.Success -> {
                        val lines = buildTopSheetLineRows(resource.data.orEmpty())
                        updateTopSheetDetail {
                            it.copy(isLoadingLines = false, linesError = null, lines = lines)
                        }
                    }

                    is Resource.Failed -> updateTopSheetDetail {
                        it.copy(isLoadingLines = false, linesError = resource.message ?: DEFAULT_LINES_ERROR)
                    }

                    is Resource.Error -> updateTopSheetDetail {
                        it.copy(isLoadingLines = false, linesError = resource.error?.message ?: DEFAULT_LINES_ERROR)
                    }
                }
            }
        }
    }

    fun onTopSheetDetailDismiss() {
        _uiState.update { it.copy(topSheetDetail = null, topSheetLineQuery = "") }
    }

    fun onTopSheetLineQueryChange(query: String) {
        _uiState.update { it.copy(topSheetLineQuery = query) }
    }

    fun onTopSheetLineSortSelect(sort: TopSheetLineSortKey) {
        _uiState.update { it.copy(topSheetLineSort = sort) }
    }

    fun onTopSheetLineSortDirectionToggle() {
        _uiState.update { it.copy(topSheetLineSortAsc = !it.topSheetLineSortAsc) }
    }

    /** Applies [transform] to the open detail, ignoring a late emission after dismiss. */
    private fun updateTopSheetDetail(transform: (TopSheetDetail) -> TopSheetDetail) {
        _uiState.update { state ->
            state.topSheetDetail?.let { state.copy(topSheetDetail = transform(it)) } ?: state
        }
    }

    // endregion

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
private const val DEFAULT_LINES_ERROR = "The TopSheet's accounts could not be loaded."
