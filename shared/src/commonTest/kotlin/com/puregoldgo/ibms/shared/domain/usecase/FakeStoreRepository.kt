package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.CreateStoreRequest
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.StoreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [StoreRepository] for unit testing.
 *
 * Serves [stores] as pages of [pageSize] so tests exercise the cursor walk in
 * `loadAllPages` rather than a single canned page.
 */
class FakeStoreRepository : StoreRepository {

    var stores: List<Store> = emptyList()
    var shouldFail = false
    var failureMessage = "Test error"
    var pageSize = 100

    var lastCreated: CreateStoreRequest? = null

    /** Every cursor this fake was asked for, in order — including the initial null. */
    val requestedCursors = mutableListOf<String?>()

    override fun listStores(
        status: StoreStatus?,
        query: String?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Store>>>> = flow {
        requestedCursors += cursor
        emit(Resource.Loading)

        if (shouldFail) {
            emit(Resource.Failed(message = failureMessage))
            return@flow
        }

        val filtered = status?.let { wanted -> stores.filter { it.status == wanted } } ?: stores
        val from = cursor?.toIntOrNull() ?: 0
        val size = minOf(limit ?: pageSize, pageSize)
        val slice = filtered.drop(from).take(size)
        val next = (from + size).takeIf { it < filtered.size }?.toString()

        emit(Resource.Success(BaseResponse(data = CursorPage(items = slice, nextCursor = next))))
    }

    override suspend fun createStore(request: CreateStoreRequest): Resource<BaseResponse<Store>> {
        return if (shouldFail) {
            Resource.Failed(message = failureMessage)
        } else {
            lastCreated = request
            Resource.Success(
                BaseResponse(
                    data = StoreTestData.createStore(
                        id = "new-id",
                        storeType = request.storeType,
                        branchCode = request.branchCode,
                        name = request.name,
                        region = request.region,
                        province = request.province,
                        city = request.city,
                        barangay = request.barangay,
                        postal = request.postal,
                    ),
                ),
            )
        }
    }
}

object StoreTestData {
    fun createStore(
        id: String = "test-id",
        storeType: StoreType = StoreType.PUREGOLD,
        branchCode: String = "TST",
        name: String = "Test Store",
        region: String? = null,
        province: String? = null,
        city: String? = null,
        barangay: String? = null,
        postal: String? = null,
        status: StoreStatus = StoreStatus.ACTIVE,
    ): Store = Store(
        id = id,
        storeType = storeType,
        branchCode = branchCode,
        name = name,
        region = region,
        province = province,
        city = city,
        barangay = barangay,
        postal = postal,
        status = status,
    )

    fun createStoreList(count: Int = 3): List<Store> =
        (1..count).map { i ->
            createStore(
                id = "id-$i",
                branchCode = "BR$i",
                name = "Store $i",
                city = "City $i",
            )
        }
}
