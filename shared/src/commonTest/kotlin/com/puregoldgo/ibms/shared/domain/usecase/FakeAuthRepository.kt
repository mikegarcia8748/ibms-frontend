package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.api.AuthResponse
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.model.User
import com.puregoldgo.ibms.shared.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of [AuthRepository] for unit testing.
 */
class FakeAuthRepository : AuthRepository {

    var shouldFail = false
    var failureMessage = "Test error"
    var lastUsername: String? = null
    var lastPassword: String? = null

    private val stubAuthResponse = AuthResponse(
        token = "fake-jwt-token",
        user = User(
            id = "user-id-1",
            googleSub = "",
            email = "admin@ibms.com",
            displayName = "Admin User",
            role = Role.SYSADMIN,
        ),
    )

    override fun login(username: String, password: String): Flow<Resource<AuthResponse>> {
        lastUsername = username
        lastPassword = password
        return if (shouldFail) {
            flowOf(Resource.Failed(message = failureMessage))
        } else {
            flowOf(Resource.Success(stubAuthResponse))
        }
    }
}
