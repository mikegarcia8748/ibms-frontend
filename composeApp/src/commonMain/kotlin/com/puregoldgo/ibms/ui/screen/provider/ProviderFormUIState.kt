package com.puregoldgo.ibms.ui.screen.provider

/**
 * Immutable UI state for the Provider create/edit form.
 *
 * [paymentScheduleDay] is a String, not an Int: a text field has to be able to
 * hold "" while someone clears it and retypes, and an Int field would force that
 * empty moment to become a 0 and fail validation for the wrong reason.
 */
data class ProviderFormUIState(
    val providerId: String? = null,
    val name: String = "",
    val paymentScheduleDay: String = "",
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val nameError: String? = null,
    val paymentScheduleDayError: String? = null,
)
