package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.model.ProviderStatus
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
        assertIs<Resource.Failed<*>>(results.last())
        assertEquals("Network error", (results.last() as Resource.Failed<*>).message)
    }

    @Test
    fun tc02_should_fail_the_whole_walk_when_a_later_page_fails() = runTest {
        // Arrange — nothing partial should surface as a success
        repository.providers = ProviderTestData.createProviderList(5)
        repository.pageSize = 2
        repository.shouldFail = true

        // Act
        val results = useCase().toList()

        // Assert
        assertIs<Resource.Failed<*>>(results.last())
        assertTrue(results.filterIsInstance<Resource.Success<*>>().isEmpty())
    }

    // endregion

    // region Happy Path Tests (tc11+)

    @Test
    fun tc11_should_return_success_with_provider_list() = runTest {
        // Arrange
        repository.providers = ProviderTestData.createProviderList()

        // Act
        val results = useCase().toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<List<*>>>()
        assertTrue(success.isNotEmpty())
        assertEquals(3, success.first().data?.size ?: 0)
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
        assertTrue(success.first().data?.isEmpty() ?: true)
    }

    @Test
    fun tc13_should_emit_loading_exactly_once_before_success() = runTest {
        // Arrange — three pages must not mean three spinners
        repository.providers = ProviderTestData.createProviderList(6)
        repository.pageSize = 2

        // Act
        val results = useCase().toList()

        // Assert
        assertEquals(1, results.count { it is Resource.Loading })
        assertIs<Resource.Success<*>>(results.last())
    }

    @Test
    fun tc14_should_walk_every_page_until_the_cursor_runs_out() = runTest {
        // Arrange
        repository.providers = ProviderTestData.createProviderList(5)
        repository.pageSize = 2

        // Act
        val results = useCase().toList()

        // Assert — 5 records at 2 a page is three requests, and all 5 come back
        val success = results.filterIsInstance<Resource.Success<List<*>>>()
        assertEquals(5, success.first().data?.size ?: 0)
        assertEquals(listOf(null, "2", "4"), repository.requestedCursors)
    }

    @Test
    fun tc15_should_pass_the_status_filter_through() = runTest {
        // Arrange
        repository.providers = listOf(
            ProviderTestData.createProvider(id = "a", status = ProviderStatus.ACTIVE),
            ProviderTestData.createProvider(id = "b", status = ProviderStatus.INACTIVE),
        )

        // Act
        val results = useCase(status = ProviderStatus.ACTIVE).toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<List<*>>>()
        assertEquals(1, success.first().data?.size ?: 0)
    }

    // endregion
}
