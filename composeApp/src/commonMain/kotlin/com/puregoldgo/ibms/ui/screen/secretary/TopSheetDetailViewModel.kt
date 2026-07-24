package com.puregoldgo.ibms.ui.screen.secretary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetLinesUseCase
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.Store
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the full-screen topsheet detail view.
 *
 * Reads its header from [TopSheetRowHolder] (set by the secretary screen before
 * navigating) and fetches the account lines from `GET /topsheets/{id}/lines`.
 * Also loads accounts, stores and providers once so tapping a line can open
 * [AccountDetailsDialog] without an extra round-trip.
 */
class TopSheetDetailViewModel(
    private val getTopSheetLines: GetTopSheetLinesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getStores: GetStoresUseCase,
    private val getProviders: GetProvidersUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        TopSheetDetailUIState(header = TopSheetRowHolder.current),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    private val topSheetId: String? = TopSheetRowHolder.current?.id
    private var cachedAccounts: List<Account> = emptyList()
    private var cachedStores: List<Store> = emptyList()
    private var cachedProviders: List<Provider> = emptyList()

    init {
        if (topSheetId != null) {
            loadLines(topSheetId)
            loadReferenceData()
        }
    }

    // region Lines

    fun loadLines(id: String = topSheetId.toString()) {
        viewModelScope.launch {
            getTopSheetLines(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoadingLines = true, linesError = null)
                    }

                    is Resource.Success -> {
                        val lines = buildTopSheetLineRows(resource.data.orEmpty())
                        _uiState.update {
                            it.copy(isLoadingLines = false, linesError = null, lines = lines)
                        }
                    }

                    is Resource.Failed -> _uiState.update {
                        it.copy(isLoadingLines = false, linesError = resource.message ?: DEFAULT_LINES_ERROR)
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(isLoadingLines = false, linesError = resource.error?.message ?: DEFAULT_LINES_ERROR)
                    }
                }
            }
        }
    }

    // endregion

    // region Search / sort

    fun onLineQueryChange(query: String) {
        _uiState.update { it.copy(lineQuery = query) }
    }

    fun onLineSortSelect(sort: TopSheetLineSortKey) {
        _uiState.update { it.copy(lineSort = sort) }
    }

    fun onLineSortDirectionToggle() {
        _uiState.update { it.copy(lineSortAsc = !it.lineSortAsc) }
    }

    // endregion

    // region Account detail

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

    // region Reference data

    private fun loadReferenceData() {
        viewModelScope.launch {
            val accountsDeferred = async { getAccounts().last() }
            val storesDeferred = async { getStores().last() }
            val providersDeferred = async { getProviders().last() }

            cachedAccounts = accountsDeferred.await().data.orEmpty()
            cachedStores = storesDeferred.await().data.orEmpty()
            cachedProviders = providersDeferred.await().data.orEmpty()
        }
    }

    // endregion

    companion object {
        private const val DEFAULT_LINES_ERROR = "The TopSheet's accounts could not be loaded."
    }
}
