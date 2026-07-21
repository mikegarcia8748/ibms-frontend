package com.puregoldgo.ibms.ui.screen.provider

/**
 * Bundles all UI→ViewModel action lambdas consumed by [ProviderFormContent] so
 * the composable receives a single callback parameter instead of many
 * individual `() -> Unit` / `(T) -> Unit` handlers.
 */
class ProviderFormCallback(
    val onNameChange: (String) -> Unit,
    val onPaymentScheduleDayChange: (String) -> Unit,
    val onSave: () -> Unit,
)
