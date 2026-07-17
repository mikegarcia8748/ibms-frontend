package com.puregoldgo.ibms.presentation.provider

/**
 * Immutable UI state for the Provider create/edit form.
 */
data class ProviderFormUIState(
    val providerId: String? = null,
    val name: String = "",
    val code: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val nameError: String? = null,
    val codeError: String? = null,
    val emailError: String? = null,
)
