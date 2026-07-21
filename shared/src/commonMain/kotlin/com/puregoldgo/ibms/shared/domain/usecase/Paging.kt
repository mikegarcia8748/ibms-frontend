package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.model.CursorPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The page size asked of every list endpoint. The backend clamps to 100, so
 * asking for more only costs a round trip.
 */
internal const val PAGE_SIZE = 100

/**
 * How many pages [loadAllPages] will walk before giving up — 100 pages of 100 is
 * 10,000 records.
 *
 * This is a guard, not a limit anyone should reach. A cursor that never resolves
 * to null (a backend bug, a mangled cursor) would otherwise loop forever with the
 * spinner up.
 */
internal const val MAX_PAGES = 100

/**
 * Walks a cursor-paginated endpoint to exhaustion and emits the whole list.
 *
 * Fetching everything is a deliberate choice for the screens that use this: their
 * filters (an A–Z rail that only offers letters that exist, an ISP dropdown and a
 * search box that compose rather than override) all need the full set to be
 * correct. It holds only while these tables are small.
 *
 * Running past [MAX_PAGES] fails rather than returning what it has. A truncated
 * list is indistinguishable on screen from a complete one, and quietly hiding
 * accounts from a billing tool is worse than showing an error that says why.
 */
internal fun <T> loadAllPages(
    fetchPage: (cursor: String?) -> Flow<Resource<BaseResponse<CursorPage<T>>>>,
): Flow<Resource<List<T>>> = flow {
    emit(Resource.Loading)

    val all = mutableListOf<T>()
    var cursor: String? = null
    var pages = 0

    while (true) {
        var page: CursorPage<T>? = null
        var failure: Resource<List<T>>? = null

        // Each page is its own Flow; its Loading is swallowed because this
        // function already emitted one for the whole walk.
        fetchPage(cursor).collect { result ->
            when (result) {
                is Resource.Loading -> Unit

                is Resource.Success -> {
                    val body = result.data?.data
                    if (body == null) {
                        failure = Resource.Failed(message = result.data?.message)
                    } else {
                        page = body
                    }
                }

                is Resource.Failed ->
                    failure = Resource.Failed(message = result.message ?: result.data?.message)

                is Resource.Error -> failure = Resource.Error(result.error)
            }
        }

        val failed = failure
        if (failed != null) {
            emit(failed)
            return@flow
        }

        val current = page ?: run {
            emit(Resource.Failed(message = "The server returned no data for this page."))
            return@flow
        }

        all += current.items
        pages++

        val next = current.nextCursor
        if (next == null) break

        if (pages >= MAX_PAGES) {
            emit(
                Resource.Failed(
                    message = "This list is larger than ${MAX_PAGES * PAGE_SIZE} records " +
                        "and can no longer be loaded in one go.",
                ),
            )
            return@flow
        }

        cursor = next
    }

    emit(Resource.Success(all.toList()))
}
