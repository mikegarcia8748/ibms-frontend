package com.puregoldgo.ibms.presentation.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the Provider list screen.
 */
class ProviderListViewModel(
    private val getProvidersUseCase: GetProvidersUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(ProviderListUIState())
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<ProviderListUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    init {
        loadProviders()
    }

    // region Public API

    fun loadProviders() {
        viewModelScope.launch {
            getProvidersUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                providers = resource.data ?: emptyList(),
                                isEmpty = resource.data?.isEmpty() ?: true,
                                errorMessage = null,
                            )
                        }
                    }
                    is Resource.Failed -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = resource.message)
                        }
                        _uiEvent.emit(ProviderListUiEvent.ShowSnackbar(resource.message ?: "An unexpected error occurred"))
                    }
                    is Resource.Error -> {
                        val msg = resource.error?.message ?: "An unexpected error occurred"
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = msg)
                        }
                        _uiEvent.emit(ProviderListUiEvent.ShowSnackbar(msg))
                    }
                }
            }
        }
    }

    fun onAddClick() {
        viewModelScope.launch {
            _uiEvent.emit(ProviderListUiEvent.NavigateToForm(providerId = null))
        }
    }

    fun onEditClick(providerId: String) {
        viewModelScope.launch {
            _uiEvent.emit(ProviderListUiEvent.NavigateToForm(providerId = providerId))
        }
    }

    // endregion
}
