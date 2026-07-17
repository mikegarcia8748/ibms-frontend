package com.puregoldgo.ibms.presentation.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.usecase.CreateProviderUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateProviderUseCase
import com.puregoldgo.ibms.shared.model.Provider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the Provider create/edit form.
 */
class ProviderFormViewModel(
    private val createProviderUseCase: CreateProviderUseCase,
    private val updateProviderUseCase: UpdateProviderUseCase,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(ProviderFormUIState())
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<ProviderFormUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    // region Public API

    /**
     * Initializes the form for editing an existing provider.
     */
    fun initWithProvider(provider: Provider) {
        _uiState.update {
            it.copy(
                providerId = provider.id,
                name = provider.name,
                code = provider.code,
                contactEmail = provider.contactEmail ?: "",
                contactPhone = provider.contactPhone ?: "",
                isEditMode = true,
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value, codeError = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(contactEmail = value, emailError = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(contactPhone = value) }
    }

    fun onSave() {
        val state = _uiState.value

        // Client-side validation
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Provider name is required") }
            hasError = true
        }
        if (state.code.isBlank()) {
            _uiState.update { it.copy(codeError = "Provider code is required") }
            hasError = true
        }
        if (hasError) {
            viewModelScope.launch {
                _uiEvent.emit(ProviderFormUiEvent.ShowValidationError("Please fix the errors above"))
            }
            return
        }

        val provider = Provider(
            id = state.providerId ?: "",
            name = state.name.trim(),
            code = state.code.trim(),
            contactEmail = state.contactEmail.trim().ifBlank { null },
            contactPhone = state.contactPhone.trim().ifBlank { null },
        )

        viewModelScope.launch {
            val useCaseFlow = if (state.isEditMode) {
                updateProviderUseCase(provider)
            } else {
                createProviderUseCase(provider)
            }

            useCaseFlow.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isSaving = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isSaving = false) }
                        _uiEvent.emit(ProviderFormUiEvent.SaveSuccess)
                    }
                    is Resource.Failed -> {
                        _uiState.update { it.copy(isSaving = false) }
                        _uiEvent.emit(ProviderFormUiEvent.ShowError(resource.message ?: "An unexpected error occurred"))
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isSaving = false) }
                        _uiEvent.emit(
                            ProviderFormUiEvent.ShowError(
                                resource.error?.message ?: "An unexpected error occurred",
                            ),
                        )
                    }
                }
            }
        }
    }

    // endregion
}
