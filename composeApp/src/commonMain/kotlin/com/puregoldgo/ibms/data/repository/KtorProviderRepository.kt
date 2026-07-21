package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.api.CreateProviderRequest
import com.puregoldgo.ibms.shared.api.UpdateProviderRequest
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.queryValue
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ktor-based implementation of [ProviderRepository].
 * Calls the IBMS backend REST API.
 */
class KtorProviderRepository(
    private val client: HttpClient,
) : ProviderRepository {
    private val tag = "KtorProviderRepository"

    override fun listProviders(
        status: ProviderStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Provider>>>> = flow {
        emit(Resource.Loading)

        emit(
            client.sendRequest<BaseResponse<CursorPage<Provider>>>(tag) {
                method = HttpMethod.Get
                url(ApiEndpoint.Providers.url(ApiConfig.baseUrl))
                // `parameter` drops nulls, so an unset filter is an absent query
                // key rather than `?status=null`.
                parameter("status", status?.queryValue)
                parameter("cursor", cursor)
                parameter("limit", limit)
            },
        )
    }

    override suspend fun createProvider(
        name: String,
        paymentScheduleDay: Int,
    ): Resource<BaseResponse<Provider>> =
        client.sendRequest<BaseResponse<Provider>>(tag) {
            method = HttpMethod.Post
            url(ApiEndpoint.Providers.url(ApiConfig.baseUrl))
            contentType(ContentType.Application.Json)
            setBody(CreateProviderRequest(name = name, paymentScheduleDay = paymentScheduleDay))
        }

    override suspend fun updateProvider(
        id: String,
        name: String?,
        paymentScheduleDay: Int?,
    ): Resource<BaseResponse<Provider>> =
        client.sendRequest<BaseResponse<Provider>>(tag) {
            method = HttpMethod.Put
            url(ApiEndpoint.Providers.url(ApiConfig.baseUrl) + "/$id")
            contentType(ContentType.Application.Json)
            setBody(UpdateProviderRequest(name = name, paymentScheduleDay = paymentScheduleDay))
        }
}
