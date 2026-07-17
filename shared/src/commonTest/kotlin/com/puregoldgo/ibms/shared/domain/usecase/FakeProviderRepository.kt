package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.Resource
import com.puregoldgo.ibms.shared.model.Provider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of [ProviderRepository] for unit testing.
 */
class FakeProviderRepository : ProviderRepository {

    var providers: List<Provider> = emptyList()
    var shouldFail = false
    var failureMessage = "Test error"
    var lastCreatedProvider: Provider? = null
    var lastUpdatedProvider: Provider? = null

    override fun getProviders(): Flow<Resource<List<Provider>>> {
        return if (shouldFail) {
            flowOf(Resource.Failed(failureMessage))
        } else {
            flowOf(Resource.Success(providers))
        }
    }

    override fun createProvider(provider: Provider): Flow<Resource<Provider>> {
        return if (shouldFail) {
            flowOf(Resource.Failed(failureMessage))
        } else {
            lastCreatedProvider = provider
            flowOf(Resource.Success(provider.copy(id = "new-id")))
        }
    }

    override fun updateProvider(provider: Provider): Flow<Resource<Provider>> {
        return if (shouldFail) {
            flowOf(Resource.Failed(failureMessage))
        } else {
            lastUpdatedProvider = provider
            flowOf(Resource.Success(provider))
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
