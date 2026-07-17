package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.ibms.shared.model.Provider
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Provider operations.
 * Implementation lives in :composeApp (data layer).
 */
interface ProviderRepository {
    fun getProviders(): Flow<Resource<List<Provider>>>
    fun createProvider(provider: Provider): Flow<Resource<Provider>>
    fun updateProvider(provider: Provider): Flow<Resource<Provider>>
}
