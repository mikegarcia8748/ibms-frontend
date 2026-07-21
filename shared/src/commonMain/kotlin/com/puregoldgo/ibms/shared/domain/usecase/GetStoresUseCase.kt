package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import kotlinx.coroutines.flow.Flow

/**
 * Fetches every store, walking the endpoint's pages to exhaustion.
 *
 * Deliberately unfiltered by default. The control panel lists closed branches
 * too — greyed out, but present — because a branch disappearing from the list is
 * indistinguishable from one that was never imported.
 */
class GetStoresUseCase(
    private val repository: StoreRepository,
) {
    operator fun invoke(status: StoreStatus? = null): Flow<Resource<List<Store>>> =
        loadAllPages { cursor ->
            repository.listStores(status = status, cursor = cursor, limit = PAGE_SIZE)
        }
}
