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

    /** A form that passes every rule, so each test varies exactly one thing. */
    private fun provision(
        username: String = "jdoe",
        firstName: String = "Jane",
        lastName: String = "Doe",
        employeeNumber: String = "010005529",
        middleInitial: String = "",
        role: Role = Role.PENDING,
    ) = useCase(
        username = username,
        firstName = firstName,
        lastName = lastName,
        employeeNumber = employeeNumber,
        middleInitial = middleInitial,
        role = role,
    )

    // region Negative Tests (tc01–tc10)

    @Test
    fun tc01_should_return_failed_when_first_name_is_blank() = runTest {
        val results = provision(firstName = "").toList()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("First name is required", failed.first().message)
    }

    @Test
    fun tc02_should_return_failed_when_username_is_too_short() = runTest {
        val results = provision(username = "jd").toList()

        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }

    @Test
    fun tc03_should_return_failed_when_username_has_an_illegal_character() = runTest {
        val results = provision(username = "jane doe").toList()

        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
    }

    @Test
    fun tc04_should_not_reach_the_repository_when_validation_fails() = runTest {
        provision(username = "jd").toList()

        // A rejected form must not cost a round trip — or half-create an account.
        assertNull(repository.lastProvisioned)
    }

    @Test
    fun tc05_should_return_failed_when_repository_fails() = runTest {
        repository.shouldFail = true
        repository.failureMessage = "username 'jdoe' is already taken"

        val results = provision().toList()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        // The backend's own wording, not house copy — it names the field to fix.
        assertEquals("username 'jdoe' is already taken", failed.first().message)
    }

    @Test
    fun tc06_should_return_failed_when_last_name_is_blank() = runTest {
        val results = provision(lastName = "  ").toList()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Last name is required", failed.first().message)
    }

    @Test
    fun tc07_should_return_failed_when_employee_number_is_blank() = runTest {
        // The column is nullable and unindexed, so nothing downstream would
        // catch this — and there is no endpoint to add one later.
        val results = provision(employeeNumber = "   ").toList()

        val failed = results.filterIsInstance<Resource.Failed<*>>()
        assertTrue(failed.isNotEmpty())
        assertEquals("Employee number is required", failed.first().message)
        assertNull(repository.lastProvisioned)
    }

    @Test
    fun tc08_should_return_failed_when_employee_number_is_not_digits() = runTest {
        val results = provision(employeeNumber = "EMP-0001").toList()

        assertTrue(results.filterIsInstance<Resource.Failed<*>>().isNotEmpty())
        assertNull(repository.lastProvisioned)
    }

    // endregion

    // region Happy Path Tests (tc11+)

    @Test
    fun tc11_should_return_the_temporary_password_on_success() = runTest {
        val results = provision().toList()

        val provisioned = results.filterIsInstance<Resource.Success<ProvisionedUser>>()
            .first().data
        assertEquals(UserTestData.TEMPORARY_PASSWORD, provisioned?.temporaryPassword)
        // The account is created holding it — this is what the row's TEMPORARY
        // chip reads, and what clears when the user sets their own.
        assertEquals(true, provisioned?.user?.mustChangePassword)
    }

    @Test
    fun tc12_should_normalise_the_username_before_sending_it() = runTest {
        provision(username = "  M.Garcia  ").toList()

        // The backend lowercases before checking uniqueness; sending the raw
        // string would collide on an account the admin could not see coming.
        assertEquals("m.garcia", repository.lastProvisioned?.username)
    }

    @Test
    fun tc13_should_compose_the_display_name_from_its_parts() = runTest {
        provision(firstName = " Michael ", middleInitial = "p", lastName = " Garcia ").toList()

        assertEquals("Michael P. Garcia", repository.lastProvisioned?.name)
    }

    @Test
    fun tc14_should_send_the_name_parts_alongside_the_composed_name() = runTest {
        // These columns were left null by the form this replaced; the whole
        // point of the split is that they arrive populated.
        provision(firstName = "Michael", middleInitial = "P.", lastName = "Garcia").toList()

        val sent = repository.lastProvisioned
        assertEquals("Michael", sent?.firstName)
        assertEquals("P.", sent?.middleInitial)
        assertEquals("Garcia", sent?.lastName)
    }

    @Test
    fun tc15_should_omit_a_middle_initial_that_was_not_given() = runTest {
        provision(middleInitial = "").toList()

        // Null is "no middle initial"; an empty string is a stored empty one.
        assertNull(repository.lastProvisioned?.middleInitial)
        assertEquals("Jane Doe", repository.lastProvisioned?.name)
    }

    @Test
    fun tc16_should_keep_the_leading_zero_on_an_employee_number() = runTest {
        provision(employeeNumber = " 010005529 ").toList()

        // Text, not a number: parsed as one it would come back as 10005529 and
        // stop matching the payroll record.
        assertEquals("010005529", repository.lastProvisioned?.employeeNumber)
    }

    @Test
    fun tc17_should_default_to_no_role_yet() = runTest {
        provision().toList()

        assertEquals(Role.PENDING, repository.lastProvisioned?.role)
    }

    @Test
    fun tc18_should_pass_the_chosen_role_through() = runTest {
        provision(role = Role.PAYABLES).toList()

        assertEquals(Role.PAYABLES, repository.lastProvisioned?.role)
    }

    // endregion
}
