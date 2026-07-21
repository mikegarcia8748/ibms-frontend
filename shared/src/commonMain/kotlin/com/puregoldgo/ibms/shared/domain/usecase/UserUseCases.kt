package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.ProvisionUserRequest
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.domain.UserRepository
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * User administration, sysadmin-only.
 *
 * Each of these unwraps the response envelope so the ViewModel never sees a
 * [BaseResponse] — see [unwrap], which also states why a `success` envelope with
 * no `data` is treated as a failure.
 */

/**
 * Fetches every user, walking the endpoint's pages to exhaustion.
 *
 * Leaves [status] unset by default: the directory lists active and deactivated
 * accounts alike, because a deactivated one still has to be findable to be
 * turned back on.
 */
class GetUsersUseCase(
    private val repository: UserRepository,
) {
    operator fun invoke(
        role: Role? = null,
        status: UserStatus? = null,
    ): Flow<Resource<List<UserProfile>>> =
        loadAllPages { cursor ->
            repository.listUsers(role = role, status = status, cursor = cursor, limit = PAGE_SIZE)
        }
}

/**
 * Creates a user account and returns the generated temporary password.
 *
 * Username and name are checked here rather than left to the server because both
 * rejections are cheap to predict and expensive to discover after a round trip —
 * and because the username is normalised on the way in (see
 * [Validation.normalizeUsername]), so the form must send the same string the
 * backend will compare for uniqueness.
 *
 * The blank-to-null pass on [employeeNumber] matters: the column is nullable and
 * an empty string is not "absent", it is a stored empty employee number.
 */
class ProvisionUserUseCase(
    private val repository: UserRepository,
) {
    operator fun invoke(
        username: String,
        name: String,
        employeeNumber: String? = null,
        role: Role = Role.PENDING,
    ): Flow<Resource<ProvisionedUser>> = flow {
        emit(Resource.Loading)

        val nameError = Validation.validateRequired(name, "Full name")
        if (nameError != null) {
            emit(Resource.Failed(message = nameError))
            return@flow
        }
        val usernameError = Validation.validateUsername(username)
        if (usernameError != null) {
            emit(Resource.Failed(message = usernameError))
            return@flow
        }

        emit(
            unwrap(
                repository.provisionUser(
                    ProvisionUserRequest(
                        username = Validation.normalizeUsername(username),
                        name = name.trim(),
                        employeeNumber = employeeNumber?.trim()?.takeIf { it.isNotBlank() },
                        role = role,
                    ),
                ),
            ),
        )
    }
}

/**
 * Issues a fresh temporary password for a user who lost or never redeemed theirs.
 *
 * Every session that user holds is revoked server-side as a side effect, so this
 * is not a silent operation — callers should confirm before invoking it.
 */
class ResetUserPasswordUseCase(
    private val repository: UserRepository,
) {
    operator fun invoke(userId: String): Flow<Resource<ProvisionedUser>> = flow {
        emit(Resource.Loading)

        if (userId.isBlank()) {
            emit(Resource.Failed(message = "A user is required to reset a password"))
            return@flow
        }
        emit(unwrap(repository.resetPassword(userId)))
    }
}

/**
 * Changes a user's role.
 *
 * The "last sysadmin cannot be demoted" rule is the backend's alone — it is the
 * only party that can count them without a race — so that rejection arrives as a
 * failure message rather than being pre-empted here.
 */
class UpdateUserRoleUseCase(
    private val repository: UserRepository,
) {
    operator fun invoke(userId: String, role: Role): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)

        if (userId.isBlank()) {
            emit(Resource.Failed(message = "A user is required to change a role"))
            return@flow
        }
        emit(unwrap(repository.updateRole(userId, role)))
    }
}

/** Activates or deactivates a user. An inactive user is blocked from signing in. */
class UpdateUserStatusUseCase(
    private val repository: UserRepository,
) {
    operator fun invoke(userId: String, status: UserStatus): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading)

        if (userId.isBlank()) {
            emit(Resource.Failed(message = "A user is required to change a status"))
            return@flow
        }
        emit(unwrap(repository.updateStatus(userId, status)))
    }
}

/**
 * Narrows a `Resource<BaseResponse<T>>` to a `Resource<T>`.
 *
 * A `success` envelope carrying no `data` becomes a failure rather than an empty
 * success. For these endpoints the payload *is* the result — a provision that
 * reported "done" without a temporary password would leave an account nobody can
 * sign in to, with nothing on screen to say so.
 */
private fun <T> unwrap(result: Resource<BaseResponse<T>>): Resource<T> = when (result) {
    is Resource.Loading -> Resource.Loading
    is Resource.Success -> result.data?.data?.let { Resource.Success(it) }
        ?: Resource.Failed(message = result.data?.message ?: "No data returned from server")

    is Resource.Failed -> Resource.Failed(message = result.message ?: result.data?.message)
    is Resource.Error -> Resource.Error(result.error)
}
