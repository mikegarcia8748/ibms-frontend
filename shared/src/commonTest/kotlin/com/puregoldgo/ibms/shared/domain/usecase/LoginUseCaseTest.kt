package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginOutcome
import com.puregoldgo.ibms.shared.api.LoginResponse
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: LoginUseCase

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = LoginUseCase(repository)
    }

    // region Negative Tests

    @Test
    fun tc01_should_return_failed_when_username_is_blank() = runTest {
        // Act
        val results = useCase(username = "", password = "password123").toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Username is required", failed.first().message)
    }

    @Test
    fun tc02_should_return_failed_when_password_is_blank() = runTest {
        // Act
        val results = useCase(username = "admin", password = "").toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Password is required", failed.first().message)
    }

    @Test
    fun tc03_should_return_failed_when_repository_fails() = runTest {
        // Arrange
        repository.shouldFail = true
        repository.failureMessage = "Invalid credentials"

        // Act
        val results = useCase(username = "admin", password = "wrong").toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Invalid credentials", failed.first().message)
    }

    // endregion

    // region Happy Path Tests

    @Test
    fun tc11_should_login_with_valid_credentials() = runTest {
        // Act
        val results = useCase(username = "admin", password = "password123").toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        assertEquals("admin", repository.lastUsername)
        assertEquals("password123", repository.lastPassword)
    }

    @Test
    fun tc12_should_return_session_tokens_when_authenticated() = runTest {
        // Act
        val results = useCase(username = "admin", password = "password123").toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        val data = success.first().data
        assertIs<LoginResponse>(data)
        assertEquals(LoginOutcome.AUTHENTICATED, data.outcome)
        assertEquals("fake-access-token", data.session?.accessToken)
        assertNull(data.passwordChange)
    }

    @Test
    fun tc13_should_return_challenge_without_session_for_temporary_password() = runTest {
        // Arrange — a temporary password is accepted but does NOT authenticate.
        repository.requiresPasswordChange = true

        // Act
        val results = useCase(username = "admin", password = "temp-password").toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        val data = success.first().data
        assertIs<LoginResponse>(data)
        assertEquals(LoginOutcome.PASSWORD_CHANGE_REQUIRED, data.outcome)
        assertNull(data.session, "a temporary password must not mint a session")
        assertEquals("fake-challenge-token", data.passwordChange?.challengeToken)
        // The user is populated while still unauthenticated — this is exactly why
        // callers must branch on `session`, never on the presence of `user`.
        assertEquals("admin", data.user.username)
    }

    // endregion
}
