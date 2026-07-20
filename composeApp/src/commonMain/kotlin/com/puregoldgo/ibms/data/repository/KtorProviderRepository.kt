package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.model.Provider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
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
    private val baseUrl: String = "http://localhost:8080",
) : ProviderRepository {
    private val tag = "KtorProviderRepository"

    override suspend fun getProviders(): Resource<BaseResponse<List<Provider>>> {
        return client.sendRequest<BaseResponse<List<Provider>>>(tag) {
            method = HttpMethod.Get
            url(ApiEndpoint.Providers.url(baseUrl))
        }
    }

    override suspend fun createProvider(provider: Provider): Resource<BaseResponse<Provider>> {
        return client.sendRequest<BaseResponse<Provider>>(tag) {
            method = HttpMethod.Post
            url(ApiEndpoint.Providers.url(baseUrl))
            setBody(provider)
        }
    }

    override suspend fun updateProvider(provider: Provider): Resource<BaseResponse<Provider>> {
        return client.sendRequest<BaseResponse<Provider>>(tag) {
            method = HttpMethod.Put
            url(ApiEndpoint.Providers.url(baseUrl) + "/${provider.id}")
            setBody(provider)
        }
    }
}
