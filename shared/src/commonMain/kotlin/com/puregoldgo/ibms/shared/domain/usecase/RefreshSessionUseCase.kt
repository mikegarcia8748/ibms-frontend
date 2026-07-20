package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.domain.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Resumes a stored session on launch.
 *
 * A failure here is ordinary, not exceptional — the refresh token expires, a
 * sysadmin resets the password, someone signs out everywhere — so the caller
 * treats it as "show sign-in", never as an error to report.
 */
class RefreshSessionUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<Resource<LoginResponse>> = repository.refreshSession()
}
