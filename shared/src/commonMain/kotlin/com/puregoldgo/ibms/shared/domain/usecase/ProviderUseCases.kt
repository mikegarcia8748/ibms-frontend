package com.puregoldgo.ibms.shared.domain.usecase

/**
 * Aggregates all Provider-related UseCases for convenient injection.
 */
data class ProviderUseCases(
    val getProviders: GetProvidersUseCase,
    val createProvider: CreateProviderUseCase,
    val updateProvider: UpdateProviderUseCase,
)
