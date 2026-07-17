package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.storage.TokenStorage
import com.puregoldgo.ibms.shared.api.ApiEnvelope
import com.puregoldgo.ibms.shared.api.AuthResponse
import com.puregoldgo.ibms.shared.api.LoginRequest
import com.puregoldgo.ibms.shared.domain.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ktor-based implementation of [AuthRepository].
 * Calls the IBMS backend REST API for authentication.
 */
class KtorAuthRepository(
    private val client: HttpClient,
    private val baseUrl: String = "http://localhost:8080",
) : AuthRepository {

    override fun login(username: String, password: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading)
        try {
            val envelope: ApiEnvelope<AuthResponse> = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username = username, password = password))
            }.body()

            if (envelope.result == "success") {
                val authData = envelope.data
                    ?: return@flow emit(Resource.Failed(message = "Authentication succeeded but no data returned"))
                TokenStorage.saveToken(authData.token)
                emit(Resource.Success(authData))
            } else {
                emit(Resource.Failed(message = envelope.message))
            }
        } catch (e: Exception) {
            emit(Resource.Failed(message = e.message ?: "Login failed. Please try again."))
        }
    }
}
