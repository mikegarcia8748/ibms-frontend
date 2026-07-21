package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.queryValue
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ktor-based implementation of [StoreRepository].
 * Calls the IBMS backend REST API.
 */
class KtorStoreRepository(
    private val client: HttpClient,
) : StoreRepository {
    private val tag = "KtorStoreRepository"

    override fun listStores(
        status: StoreStatus?,
        query: String?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Store>>>> = flow {
        emit(Resource.Loading)

        emit(
            client.sendRequest<BaseResponse<CursorPage<Store>>>(tag) {
                method = HttpMethod.Get
                url(ApiEndpoint.Stores.url(ApiConfig.baseUrl))
                // `parameter` drops nulls, so an unset filter is an absent query
                // key rather than `?status=null`.
                parameter("status", status?.queryValue)
                parameter("q", query?.takeIf { it.isNotBlank() })
                parameter("cursor", cursor)
                parameter("limit", limit)
            },
        )
    }
}
