package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Implementation lives in :composeApp (data layer).
 *
 * All three calls answer with the same [LoginResponse] shape, which is what lets
 * the rule "authenticated ⟺ `session != null`" be checked in one place no matter
 * how the caller arrived — first sign-in, forced password change, or a silent
 * resume on launch.
 */
interface AuthRepository {

    fun login(username: String, password: String): Flow<Resource<LoginResponse>>

    /**
     * Redeems a change-password challenge. [challengeToken] authorizes this one
     * call and nothing else — it is not a session token, so it is passed in
     * explicitly rather than read from session storage.
     *
     * For a provisioned account this is the moment a session first exists.
     */
    fun completePasswordChange(
        challengeToken: String,
        newPassword: String,
    ): Flow<Resource<LoginResponse>>

    /**
     * Exchanges the stored refresh token for a fresh pair — how a launch resumes
     * a session. The presented token is revoked as it is spent, so this succeeds
     * at most once per stored token.
     */
    fun refreshSession(): Flow<Resource<LoginResponse>>

    /** Drops local session state. There is no server call — no logout endpoint. */
    fun signOut()
}
