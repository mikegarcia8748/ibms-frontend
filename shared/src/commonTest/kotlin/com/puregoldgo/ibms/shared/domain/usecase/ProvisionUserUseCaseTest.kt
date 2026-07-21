package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.ProvisionedUser
import com.puregoldgo.ibms.shared.model.Role
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProvisionUserUseCaseTest {

    private lateinit var repository: FakeUserRepository
    private lateinit var useCase: ProvisionUserUseCase

    @BeforeTest
    fun setup() {
        repository = FakeUserRepository()
        useCase = ProvisionUserUseCase(repository)
    }

    // region Negative Tests (tc01–tc10)

    @Test
    fun tc01_should_return_failed_when_name_is_blank() = runTest {
        val results = useCase(username = "jdoe", name = "").toList()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Full name is required", failed.first().message)
    }

    @Test
    fun tc02_should_return_failed_when_username_is_too_short() = runTest {
        val results = useCase(username = "jd", name = "Jane Doe").toList()

        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }

    @Test
    fun tc03_should_return_failed_when_username_has_an_illegal_character() = runTest {
        val results = useCase(username = "jane doe", name = "Jane Doe").toList()

        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }

    @Test
    fun tc04_should_not_reach_the_repository_when_validation_fails() = runTest {
        useCase(username = "jd", name = "Jane Doe").toList()

        // A rejected form must not cost a round trip — or half-create an account.
        assertNull(repository.lastProvisioned)
    }

    @Test
    fun tc05_should_return_failed_when_repository_fails() = runTest {
        repository.shouldFail = true
        repository.failureMessage = "username 'jdoe' is already taken"

        val results = useCase(username = "jdoe", name = "Jane Doe").toList()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        // The backend's own wording, not house copy — it names the field to fix.
        assertEquals("username 'jdoe' is already taken", failed.first().message)
    }

    // endregion

    // region Happy Path Tests (tc11+)

    @Test
    fun tc11_should_return_the_temporary_password_on_success() = runTest {
        val results = useCase(username = "jdoe", name = "Jane Doe").toList()

        val provisioned = results.filterIsInstance<Resource.Success<ProvisionedUser>>()
            .first().data
        assertEquals(UserTestData.TEMPORARY_PASSWORD, provisioned?.temporaryPassword)
        // The account is created holding it — this is what the row's TEMPORARY
        // chip reads, and what clears when the user sets their own.
        assertEquals(true, provisioned?.user?.mustChangePassword)
    }

    @Test
    fun tc12_should_normalise_the_username_before_sending_it() = runTest {
        useCase(username = "  M.Garcia  ", name = "Michael Garcia").toList()

        // The backend lowercases before checking uniqueness; sending the raw
        // string would collide on an account the admin could not see coming.
        assertEquals("m.garcia", repository.lastProvisioned?.username)
    }

    @Test
    fun tc13_should_trim_the_name_before_sending_it() = runTest {
        useCase(username = "jdoe", name = "  Jane Doe  ").toList()

        assertEquals("Jane Doe", repository.lastProvisioned?.name)
    }

    @Test
    fun tc14_should_send_a_blank_employee_number_as_absent() = runTest {
        useCase(username = "jdoe", name = "Jane Doe", employeeNumber = "   ").toList()

        // Null is "no employee number"; an empty string is a stored empty one.
        assertNull(repository.lastProvisioned?.employeeNumber)
    }

    @Test
    fun tc15_should_default_to_no_role_yet() = runTest {
        useCase(username = "jdoe", name = "Jane Doe").toList()

        assertEquals(Role.PENDING, repository.lastProvisioned?.role)
    }

    @Test
    fun tc16_should_pass_the_chosen_role_through() = runTest {
        useCase(username = "jdoe", name = "Jane Doe", role = Role.PAYABLES).toList()

        assertEquals(Role.PAYABLES, repository.lastProvisioned?.role)
    }

    // endregion
}
