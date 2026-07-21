package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateProviderUseCaseTest {

    private lateinit var repository: FakeProviderRepository
    private lateinit var useCase: CreateProviderUseCase

    @BeforeTest
    fun setup() {
        repository = FakeProviderRepository()
        useCase = CreateProviderUseCase(repository)
    }

    // region Negative Tests (tc01–tc10)

    @Test
    fun tc01_should_return_failed_when_name_is_blank() = runTest {
        // Act
        val results = useCase(name = "", paymentScheduleDay = 5).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Provider name is required", failed.first().message)
    }

    @Test
    fun tc02_should_return_failed_when_payment_day_is_below_range() = runTest {
        // Act
        val results = useCase(name = "Globe", paymentScheduleDay = 0).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Payment schedule day must be between 1 and 31", failed.first().message)
    }

    @Test
    fun tc03_should_return_failed_when_payment_day_is_above_range() = runTest {
        // Act
        val results = useCase(name = "Globe", paymentScheduleDay = 32).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Payment schedule day must be between 1 and 31", failed.first().message)
    }

    @Test
    fun tc04_should_not_reach_the_repository_when_validation_fails() = runTest {
        // Act
        useCase(name = "  ", paymentScheduleDay = 5).toList()

        // Assert — a rejected form must not cost a round trip
        assertNull(repository.lastCreated)
    }

    @Test
    fun tc05_should_return_failed_when_repository_fails() = runTest {
        // Arrange
        repository.shouldFail = true
        repository.failureMessage = "Server error"

        // Act
        val results = useCase(name = "Globe", paymentScheduleDay = 5).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Server error", failed.first().message)
    }

    // endregion

    // region Happy Path Tests (tc11+)

    @Test
    fun tc11_should_create_provider_with_valid_data() = runTest {
        // Act
        val results = useCase(name = "Globe Telecom", paymentScheduleDay = 5).toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        assertEquals("Globe Telecom" to 5, repository.lastCreated)
    }

    @Test
    fun tc12_should_accept_both_ends_of_the_payment_day_range() = runTest {
        // Act
        val first = useCase(name = "Converge", paymentScheduleDay = 1).toList()
        val last = useCase(name = "Converge", paymentScheduleDay = 31).toList()

        // Assert
        assertTrue(first.filterIsInstance<Resource.Success<*>>().isNotEmpty())
        assertTrue(last.filterIsInstance<Resource.Success<*>>().isNotEmpty())
    }

    @Test
    fun tc13_should_trim_the_name_before_sending_it() = runTest {
        // Act
        useCase(name = "  Smart  ", paymentScheduleDay = 10).toList()

        // Assert
        assertEquals("Smart" to 10, repository.lastCreated)
    }

    // endregion
}
