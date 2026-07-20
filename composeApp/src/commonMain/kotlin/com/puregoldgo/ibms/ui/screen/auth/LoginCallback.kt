package com.puregoldgo.ibms.ui.screen.auth

/**
 * Bundles all UI→ViewModel action lambdas consumed by [LoginContent] so the
 * composable receives a single callback parameter instead of many individual
 * `() -> Unit` / `(T) -> Unit` handlers.
 */
class LoginCallback(
    val onUsernameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onTogglePasswordVisibility: () -> Unit,
    val onLoginClick: () -> Unit,
)
