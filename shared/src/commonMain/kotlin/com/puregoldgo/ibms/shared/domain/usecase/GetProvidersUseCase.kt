package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.model.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fetches the full list of providers.
 */
class GetProvidersUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(): Flow<Resource<List<Provider>>> = flow {
        when (val result = repository.getProviders()) {
            is Resource.Loading -> emit(Resource.Loading)
            is Resource.Success -> emit(Resource.Success(result.data?.data ?: emptyList()))
            is Resource.Failed -> emit(Resource.Failed(message = result.message ?: result.data?.message))
            is Resource.Error -> emit(Resource.Error(result.error))
        }
    }
}
