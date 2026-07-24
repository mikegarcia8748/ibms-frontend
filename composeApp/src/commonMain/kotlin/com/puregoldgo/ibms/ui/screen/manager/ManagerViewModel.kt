package com.puregoldgo.ibms.ui.screen.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.domain.usecase.CreateStoreUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.ui.screen.store.RegisterBranchForm
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.toRow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the oversight console.
 *
 * Branches are live from `GET /stores` because managers can create new stores.
 * TopSheets and activities are still sample-fed this pass; the wiring pass will
 * replace those with real fetches.
 */
class ManagerViewModel(
    private val authRepository: AuthRepository,
    private val getStores: GetStoresUseCase,
    private val createStore: CreateStoreUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        ManagerUIState(
            userName = CurrentUserStore.name.orEmpty(),
            userRole = CurrentUserStore.role.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<ManagerUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    init {
        loadPanel()
    }

    // region Public API

    /** Seeds the console. Branches come from the API; topsheets and activities are sample data for now. */
    fun loadPanel(refresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = !refresh, loadError = null)
            }

            val storeResult = getStores().last()
            val storeList = storeResult.data

            if (storeList == null) {
                failLoad(
                    when (storeResult) {
                        is Resource.Failed -> storeResult.message
                        is Resource.Error -> storeResult.error?.message
                        else -> null
                    },
                )
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadError = null,
                    topSheets = ManagerSampleData.topSheets,
                    activities = ManagerSampleData.activities,
                    branches = storeList.map { store -> store.toRow(emptySet()) },
                )
            }
        }
    }

    private fun failLoad(message: String?) {
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = message ?: "The branches could not be loaded.",
            )
        }
    }

    fun onTabSelect(tab: ManagerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onTopSheetQueryChange(query: String) {
        _uiState.update { it.copy(topSheetQuery = query) }
    }

    fun onActivityQueryChange(query: String) {
        _uiState.update { it.copy(activityQuery = query) }
    }

    // endregion

    // region Branch filters

    fun onBranchQueryChange(query: String) {
        _uiState.update { it.copy(branchQuery = query) }
    }

    fun onBranchLetterSelect(letter: Char) {
        _uiState.update { it.copy(branchLetter = letter) }
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

    /** Submits the form to `POST /stores` and refreshes the branch list on success. */
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
                        _uiEvent.emit(ManagerUiEvent.ShowSnackbar("Branch registered."))
                    }
                    is Resource.Failed -> {
                        _uiState.update {
                            it.copy(registerBranchForm = form.copy(isSaving = false))
                        }
                        _uiEvent.emit(
                            ManagerUiEvent.ShowSnackbar(resource.message ?: "Could not register branch."),
                        )
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(registerBranchForm = form.copy(isSaving = false))
                        }
                        _uiEvent.emit(
                            ManagerUiEvent.ShowSnackbar(resource.error?.message ?: "Could not register branch."),
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

    /** Drops the stored session. There is no logout endpoint to call. */
    fun onLogout() {
        authRepository.signOut()
        viewModelScope.launch {
            _uiEvent.emit(ManagerUiEvent.NavigateToLogin)
        }
    }
}
