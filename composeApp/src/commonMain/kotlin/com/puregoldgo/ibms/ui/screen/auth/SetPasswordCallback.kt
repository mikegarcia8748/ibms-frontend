package com.puregoldgo.ibms.ui.screen.auth

/**
 * Bundles all UI→ViewModel action lambdas consumed by `SetPasswordContent` so the
 * composable receives a single callback parameter instead of many individual
 * handlers.
 */
class SetPasswordCallback(
    val onNewPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onToggleNewPasswordVisibility: () -> Unit,
    val onToggleConfirmPasswordVisibility: () -> Unit,
    val onSubmit: () -> Unit,
    val onCancel: () -> Unit,
)
