package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.Resource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
        // Arrange
        val provider = ProviderTestData.createProvider(name = "")

        // Act
        val results = useCase(provider).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Provider name is required", failed.first().message)
    }

    @Test
    fun tc02_should_return_failed_when_code_is_blank() = runTest {
        // Arrange
        val provider = ProviderTestData.createProvider(code = "")

        // Act
        val results = useCase(provider).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Provider code is required", failed.first().message)
    }

    @Test
    fun tc03_should_return_failed_when_email_is_invalid() = runTest {
        // Arrange
        val provider = ProviderTestData.createProvider(contactEmail = "invalid-email")

        // Act
        val results = useCase(provider).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Invalid email format", failed.first().message)
    }

    @Test
    fun tc04_should_return_failed_when_repository_fails() = runTest {
        // Arrange
        repository.shouldFail = true
        repository.failureMessage = "Server error"
        val provider = ProviderTestData.createProvider()

        // Act
        val results = useCase(provider).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Server error", failed.first().message)
    }

    // endregion

    // region Happy Path Tests (tc11+)

    @Test
    fun tc11_should_create_provider_with_valid_data() = runTest {
        // Arrange
        val provider = ProviderTestData.createProvider(
            name = "Globe Telecom",
            code = "GLOBE",
            contactEmail = "support@globe.com",
        )

        // Act
        val results = useCase(provider).toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        assertEquals("Globe Telecom", repository.lastCreatedProvider?.name)
    }

    @Test
    fun tc12_should_create_provider_with_null_optional_fields() = runTest {
        // Arrange
        val provider = ProviderTestData.createProvider(
            name = "Smart",
            code = "SMART",
            contactEmail = null,
            contactPhone = null,
        )

        // Act
        val results = useCase(provider).toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        assertEquals("Smart", repository.lastCreatedProvider?.name)
    }

    // endregion
}
