package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [ProviderRepository] for unit testing.
 *
 * Serves [providers] as pages of [pageSize] so tests exercise the cursor walk in
 * `loadAllPages` rather than a single canned page — paging is the part most
 * likely to be wrong. The cursor is the index to resume from, as a string.
 */
class FakeProviderRepository : ProviderRepository {

    var providers: List<Provider> = emptyList()
    var shouldFail = false
    var failureMessage = "Test error"
    var pageSize = 100

    var lastCreated: Pair<String, Int>? = null
    var lastUpdated: Triple<String, String?, Int?>? = null

    /** Every cursor this fake was asked for, in order — including the initial null. */
    val requestedCursors = mutableListOf<String?>()

    override fun listProviders(
        status: ProviderStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Provider>>>> = flow {
        requestedCursors += cursor
        emit(Resource.Loading)

        if (shouldFail) {
            emit(Resource.Failed(message = failureMessage))
            return@flow
        }

        val filtered = status?.let { wanted -> providers.filter { it.status == wanted } } ?: providers
        val from = cursor?.toIntOrNull() ?: 0
        // [pageSize] is the ceiling, mirroring the backend clamping `limit` to
        // its own maximum. A caller asking for more than the server will give is
        // exactly the case that makes the cursor walk necessary.
        val size = minOf(limit ?: pageSize, pageSize)
        val slice = filtered.drop(from).take(size)
        val next = (from + size).takeIf { it < filtered.size }?.toString()

        emit(Resource.Success(BaseResponse(data = CursorPage(items = slice, nextCursor = next))))
    }

    override suspend fun createProvider(
        name: String,
        paymentScheduleDay: Int,
    ): Resource<BaseResponse<Provider>> {
        return if (shouldFail) {
            Resource.Failed(message = failureMessage)
        } else {
            lastCreated = name to paymentScheduleDay
            Resource.Success(
                BaseResponse(
                    data = ProviderTestData.createProvider(
                        id = "new-id",
                        name = name,
                        paymentScheduleDay = paymentScheduleDay,
                    ),
                ),
            )
        }
    }

    override suspend fun updateProvider(
        id: String,
        name: String?,
        paymentScheduleDay: Int?,
    ): Resource<BaseResponse<Provider>> {
        return if (shouldFail) {
            Resource.Failed(message = failureMessage)
        } else {
            lastUpdated = Triple(id, name, paymentScheduleDay)
            Resource.Success(
                BaseResponse(
                    data = ProviderTestData.createProvider(
                        id = id,
                        name = name ?: "Unchanged",
                        paymentScheduleDay = paymentScheduleDay ?: 5,
                    ),
                ),
            )
        }
    }
}

object ProviderTestData {
    fun createProvider(
        id: String = "test-id",
        name: String = "Test Provider",
        paymentScheduleDay: Int = 5,
        status: ProviderStatus = ProviderStatus.ACTIVE,
    ): Provider = Provider(
        id = id,
        name = name,
        paymentScheduleDay = paymentScheduleDay,
        status = status,
    )

    fun createProviderList(count: Int = 3): List<Provider> =
        (1..count).map { i ->
            createProvider(id = "id-$i", name = "Provider $i", paymentScheduleDay = (i % 28) + 1)
        }
}
