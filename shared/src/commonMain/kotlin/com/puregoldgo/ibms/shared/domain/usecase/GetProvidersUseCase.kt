package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import kotlinx.coroutines.flow.Flow

/**
 * Fetches every provider, walking the endpoint's pages to exhaustion.
 *
 * Leaves [status] unset by default: callers that show active and inactive ISPs
 * side by side need both, and filtering a list this small is cheaper than a
 * second round trip.
 */
class GetProvidersUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(status: ProviderStatus? = null): Flow<Resource<List<Provider>>> =
        loadAllPages { cursor ->
            repository.listProviders(status = status, cursor = cursor, limit = PAGE_SIZE)
        }
}
