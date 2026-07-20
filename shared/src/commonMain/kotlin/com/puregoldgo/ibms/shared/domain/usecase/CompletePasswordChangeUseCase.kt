package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Redeems a change-password challenge — the step that turns "holds a temporary
 * password" into "has a session".
 *
 * Validates locally first so an obviously bad password costs no round trip, but
 * the backend re-checks everything: this copy of the policy is a courtesy to the
 * user, not a security boundary.
 */
class CompletePasswordChangeUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(
        challengeToken: String,
        username: String,
        newPassword: String,
        confirmPassword: String,
    ): Flow<Resource<LoginResponse>> = flow {
        if (challengeToken.isBlank()) {
            // No challenge means nothing to redeem — the caller has to sign in
            // again with the temporary password.
            emit(Resource.Failed(message = "Your set-password session has expired. Please sign in again."))
            return@flow
        }

        val passwordError = Validation.validateNewPassword(newPassword, username)
        if (passwordError != null) {
            emit(Resource.Failed(message = passwordError))
            return@flow
        }

        val confirmationError = Validation.validatePasswordConfirmation(newPassword, confirmPassword)
        if (confirmationError != null) {
            emit(Resource.Failed(message = confirmationError))
            return@flow
        }

        emitAll(repository.completePasswordChange(challengeToken, newPassword))
    }
}
