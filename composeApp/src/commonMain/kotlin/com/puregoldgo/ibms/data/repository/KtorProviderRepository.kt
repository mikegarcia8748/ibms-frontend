package com.puregoldgo.ibms.data.repository

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.Resource
import com.puregoldgo.ibms.shared.model.Provider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
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

    override fun getProviders(): Flow<Resource<List<Provider>>> = flow {
        emit(Resource.Loading)
        try {
            val providers: List<Provider> = client.get("$baseUrl/providers").body()
            emit(Resource.Success(providers))
        } catch (e: Exception) {
            emit(Resource.Failed(e.message ?: "Failed to fetch providers"))
        }
    }

    override fun createProvider(provider: Provider): Flow<Resource<Provider>> = flow {
        emit(Resource.Loading)
        try {
            val created: Provider = client.post("$baseUrl/providers") {
                contentType(ContentType.Application.Json)
                setBody(provider)
            }.body()
            emit(Resource.Success(created))
        } catch (e: Exception) {
            emit(Resource.Failed(e.message ?: "Failed to create provider"))
        }
    }

    override fun updateProvider(provider: Provider): Flow<Resource<Provider>> = flow {
        emit(Resource.Loading)
        try {
            val updated: Provider = client.put("$baseUrl/providers/${provider.id}") {
                contentType(ContentType.Application.Json)
                setBody(provider)
            }.body()
            emit(Resource.Success(updated))
        } catch (e: Exception) {
            emit(Resource.Failed(e.message ?: "Failed to update provider"))
        }
    }
}
