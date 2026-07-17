package com.puregoldgo.ibms.ui.screen.auth

/**
 * Immutable UI state for the Login screen.
 */
data class LoginUIState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
)
