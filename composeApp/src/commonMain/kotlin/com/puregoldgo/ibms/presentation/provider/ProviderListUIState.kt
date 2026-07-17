package com.puregoldgo.ibms.presentation.provider

import com.puregoldgo.ibms.shared.model.Provider

/**
 * Immutable UI state for the Provider list screen.
 */
data class ProviderListUIState(
    val providers: List<Provider> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmpty: Boolean = false,
)
