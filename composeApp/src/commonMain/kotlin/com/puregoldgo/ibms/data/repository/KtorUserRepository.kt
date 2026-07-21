package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.api.ProvisionUserRequest
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.api.UpdateRoleRequest
import com.puregoldgo.ibms.shared.api.UpdateUserStatusRequest
import com.puregoldgo.ibms.shared.domain.UserRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
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
 * Ktor-based implementation of [UserRepository].
 * Calls the IBMS backend REST API.
 *
 * Takes the session-aware client — unlike `KtorAuthRepository`, every endpoint
 * here is behind the sysadmin bearer.
 */
class KtorUserRepository(
    private val client: HttpClient,
) : UserRepository {
    private val tag = "KtorUserRepository"

    private val usersUrl: String get() = ApiEndpoint.Users.url(ApiConfig.baseUrl)

    override fun listUsers(
        role: Role?,
        status: UserStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<UserProfile>>>> = flow {
        emit(Resource.Loading)

        emit(
            client.sendRequest<BaseResponse<CursorPage<UserProfile>>>(tag) {
                method = HttpMethod.Get
                url(usersUrl)
                // `parameter` drops nulls, so an unset filter is an absent query
                // key rather than `?status=null`.
                parameter("role", role?.queryValue)
                parameter("status", status?.queryValue)
                parameter("cursor", cursor)
                parameter("limit", limit)
            },
        )
    }

    override suspend fun provisionUser(
        request: ProvisionUserRequest,
    ): Resource<BaseResponse<ProvisionedUser>> =
        client.sendRequest<BaseResponse<ProvisionedUser>>(tag) {
            method = HttpMethod.Post
            url(usersUrl)
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    override suspend fun resetPassword(
        userId: String,
    ): Resource<BaseResponse<ProvisionedUser>> =
        client.sendRequest<BaseResponse<ProvisionedUser>>(tag) {
            method = HttpMethod.Post
            url("$usersUrl/$userId/reset-password")
        }

    override suspend fun updateRole(
        userId: String,
        role: Role,
    ): Resource<BaseResponse<UserProfile>> =
        client.sendRequest<BaseResponse<UserProfile>>(tag) {
            method = HttpMethod.Patch
            url("$usersUrl/$userId/role")
            contentType(ContentType.Application.Json)
            setBody(UpdateRoleRequest(role = role))
        }

    override suspend fun updateStatus(
        userId: String,
        status: UserStatus,
    ): Resource<BaseResponse<UserProfile>> =
        client.sendRequest<BaseResponse<UserProfile>>(tag) {
            method = HttpMethod.Patch
            url("$usersUrl/$userId/status")
            contentType(ContentType.Application.Json)
            setBody(UpdateUserStatusRequest(status = status))
        }
}
