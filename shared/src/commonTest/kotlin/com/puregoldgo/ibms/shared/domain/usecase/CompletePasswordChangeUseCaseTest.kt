package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginOutcome
import com.puregoldgo.ibms.shared.api.LoginResponse
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompletePasswordChangeUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: CompletePasswordChangeUseCase

    private val validPassword = "Chosen-Passw0rd"

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = CompletePasswordChangeUseCase(repository)
    }

    private suspend fun invoke(
        challengeToken: String = "challenge-token",
        username: String = "admin",
        newPassword: String = validPassword,
        confirmPassword: String = validPassword,
    ) = useCase(challengeToken, username, newPassword, confirmPassword).toList()

    // region Negative Tests

    @Test
    fun tc01_should_fail_without_a_challenge_token() = runTest {
        // No challenge means nothing to redeem — do not spend a request finding out.
        val results = invoke(challengeToken = "")

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertTrue(failed.first().message!!.contains("expired"))
        assertNull(repository.lastNewPassword)
    }

    @Test
    fun tc02_should_fail_when_password_violates_policy() = runTest {
        val results = invoke(newPassword = "short", confirmPassword = "short")

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertTrue(failed.first().message!!.startsWith("Password must"))
        assertNull(repository.lastNewPassword)
    }

    @Test
    fun tc03_should_fail_when_password_contains_the_username() = runTest {
        val chosen = "Admin-Passw0rd"
        val results = invoke(username = "admin", newPassword = chosen, confirmPassword = chosen)

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.first().message!!.contains("not contain your username"))
    }

    @Test
    fun tc04_should_fail_when_confirmation_does_not_match() = runTest {
        val results = invoke(confirmPassword = "Chosen-Passw0rE")

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertEquals("Passwords do not match", failed.first().message)
        assertNull(repository.lastNewPassword)
    }

    @Test
    fun tc05_should_surface_a_repository_failure() = runTest {
        repository.shouldFail = true
        repository.failureMessage = "password has already been set"

        val results = invoke()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertEquals("password has already been set", failed.first().message)
    }

    // endregion

    // region Positive Tests

    @Test
    fun tc06_should_pass_the_challenge_and_password_through_to_the_repository() = runTest {
        invoke()

        assertEquals("challenge-token", repository.lastChallengeToken)
        assertEquals(validPassword, repository.lastNewPassword)
    }

    @Test
    fun tc07_should_emit_an_authenticated_session() = runTest {
        val results = invoke()

        val success = results.filterIsInstance<Resource.Success<LoginResponse>>().first()
        // The whole point of the endpoint: this is where the session begins.
        assertEquals(LoginOutcome.AUTHENTICATED, success.data?.outcome)
        assertNotNull(success.data?.session)
        assertNull(success.data?.passwordChange)
    }

    // endregion
}
