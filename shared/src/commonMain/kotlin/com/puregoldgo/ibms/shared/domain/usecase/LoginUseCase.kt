package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.api.AuthResponse
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Authenticates a user with username and password.
 * Validates required fields before delegating to the repository.
 */
class LoginUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(username: String, password: String): Flow<Resource<AuthResponse>> = flow {
        val usernameError = Validation.validateRequired(username, "Username")
        if (usernameError != null) {
            emit(Resource.Failed(message = usernameError))
            return@flow
        }
        val passwordError = Validation.validateRequired(password, "Password")
        if (passwordError != null) {
            emit(Resource.Failed(message = passwordError))
            return@flow
        }
        emitAll(repository.login(username, password))
    }
}
