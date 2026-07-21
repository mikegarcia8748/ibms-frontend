package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Store operations.
 * Implementation lives in :composeApp (data layer).
 */
interface StoreRepository {

    /**
     * One page of `GET /stores`.
     *
     * [query] is the endpoint's own `q` search. The control panel does not use
     * it — its search runs client-side alongside the A–Z rail and the ISP
     * filter, which have to compose — but it is on the interface because paging
     * the whole table is only viable while the table is small.
     */
    fun listStores(
        status: StoreStatus? = null,
        query: String? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): Flow<Resource<BaseResponse<CursorPage<Store>>>>
}
