package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.ProvisionUserRequest
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.UserRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In-memory stand-ins for the two repositories the directory panel talks to.
 *
 * Restated here rather than shared with `:shared`'s fakes: a test source set is
 * not published to its dependents, and two small fakes are cheaper than a test
 * fixtures artifact for two of them.
 *
 * Each records what it was asked to do, because most of what is worth asserting
 * about this panel is what it does *not* send — a no-op role change that should
 * never reach the wire, a rejected form that must not half-create an account.
 */
class FakeUserRepository : UserRepository {

    var users: List<UserProfile> = emptyList()
    var shouldFail = false
    var failureMessage = "Test error"

    var lastProvisioned: ProvisionUserRequest? = null
    var lastReset: String? = null
    var roleChanges: MutableList<Pair<String, Role>> = mutableListOf()
    var statusChanges: MutableList<Pair<String, UserStatus>> = mutableListOf()

    override fun listUsers(
        role: Role?,
        status: UserStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<UserProfile>>>> = flow {
        emit(Resource.Loading)
        if (shouldFail) {
            emit(Resource.Failed(message = failureMessage))
            return@flow
        }
        emit(Resource.Success(BaseResponse(data = CursorPage(items = users, nextCursor = null))))
    }

    override suspend fun provisionUser(
        request: ProvisionUserRequest,
    ): Resource<BaseResponse<ProvisionedUser>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        lastProvisioned = request
        return Resource.Success(
            BaseResponse(
                data = ProvisionedUser(
                    user = UserProfile(
                        id = "new-id",
                        username = request.username,
                        name = request.name,
                        employeeNumber = request.employeeNumber,
                        role = request.role,
                        status = UserStatus.ACTIVE,
                        mustChangePassword = true,
                    ),
                    temporaryPassword = TEST_TEMPORARY_PASSWORD,
                    temporaryPasswordExpiresAt = "2026-07-24T09:00:00Z",
                ),
            ),
        )
    }

    override suspend fun resetPassword(userId: String): Resource<BaseResponse<ProvisionedUser>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        lastReset = userId
        val existing = users.first { it.id == userId }
        return Resource.Success(
            BaseResponse(
                data = ProvisionedUser(
                    user = existing,
                    temporaryPassword = TEST_TEMPORARY_PASSWORD,
                    temporaryPasswordExpiresAt = "2026-07-24T09:00:00Z",
                ),
            ),
        )
    }

    override suspend fun updateRole(
        userId: String,
        role: Role,
    ): Resource<BaseResponse<UserProfile>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        roleChanges += userId to role
        return Resource.Success(BaseResponse(data = users.first { it.id == userId }.copy(role = role)))
    }

    override suspend fun updateStatus(
        userId: String,
        status: UserStatus,
    ): Resource<BaseResponse<UserProfile>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        statusChanges += userId to status
        return Resource.Success(
            BaseResponse(data = users.first { it.id == userId }.copy(status = status)),
        )
    }
}

class FakeProviderRepository : ProviderRepository {

    var providers: List<Provider> = emptyList()

    override fun listProviders(
        status: ProviderStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Provider>>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(BaseResponse(data = CursorPage(items = providers, nextCursor = null))))
    }

    override suspend fun createProvider(
        name: String,
        paymentScheduleDay: Int,
    ): Resource<BaseResponse<Provider>> = error("not used by the directory panel")

    override suspend fun updateProvider(
        id: String,
        name: String?,
        paymentScheduleDay: Int?,
    ): Resource<BaseResponse<Provider>> = error("not used by the directory panel")
}

/** Obviously fake — nothing resembling a real generated secret belongs in a source file. */
private const val TEST_TEMPORARY_PASSWORD = "TEST-not-a-real-password"
