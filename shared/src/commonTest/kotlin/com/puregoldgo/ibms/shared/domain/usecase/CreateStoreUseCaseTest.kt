package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.model.StoreType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CreateStoreUseCaseTest {

    private lateinit var repository: FakeStoreRepository
    private lateinit var useCase: CreateStoreUseCase

    @BeforeTest
    fun setup() {
        repository = FakeStoreRepository()
        useCase = CreateStoreUseCase(repository)
    }

    // region Negative Tests

    @Test
    fun should_return_failed_when_branch_code_is_blank() = runTest {
        // Act
        val results = useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = "",
            name = "Alapan 1B",
        ).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Branch code is required", failed.first().message)
    }

    @Test
    fun should_return_failed_when_branch_name_is_blank() = runTest {
        // Act
        val results = useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = "ALP",
            name = "",
        ).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Branch name is required", failed.first().message)
    }

    @Test
    fun should_not_reach_the_repository_when_validation_fails() = runTest {
        // Act
        useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = " ",
            name = "Alapan 1B",
        ).toList()

        // Assert
        assertNull(repository.lastCreated)
    }

    @Test
    fun should_return_failed_when_repository_fails() = runTest {
        // Arrange
        repository.shouldFail = true
        repository.failureMessage = "Server error"

        // Act
        val results = useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = "ALP",
            name = "Alapan 1B",
        ).toList()

        // Assert
        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Server error", failed.first().message)
    }

    // endregion

    // region Happy Path Tests

    @Test
    fun should_create_store_with_required_fields() = runTest {
        // Act
        val results = useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = "ALP",
            name = "Alapan 1B",
        ).toList()

        // Assert
        val success = results.filterIsInstance<Resource.Success<*>>()
        assertTrue(success.isNotEmpty())
        val request = repository.lastCreated
        assertEquals(StoreType.PUREGOLD, request?.storeType)
        assertEquals("ALP", request?.branchCode)
        assertEquals("Alapan 1B", request?.name)
        assertNull(request?.region)
    }

    @Test
    fun should_pass_optional_location_fields_when_present() = runTest {
        // Act
        useCase(
            storeType = StoreType.PUREMART,
            branchCode = "agd",
            name = "Aguado",
            region = "Calabarzon",
            province = "Cavite",
            city = "Trece Martires",
            barangay = "San Agustin",
            postal = "4109",
        ).toList()

        // Assert
        val request = repository.lastCreated
        assertEquals(StoreType.PUREMART, request?.storeType)
        assertEquals("AGD", request?.branchCode)
        assertEquals("Aguado", request?.name)
        assertEquals("Calabarzon", request?.region)
        assertEquals("Cavite", request?.province)
        assertEquals("Trece Martires", request?.city)
        assertEquals("San Agustin", request?.barangay)
        assertEquals("4109", request?.postal)
    }

    @Test
    fun should_trim_branch_code_and_name_before_sending() = runTest {
        // Act
        useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = "  alp  ",
            name = "  Alapan 1B  ",
        ).toList()

        // Assert
        val request = repository.lastCreated
        assertEquals("ALP", request?.branchCode)
        assertEquals("Alapan 1B", request?.name)
    }

    @Test
    fun should_drop_blank_optional_location_fields() = runTest {
        // Act
        useCase(
            storeType = StoreType.PUREGOLD,
            branchCode = "ALP",
            name = "Alapan 1B",
            region = "",
            city = "   ",
        ).toList()

        // Assert
        val request = repository.lastCreated
        assertNull(request?.region)
        assertNull(request?.city)
    }

    // endregion
}
