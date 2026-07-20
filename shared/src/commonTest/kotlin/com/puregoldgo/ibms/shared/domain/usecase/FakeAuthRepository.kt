package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginOutcome
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.api.PasswordChangeChallenge
import com.puregoldgo.ibms.shared.api.SessionTokens
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of [AuthRepository] for unit testing.
 *
 * Defaults to the fully-authenticated outcome. Set [requiresPasswordChange] to
 * emit the temporary-password path instead, where `session` is null and the
 * caller holds only a change-password challenge.
 */
class FakeAuthRepository : AuthRepository {

    var shouldFail = false
    var failureMessage = "Test error"
    var requiresPasswordChange = false
    var lastUsername: String? = null
    var lastPassword: String? = null

    /** Set when there is no stored refresh token to resume from. */
    var hasStoredSession = true
    var lastChallengeToken: String? = null
    var lastNewPassword: String? = null
    var signedOut = false

    private val stubUser = UserProfile(
        id = "user-id-1",
        username = "admin",
        name = "Admin User",
        employeeNumber = "EMP-0001",
        role = Role.SYSADMIN,
        mustChangePassword = false,
    )

    private val authenticatedResponse = LoginResponse(
        outcome = LoginOutcome.AUTHENTICATED,
        user = stubUser,
        session = SessionTokens(
            accessToken = "fake-access-token",
            refreshToken = "fake-refresh-token",
            tokenType = "Bearer",
            expiresInSeconds = 900,
        ),
        passwordChange = null,
    )

    private val passwordChangeResponse = LoginResponse(
        outcome = LoginOutcome.PASSWORD_CHANGE_REQUIRED,
        user = stubUser.copy(mustChangePassword = true),
        session = null,
        passwordChange = PasswordChangeChallenge(
            challengeToken = "fake-challenge-token",
            expiresInSeconds = 600,
            reason = "temporary_password",
        ),
    )

    override fun login(username: String, password: String): Flow<Resource<LoginResponse>> {
        lastUsername = username
        lastPassword = password
        return when {
            shouldFail -> flowOf(Resource.Failed(message = failureMessage))
            requiresPasswordChange -> flowOf(Resource.Success(passwordChangeResponse))
            else -> flowOf(Resource.Success(authenticatedResponse))
        }
    }

    override fun completePasswordChange(
        challengeToken: String,
        newPassword: String,
    ): Flow<Resource<LoginResponse>> {
        lastChallengeToken = challengeToken
        lastNewPassword = newPassword
        return if (shouldFail) {
            flowOf(Resource.Failed(message = failureMessage))
        } else {
            // Redeeming a challenge always lands on a session — that is the point
            // of the endpoint.
            flowOf(Resource.Success(authenticatedResponse))
        }
    }

    override fun refreshSession(): Flow<Resource<LoginResponse>> = when {
        !hasStoredSession -> flowOf(Resource.Failed(message = "No stored session"))
        shouldFail -> flowOf(Resource.Failed(message = failureMessage))
        else -> flowOf(Resource.Success(authenticatedResponse))
    }

    override fun signOut() {
        signedOut = true
        hasStoredSession = false
    }
}
