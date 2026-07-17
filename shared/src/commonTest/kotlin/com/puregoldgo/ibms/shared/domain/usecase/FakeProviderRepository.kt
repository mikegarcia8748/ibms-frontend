package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.model.Provider

/**
 * Fake implementation of [ProviderRepository] for unit testing.
 */
class FakeProviderRepository : ProviderRepository {

    var providers: List<Provider> = emptyList()
    var shouldFail = false
    var failureMessage = "Test error"
    var lastCreatedProvider: Provider? = null
    var lastUpdatedProvider: Provider? = null

    override suspend fun getProviders(): Resource<BaseResponse<List<Provider>>> {
        return if (shouldFail) {
            Resource.Failed(message = failureMessage)
        } else {
            Resource.Success(BaseResponse(data = providers))
        }
    }

    override suspend fun createProvider(provider: Provider): Resource<BaseResponse<Provider>> {
        return if (shouldFail) {
            Resource.Failed(message = failureMessage)
        } else {
            lastCreatedProvider = provider
            Resource.Success(BaseResponse(data = provider.copy(id = "new-id")))
        }
    }

    override suspend fun updateProvider(provider: Provider): Resource<BaseResponse<Provider>> {
        return if (shouldFail) {
            Resource.Failed(message = failureMessage)
        } else {
            lastUpdatedProvider = provider
            Resource.Success(BaseResponse(data = provider))
        }
    }
}

object ProviderTestData {
    fun createProvider(
        id: String = "test-id",
        name: String = "Test Provider",
        code: String = "TP",
        contactEmail: String? = "test@example.com",
        contactPhone: String? = "+1234567890",
    ): Provider = Provider(
        id = id,
        name = name,
        code = code,
        contactEmail = contactEmail,
        contactPhone = contactPhone,
    )

    fun createProviderList(count: Int = 3): List<Provider> =
        (1..count).map { i ->
            createProvider(id = "id-$i", name = "Provider $i", code = "P$i")
        }
}
