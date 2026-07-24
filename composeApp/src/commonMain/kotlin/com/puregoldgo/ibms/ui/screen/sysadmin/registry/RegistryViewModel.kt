package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.domain.usecase.BulkImportAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.CreateStoreUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import com.puregoldgo.ibms.ui.screen.sysadmin.activeRows
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the branch and ISP-account registries, and the bulk import
 * that fills them.
 *
 * The two panels share this one because they share a fetch — see
 * [RegistryUIState] for why the join makes them inseparable until the backend
 * models a store→provider relation. The staff directory, which is joined to
 * nothing, has a ViewModel of its own.
 *
 * Filtering lives in [RegistryUIState] rather than in the composables, so the
 * rules are testable and the previews can show a filtered list without running a
 * ViewModel.
 */
class RegistryViewModel(
    private val getProviders: GetProvidersUseCase,
    private val getStores: GetStoresUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val createStore: CreateStoreUseCase,
    private val bulkImportAccounts: BulkImportAccountsUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(RegistryUIState())
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<RegistryUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    /** The spreadsheet awaiting upload. Kept out of the state — see [RegistryUIState]. */
    private var pickedFile: PickedFile? = null

    init {
        loadPanel()
    }

    // region Public API

    /**
     * Fetches providers, branches and accounts and assembles both registries.
     *
     * All three concurrently, then awaited together: the rows are cross-joined —
     * accounts give each branch its ISPs, stores give each account its name — so
     * there is nothing worth drawing until every list is in. A [refresh] keeps
     * the current rows on screen while it runs; a first load has none to keep.
     */
    fun loadPanel(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = !refresh, isRefreshing = refresh, loadError = null)
            }

            val providers = async { getProviders().last() }
            val stores = async { getStores().last() }
            val accounts = async { getAccounts().last() }

            val providerResult = providers.await()
            val storeResult = stores.await()
            val accountResult = accounts.await()

            val providerList = providerResult.data
            val storeList = storeResult.data
            val accountList = accountResult.data

            if (providerList == null || storeList == null || accountList == null) {
                // Report the first thing that actually went wrong rather than a
                // summary — "invalid cursor" names the bug, "could not load"
                // does not.
                failLoad(firstErrorMessage(providerResult, storeResult, accountResult))
                return@launch
            }

            val storeNames = storeList.associate { it.id to it.name }
            val providerIds = providerIdsByStore(accountList)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    loadError = null,
                    activeProviders = providerList.activeRows(),
                    branches = storeList.map { store ->
                        store.toRow(providerIds[store.id].orEmpty())
                    },
                    accounts = accountList.map { account -> account.toRow(storeNames) },
                )
            }
        }
    }

    // endregion

    // region Branch filters

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

    // region Register branch

    /** Opens the register-branch dialog on a clean slate. */
    fun onRegisterBranchClick() {
        _uiState.update { it.copy(registerBranchForm = RegisterBranchForm()) }
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

    /** Submits the form to `POST /stores` and refreshes the registries on success. */
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
                        loadPanel(refresh = true)
                        _uiEvent.emit(RegistryUiEvent.ShowSnackbar("Branch registered."))
                    }
                    is Resource.Failed -> {
                        _uiState.update {
                            it.copy(registerBranchForm = form.copy(isSaving = false))
                        }
                        _uiEvent.emit(
                            RegistryUiEvent.ShowSnackbar(resource.message ?: "Could not register branch."),
                        )
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(registerBranchForm = form.copy(isSaving = false))
                        }
                        _uiEvent.emit(
                            RegistryUiEvent.ShowSnackbar(resource.error?.message ?: "Could not register branch."),
                        )
                    }
                }
            }
        }
    }

    fun onRegisterBranchDismiss() {
        _uiState.update { it.copy(registerBranchForm = null) }
    }

    private fun updateRegisterBranchForm(transform: (RegisterBranchForm) -> RegisterBranchForm) {
        _uiState.update { state ->
            state.registerBranchForm?.let { state.copy(registerBranchForm = transform(it)) } ?: state
        }
    }

    // endregion

    // region Account filters

    fun onAccountQueryChange(query: String) {
        _uiState.update { it.copy(accountQuery = query) }
    }

    fun onAccountProviderSelect(providerId: String?) {
        _uiState.update { it.copy(accountProviderId = providerId) }
    }

    // endregion

    // region Bulk import

    /** Opens the import dialog on a clean slate — no stale file, error or result. */
    fun onBulkUploadClick() {
        pickedFile = null
        _uiState.update {
            it.copy(
                isBulkImportOpen = true,
                bulkImportFileName = null,
                bulkImportFileSize = 0,
                isBulkImporting = false,
                bulkImportError = null,
                bulkImportSummary = null,
            )
        }
    }

    /**
     * Accepts a chosen file. The bytes stay here rather than in [RegistryUIState]
     * — see the note on its bulk-import fields.
     */
    fun onBulkImportFilePicked(file: PickedFile) {
        pickedFile = file
        _uiState.update {
            it.copy(
                bulkImportFileName = file.name,
                bulkImportFileSize = file.size,
                bulkImportError = null,
                bulkImportSummary = null,
            )
        }
    }

    /** Uploads the chosen file to `POST /accounts/bulk-import`. */
    fun onBulkImportStart() {
        val file = pickedFile ?: return
        if (!_uiState.value.canStartImport) return

        viewModelScope.launch {
            bulkImportAccounts(
                fileName = file.name,
                mimeType = file.mimeType,
                bytes = file.bytes,
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update {
                        it.copy(isBulkImporting = true, bulkImportError = null)
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isBulkImporting = false,
                                bulkImportSummary = resource.data?.toUiSummary(),
                            )
                        }
                        // The rows behind the dialog are now stale — the import
                        // is the whole reason new ones exist. Refreshing rather
                        // than reloading keeps them on screen meanwhile.
                        loadPanel(refresh = true)
                    }

                    // The backend's own wording — "missing required column 'Store
                    // Code' in header row" and friends name the cell to go fix.
                    // Replacing it with generic copy would throw that away.
                    is Resource.Failed -> failImport(resource.message)

                    is Resource.Error -> failImport(resource.error?.message)
                }
            }
        }
    }

    /** Closes the dialog and forgets everything about the attempt. */
    fun onBulkUploadDismiss() {
        pickedFile = null
        _uiState.update {
            it.copy(
                isBulkImportOpen = false,
                bulkImportFileName = null,
                bulkImportFileSize = 0,
                isBulkImporting = false,
                bulkImportError = null,
                bulkImportSummary = null,
            )
        }
    }

    // endregion

    // region Internals

    private fun failImport(message: String?) {
        _uiState.update {
            it.copy(
                isBulkImporting = false,
                bulkImportError = message ?: "The import could not be completed.",
            )
        }
    }

    private fun failLoad(message: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                loadError = message ?: "The registries could not be loaded.",
            )
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

/** Narrows the API summary to what the dialog draws. */
private fun BulkImportSummaryResponse.toUiSummary() = BulkImportSummary(
    providers = providers.map {
        ProviderImportSummary(
            name = it.name,
            created = it.created,
            accountsCreated = it.accountsCreated,
            accountsReused = it.accountsReused,
        )
    },
    storesCreated = storesCreated,
    storesReused = storesReused,
    accountsCreated = accountsCreated,
    accountsReused = accountsReused,
    rowsSkipped = rowsSkipped,
    skipReasons = skipReasons,
    totalRows = totalRows,
)
