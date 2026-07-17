package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.Resource
import com.puregoldgo.ibms.shared.model.Provider
import kotlinx.coroutines.flow.Flow

/**
 * Fetches the full list of providers.
 */
class GetProvidersUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(): Flow<Resource<List<Provider>>> = repository.getProviders()
}
