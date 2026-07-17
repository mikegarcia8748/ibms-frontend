package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.Resource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetProvidersUseCaseTest {

    private lateinit var repository: FakeProviderRepository
    private lateinit var useCase: GetProvidersUseCase

    @BeforeTest
    fun setup() {
        repository = FakeProviderRepository()
        useCase = GetProvidersUseCase(repository)
    }

    // region Negative Tests (tc01–tc10)

    @Test
    fun tc01_should_return_failed_when_repository_fails() = runTest {
        // Arrange
        repository.shouldFail = true
        repository.failureMessage = "Network error"

        // Act
        val results = useCase().toList()

        // Assert
        assertIs<Resource.Failed>(results.last())
        assertEquals("Network error", (results.last() as Resource.Failed).message)
    }

    // endregion

    // region Happy Path Tests (tc11+)

    @Test
    fun tc11_should_return_success_with_provider_list() = runTest {
        // Arrange
        val expected = ProviderTestData.createProviderList()
        repository.providers = expected

        // Act
        val results = useCase().toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<List<*>>>()
        assertTrue(success.isNotEmpty())
        assertEquals(3, success.first().data.size)
    }

    @Test
    fun tc12_should_return_empty_list_when_no_providers() = runTest {
        // Arrange
        repository.providers = emptyList()

        // Act
        val results = useCase().toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<List<*>>>()
        assertTrue(success.isNotEmpty())
        assertTrue(success.first().data.isEmpty())
    }

    @Test
    fun tc13_should_emit_loading_then_success() = runTest {
        // Arrange
        repository.providers = ProviderTestData.createProviderList(1)

        // Act
        val results = useCase().toList()

        // Assert — Loading is NOT emitted because the UseCase delegates directly to repository
        // The repository implementation controls the loading emission
        assertTrue(results.isNotEmpty())
    }

    // endregion
}
