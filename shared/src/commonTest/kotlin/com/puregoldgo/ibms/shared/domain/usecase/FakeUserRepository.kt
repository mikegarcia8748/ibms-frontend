package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.ProvisionUserRequest
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.domain.UserRepository
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [UserRepository] for unit testing.
 *
 * Serves [users] as pages of [pageSize] so tests exercise the cursor walk in
 * `loadAllPages` rather than a single canned page. The cursor is the index to
 * resume from, as a string — same shape as [FakeProviderRepository].
 */
class FakeUserRepository : UserRepository {

    var users: List<UserProfile> = emptyList()
    var shouldFail = false
    var failureMessage = "Test error"
    var pageSize = 100

    var lastProvisioned: ProvisionUserRequest? = null
    var lastReset: String? = null
    var lastRoleChange: Pair<String, Role>? = null
    var lastStatusChange: Pair<String, UserStatus>? = null

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

        val filtered = users
            .filter { role == null || it.role == role }
            .filter { status == null || it.status == status }
        val from = cursor?.toIntOrNull() ?: 0
        val size = minOf(limit ?: pageSize, pageSize)
        val slice = filtered.drop(from).take(size)
        val next = (from + size).takeIf { it < filtered.size }?.toString()

        emit(Resource.Success(BaseResponse(data = CursorPage(items = slice, nextCursor = next))))
    }

    override suspend fun provisionUser(
        request: ProvisionUserRequest,
    ): Resource<BaseResponse<ProvisionedUser>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        lastProvisioned = request
        return Resource.Success(
            BaseResponse(
                data = UserTestData.provisioned(
                    UserTestData.user(
                        id = "new-id",
                        username = request.username,
                        name = request.name,
                        employeeNumber = request.employeeNumber,
                        role = request.role,
                        mustChangePassword = true,
                    ),
                ),
            ),
        )
    }

    override suspend fun resetPassword(userId: String): Resource<BaseResponse<ProvisionedUser>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        lastReset = userId
        return Resource.Success(
            BaseResponse(
                data = UserTestData.provisioned(
                    UserTestData.user(id = userId, mustChangePassword = true),
                ),
            ),
        )
    }

    override suspend fun updateRole(
        userId: String,
        role: Role,
    ): Resource<BaseResponse<UserProfile>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        lastRoleChange = userId to role
        return Resource.Success(BaseResponse(data = UserTestData.user(id = userId, role = role)))
    }

    override suspend fun updateStatus(
        userId: String,
        status: UserStatus,
    ): Resource<BaseResponse<UserProfile>> {
        if (shouldFail) return Resource.Failed(message = failureMessage)
        lastStatusChange = userId to status
        return Resource.Success(
            BaseResponse(data = UserTestData.user(id = userId, status = status)),
        )
    }
}

object UserTestData {

    /** Deliberately not password-shaped — nothing that reads as a real secret. */
    const val TEMPORARY_PASSWORD = "TEST-temporary-password"

    fun user(
        id: String = "usr-1",
        username: String = "jdoe",
        name: String = "Jane Doe",
        employeeNumber: String? = null,
        role: Role = Role.PENDING,
        status: UserStatus = UserStatus.ACTIVE,
        mustChangePassword: Boolean = false,
    ): UserProfile = UserProfile(
        id = id,
        username = username,
        name = name,
        employeeNumber = employeeNumber,
        role = role,
        status = status,
        mustChangePassword = mustChangePassword,
    )

    fun provisioned(user: UserProfile): ProvisionedUser = ProvisionedUser(
        user = user,
        temporaryPassword = TEMPORARY_PASSWORD,
        temporaryPasswordExpiresAt = "2026-07-24T09:00:00Z",
    )

    fun userList(count: Int = 3): List<UserProfile> =
        (1..count).map { i -> user(id = "usr-$i", username = "user$i", name = "User $i") }
}
