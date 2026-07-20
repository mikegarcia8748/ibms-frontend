package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.LoginResponse
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RefreshSessionUseCaseTest {

    private lateinit var repository: FakeAuthRepository
    private lateinit var useCase: RefreshSessionUseCase

    @BeforeTest
    fun setup() {
        repository = FakeAuthRepository()
        useCase = RefreshSessionUseCase(repository)
    }

    @Test
    fun tc01_should_fail_when_no_session_is_stored() = runTest {
        repository.hasStoredSession = false

        val results = useCase().toList()

        // A first launch, not an error to show anyone.
        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }

    @Test
    fun tc02_should_fail_when_the_stored_token_is_rejected() = runTest {
        repository.shouldFail = true
        repository.failureMessage = "refresh token is invalid or expired"

        val results = useCase().toList()

        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }

    @Test
    fun tc03_should_return_a_session_with_the_user_attached() = runTest {
        val results = useCase().toList()

        val success = results.filterIsInstance<Resource.Success<LoginResponse>>().first()
        // Refresh carries the profile, which is why resuming a launch needs no
        // second call to find out who is signed in.
        assertNotNull(success.data?.session)
        assertNotNull(success.data?.user)
    }

    @Test
    fun tc04_should_clear_the_stored_session_on_sign_out() = runTest {
        repository.signOut()

        assertTrue(repository.signedOut)
        assertTrue(useCase().toList().filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }
}
