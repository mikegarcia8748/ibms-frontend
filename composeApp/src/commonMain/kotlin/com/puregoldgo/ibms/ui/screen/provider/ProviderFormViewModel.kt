package com.puregoldgo.ibms.ui.screen.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.usecase.CreateProviderUseCase
import com.puregoldgo.ibms.shared.domain.usecase.PAYMENT_SCHEDULE_DAYS
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
                paymentScheduleDay = provider.paymentScheduleDay.toString(),
                isEditMode = true,
            )
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    /**
     * Accepts digits only, capped at two. Filtering here rather than validating
     * on save means the field cannot hold a value the form would later have to
     * reject — a numeric keyboard still lets a paste through.
     */
    fun onPaymentScheduleDayChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(2)
        _uiState.update { it.copy(paymentScheduleDay = digits, paymentScheduleDayError = null) }
    }

    fun onSave() {
        val state = _uiState.value

        // Client-side validation
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Provider name is required") }
            hasError = true
        }
        val day = state.paymentScheduleDay.toIntOrNull()
        if (day == null || day !in PAYMENT_SCHEDULE_DAYS) {
            _uiState.update { it.copy(paymentScheduleDayError = "Enter a day between 1 and 31") }
            hasError = true
        }
        if (hasError || day == null) {
            viewModelScope.launch {
                _uiEvent.emit(ProviderFormUiEvent.ShowValidationError("Please fix the errors above"))
            }
            return
        }

        val name = state.name.trim()

        viewModelScope.launch {
            val useCaseFlow = if (state.isEditMode) {
                updateProviderUseCase(
                    id = state.providerId.orEmpty(),
                    name = name,
                    paymentScheduleDay = day,
                )
            } else {
                createProviderUseCase(name = name, paymentScheduleDay = day)
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
