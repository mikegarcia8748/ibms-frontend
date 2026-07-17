package com.puregoldgo.ibms.ui.screen.provider

import com.puregoldgo.ibms.shared.model.Provider

/**
 * Bundles all UI→ViewModel action lambdas consumed by [ProviderListContent] so
 * the composable receives a single callback parameter instead of many
 * individual `() -> Unit` / `(T) -> Unit` handlers.
 */
class ProviderListCallback(
    val onProviderClick: (Provider) -> Unit,
    val onRetry: () -> Unit,
)
