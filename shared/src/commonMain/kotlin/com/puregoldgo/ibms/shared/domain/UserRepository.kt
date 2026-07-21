package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.ProvisionUserRequest
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user administration — every call here is sysadmin-only.
 * Implementation lives in :composeApp (data layer).
 *
 * There is no `deleteUser`, by design: an employee who leaves is deactivated via
 * [updateStatus], which blocks sign-in while keeping the audit trail their
 * account is attached to.
 */
interface UserRepository {

    /**
     * One page of `GET /users`.
     *
     * A page, not the whole list, for the same reason as the other list
     * endpoints — see [ProviderRepository.listProviders]. Callers that need
     * everything loop on [CursorPage.nextCursor] via [usecase.GetUsersUseCase].
     *
     * Omitting [status] returns active and inactive alike, which is what the
     * directory wants: a deactivated account still has to be findable to be
     * turned back on.
     */
    fun listUsers(
        role: Role? = null,
        status: UserStatus? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): Flow<Resource<BaseResponse<CursorPage<UserProfile>>>>

    /**
     * `POST /users` — the only way an account comes into existence.
     *
     * The response carries a plaintext temporary password. See [ProvisionedUser]
     * for what callers may and may not do with it.
     */
    suspend fun provisionUser(
        request: ProvisionUserRequest,
    ): Resource<BaseResponse<ProvisionedUser>>

    /**
     * `POST /users/{id}/reset-password` — the recovery path for a forgotten or
     * expired temporary password.
     *
     * Every existing session for that user is revoked server-side, so this also
     * doubles as the way to cut off an account whose credentials may have leaked.
     */
    suspend fun resetPassword(userId: String): Resource<BaseResponse<ProvisionedUser>>

    /** `PATCH /users/{id}/role`. The backend refuses to demote the last sysadmin. */
    suspend fun updateRole(userId: String, role: Role): Resource<BaseResponse<UserProfile>>

    /** `PATCH /users/{id}/status`. An inactive user is blocked from signing in. */
    suspend fun updateStatus(
        userId: String,
        status: UserStatus,
    ): Resource<BaseResponse<UserProfile>>
}
