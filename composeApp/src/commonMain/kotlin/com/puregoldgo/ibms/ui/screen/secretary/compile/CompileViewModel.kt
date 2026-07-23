package com.puregoldgo.ibms.ui.screen.secretary.compile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.BillingPeriod
import com.puregoldgo.ibms.ui.component.LETTER_ALL
import com.puregoldgo.ibms.shared.domain.usecase.AssignRfpUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ConfirmTopSheetUseCase
import com.puregoldgo.ibms.shared.domain.usecase.CreateTopSheetDraftUseCase
import com.puregoldgo.ibms.shared.domain.usecase.DeleteTopSheetLineUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetLinesUseCase
import com.puregoldgo.ibms.shared.domain.usecase.PreviewTopSheetUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateTopSheetLineUseCase
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.ui.screen.secretary.SecretaryProviderRow
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the Compile TopSheet panel.
 *
 * Drives the two-phase flow: browse/preview → create draft → per-line RFP entry →
 * confirm. The browse view (no provider selected) joins the full accounts DB with
 * stores and providers; selecting an ISP switches to that provider's eligible-
 * account preview. Concurrent loads are awaited together the way
 * `DirectoryViewModel.loadPanel` does — a half-loaded panel would draw rows that
 * are quietly wrong.
 */
class CompileViewModel(
    private val getProviders: GetProvidersUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getStores: GetStoresUseCase,
    private val preview: PreviewTopSheetUseCase,
    private val createDraft: CreateTopSheetDraftUseCase,
    private val getLines: GetTopSheetLinesUseCase,
    private val updateLine: UpdateTopSheetLineUseCase,
    private val assignRfp: AssignRfpUseCase,
    private val deleteLine: DeleteTopSheetLineUseCase,
    private val confirm: ConfirmTopSheetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompileUIState(billingPeriod = BillingPeriod.current()))
    val uiState = _uiState.asStateFlow()

    init {
        loadContext()
    }

    // region Review

    /**
     * Loads the accounts-for-review list for the current provider/period.
     *
     * No provider selected ⇒ browse: the whole accounts DB joined to stores and
     * providers, totalled client-side. A provider selected ⇒ that ISP's eligible
     * accounts and server-computed total via preview. Providers are fetched either
     * way — the dropdown and the per-row billing day need them.
     */
    fun loadContext() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null) }

            val providerId = _uiState.value.selectedProviderId
            val period = _uiState.value.billingPeriod
            val providersDeferred = async { getProviders(ProviderStatus.ACTIVE).last() }

            if (providerId == null) {
                val accountsDeferred = async { getAccounts().last() }
                val storesDeferred = async { getStores().last() }

                val providersResult = providersDeferred.await()
                val accountsResult = accountsDeferred.await()
                val storesResult = storesDeferred.await()

                val providers = providersResult.data
                val accounts = accountsResult.data
                val stores = storesResult.data
                if (providers == null || accounts == null || stores == null) {
                    failLoad(firstErrorMessage(providersResult, accountsResult, storesResult))
                    return@launch
                }

                val rows = buildBrowseRows(accounts, stores, providers)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = null,
                        providers = providers.toRows(),
                        reviewRows = rows,
                        totalAmount = sumMoney(rows.map { row -> row.amount }),
                    )
                }
            } else {
                val previewDeferred = async { preview(providerId, period).last() }

                val providersResult = providersDeferred.await()
                val previewResult = previewDeferred.await()

                val providers = providersResult.data
                val previewData = previewResult.data
                if (providers == null || previewData == null) {
                    failLoad(firstErrorMessage(providersResult, previewResult))
                    return@launch
                }

                val provider = providers.firstOrNull { it.id == providerId }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = null,
                        providers = providers.toRows(),
                        reviewRows = buildPreviewRows(previewData, provider),
                        totalAmount = previewData.totalAmount,
                    )
                }
            }
        }
    }

    fun onProviderSelect(providerId: String?) {
        _uiState.update {
            it.copy(selectedProviderId = providerId, letter = LETTER_ALL, query = "")
        }
        loadContext()
    }

    fun onPreviousMonth() {
        _uiState.update { it.copy(billingPeriod = BillingPeriod.previous(it.billingPeriod)) }
        loadContext()
    }

    fun onNextMonth() {
        val next = BillingPeriod.next(_uiState.value.billingPeriod)
        if (BillingPeriod.isFuture(next)) return
        _uiState.update { it.copy(billingPeriod = next) }
        loadContext()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onLetterSelect(letter: Char) {
        _uiState.update { it.copy(letter = letter) }
    }

    // endregion

    // region Compile → draft

    /** Creates the DRAFT and moves to RFP entry, then loads its lines. */
    fun onCompileClick() {
        val providerId = _uiState.value.selectedProviderId ?: return
        val period = _uiState.value.billingPeriod
        if (!_uiState.value.canCompile) return

        viewModelScope.launch {
            createDraft(providerId, period).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isCompiling = true, compileError = null)
                    }

                    is Resource.Success -> {
                        val draft = resource.data
                        if (draft == null) {
                            _uiState.update {
                                it.copy(isCompiling = false, compileError = "The draft could not be created.")
                            }
                            return@collect
                        }
                        _uiState.update {
                            it.copy(
                                isCompiling = false,
                                compileError = null,
                                draft = draft,
                                phase = CompilePhase.RfpEntry,
                            )
                        }
                        loadLines(draft.id)
                    }

                    is Resource.Failed -> _uiState.update {
                        it.copy(isCompiling = false, compileError = resource.message ?: DEFAULT_COMPILE_ERROR)
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(isCompiling = false, compileError = resource.error?.message ?: DEFAULT_COMPILE_ERROR)
                    }
                }
            }
        }
    }

    // endregion

    // region RFP entry

    private fun loadLines(topsheetId: String) {
        viewModelScope.launch {
            getLines(topsheetId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isLoadingLines = true, linesError = null)
                    }

                    is Resource.Success -> {
                        val lines = resource.data.orEmpty()
                            .map { line -> line.toLineRow() }
                            .sortedBy { line -> line.rfpSortOrder ?: Int.MAX_VALUE }
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

    /** RFP numbers are numeric-only (the backend rejects anything else). */
    fun onRfpChange(lineId: String, value: String) {
        val digits = value.filter { it.isDigit() }
        updateLineState(lineId) { it.copy(rfpInput = digits, rowError = null) }
    }

    /** Persists a line's RFP number when its field commits (loses focus / done). */
    fun onRfpCommit(lineId: String) {
        val line = _uiState.value.lines.firstOrNull { it.id == lineId } ?: return
        val draftId = _uiState.value.draft?.id ?: return
        if (!line.hasUnsavedRfp || line.rfpInput.isBlank() || line.isSavingRfp) return

        viewModelScope.launch {
            updateLine(draftId, lineId, rfpNumber = line.rfpInput).collect { resource ->
                when (resource) {
                    is Resource.Loading -> updateLineState(lineId) {
                        it.copy(isSavingRfp = true, rowError = null)
                    }

                    is Resource.Success -> {
                        val updated = resource.data
                        updateLineState(lineId) {
                            it.copy(
                                isSavingRfp = false,
                                rowError = null,
                                savedRfpNumber = updated?.rfpNumber ?: it.rfpInput,
                                rfpInput = updated?.rfpNumber ?: it.rfpInput,
                                proratedAmount = updated?.proratedAmount ?: it.proratedAmount,
                            )
                        }
                    }

                    is Resource.Failed -> updateLineState(lineId) {
                        it.copy(isSavingRfp = false, rowError = resource.message ?: DEFAULT_LINE_SAVE_ERROR)
                    }

                    is Resource.Error -> updateLineState(lineId) {
                        it.copy(isSavingRfp = false, rowError = resource.error?.message ?: DEFAULT_LINE_SAVE_ERROR)
                    }
                }
            }
        }
    }

    /** RFP range ends are numeric-only, same as the per-line field. */
    fun onRfpRangeStartChange(value: String) {
        _uiState.update { it.copy(rfpRangeStart = value.filter { c -> c.isDigit() }, assignRfpError = null) }
    }

    fun onRfpRangeEndChange(value: String) {
        _uiState.update { it.copy(rfpRangeEnd = value.filter { c -> c.isDigit() }, assignRfpError = null) }
    }

    /**
     * Bulk-numbers the draft from the entered range: the backend gives one number
     * per distinct store code and returns the lines re-sorted, which replace the
     * current list (seeding each edit buffer with its saved number) so `canConfirm`
     * flips true once the range covers every store.
     */
    fun onAssignRfpClick() {
        val draftId = _uiState.value.draft?.id ?: return
        val state = _uiState.value
        if (!state.canAssignRfp) return

        viewModelScope.launch {
            assignRfp(draftId, state.rfpRangeStart, state.rfpRangeEnd).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isAssigningRfp = true, assignRfpError = null)
                    }

                    is Resource.Success -> {
                        val lines = resource.data.orEmpty()
                            .map { line -> line.toLineRow() }
                            .sortedBy { line -> line.rfpSortOrder ?: Int.MAX_VALUE }
                        _uiState.update {
                            it.copy(
                                isAssigningRfp = false,
                                assignRfpError = null,
                                rfpRangeStart = "",
                                rfpRangeEnd = "",
                                lines = lines,
                            )
                        }
                    }

                    is Resource.Failed -> _uiState.update {
                        it.copy(isAssigningRfp = false, assignRfpError = resource.message ?: DEFAULT_ASSIGN_RFP_ERROR)
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(isAssigningRfp = false, assignRfpError = resource.error?.message ?: DEFAULT_ASSIGN_RFP_ERROR)
                    }
                }
            }
        }
    }

    /** Removes a line from the draft, then reloads so ordering and totals stay the server's. */
    fun onRemoveLine(lineId: String) {
        val draftId = _uiState.value.draft?.id ?: return
        if (_uiState.value.lines.size <= 1) {
            _uiState.update { it.copy(linesError = "The last line cannot be removed; discard the draft instead.") }
            return
        }

        viewModelScope.launch {
            deleteLine(draftId, lineId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> updateLineState(lineId) {
                        it.copy(isRemoving = true, rowError = null)
                    }

                    is Resource.Success -> loadLines(draftId)

                    is Resource.Failed -> updateLineState(lineId) {
                        it.copy(isRemoving = false, rowError = resource.message ?: DEFAULT_LINE_REMOVE_ERROR)
                    }

                    is Resource.Error -> updateLineState(lineId) {
                        it.copy(isRemoving = false, rowError = resource.error?.message ?: DEFAULT_LINE_REMOVE_ERROR)
                    }
                }
            }
        }
    }

    fun onRetryLines() {
        _uiState.value.draft?.id?.let { loadLines(it) }
    }

    fun onLinesQueryChange(query: String) {
        _uiState.update { it.copy(linesQuery = query) }
    }

    fun onLinesLetterSelect(letter: Char) {
        _uiState.update { it.copy(linesLetter = letter) }
    }

    fun onLinesSortSelect(order: LineSortOrder) {
        _uiState.update { it.copy(linesSort = order) }
    }

    /** Returns to review, keeping the period/provider. The draft persists server-side. */
    fun onBackToReview() {
        _uiState.update {
            it.copy(
                phase = CompilePhase.Review,
                draft = null,
                lines = emptyList(),
                linesError = null,
                compileError = null,
                rfpRangeStart = "",
                rfpRangeEnd = "",
                assignRfpError = null,
                linesQuery = "",
                linesLetter = LETTER_ALL,
                linesSort = LineSortOrder.StoreCode,
            )
        }
    }

    /** Re-validates, mints the invoice, and shows the compiled result. */
    fun onConfirmClick() {
        val draftId = _uiState.value.draft?.id ?: return
        if (!_uiState.value.canConfirm) return

        viewModelScope.launch {
            confirm(draftId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isConfirming = true, confirmError = null)
                    }

                    is Resource.Success -> {
                        val compiled = resource.data
                        if (compiled == null) {
                            _uiState.update {
                                it.copy(isConfirming = false, confirmError = "The topsheet could not be confirmed.")
                            }
                            return@collect
                        }
                        _uiState.update {
                            it.copy(isConfirming = false, confirmError = null, compiled = compiled, phase = CompilePhase.Compiled)
                        }
                    }

                    is Resource.Failed -> _uiState.update {
                        it.copy(isConfirming = false, confirmError = resource.message ?: DEFAULT_CONFIRM_ERROR)
                    }

                    is Resource.Error -> _uiState.update {
                        it.copy(isConfirming = false, confirmError = resource.error?.message ?: DEFAULT_CONFIRM_ERROR)
                    }
                }
            }
        }
    }

    // endregion

    // region Result

    /** Resets to a clean review and reloads — the just-billed accounts are no longer eligible. */
    fun onStartNew() {
        _uiState.update {
            it.copy(
                phase = CompilePhase.Review,
                draft = null,
                lines = emptyList(),
                compiled = null,
                compileError = null,
                confirmError = null,
                linesError = null,
                rfpRangeStart = "",
                rfpRangeEnd = "",
                assignRfpError = null,
                linesQuery = "",
                linesLetter = LETTER_ALL,
                linesSort = LineSortOrder.StoreCode,
            )
        }
        loadContext()
    }

    // endregion

    // region Internals

    private fun updateLineState(lineId: String, transform: (CompileLineRow) -> CompileLineRow) {
        _uiState.update { state ->
            state.copy(lines = state.lines.map { if (it.id == lineId) transform(it) else it })
        }
    }

    private fun List<Provider>.toRows(): List<SecretaryProviderRow> =
        map { SecretaryProviderRow(id = it.id, name = it.name, isActive = it.status == ProviderStatus.ACTIVE) }

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

private const val DEFAULT_LOAD_ERROR = "The accounts could not be loaded."
private const val DEFAULT_COMPILE_ERROR = "The draft could not be created."
private const val DEFAULT_LINES_ERROR = "The draft lines could not be loaded."
private const val DEFAULT_LINE_SAVE_ERROR = "The RFP number could not be saved."
private const val DEFAULT_ASSIGN_RFP_ERROR = "The RFP numbers could not be assigned."
private const val DEFAULT_LINE_REMOVE_ERROR = "The line could not be removed."
private const val DEFAULT_CONFIRM_ERROR = "The topsheet could not be confirmed."
