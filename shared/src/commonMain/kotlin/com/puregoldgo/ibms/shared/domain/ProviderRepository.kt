package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.model.Provider
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Provider operations.
 * Implementation lives in :composeApp (data layer).
 */
interface ProviderRepository {
    suspend fun getProviders(): Resource<BaseResponse<List<Provider>>>
    suspend fun createProvider(provider: Provider): Resource<BaseResponse<Provider>>
    suspend fun updateProvider(provider: Provider): Resource<BaseResponse<Provider>>
}
