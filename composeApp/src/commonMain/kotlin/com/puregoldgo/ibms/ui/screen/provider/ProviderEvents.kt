package com.puregoldgo.ibms.ui.screen.provider

/**
 * One-time UI events emitted by [ProviderListViewModel].
 */
sealed class ProviderListUiEvent {
    data class NavigateToForm(val providerId: String?) : ProviderListUiEvent()
    data class ShowSnackbar(val message: String) : ProviderListUiEvent()
}

/**
 * One-time UI events emitted by [ProviderFormViewModel].
 */
sealed class ProviderFormUiEvent {
    data object SaveSuccess : ProviderFormUiEvent()
    data class ShowValidationError(val message: String) : ProviderFormUiEvent()
    data class ShowError(val message: String) : ProviderFormUiEvent()
}
