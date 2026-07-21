package com.puregoldgo.ibms.ui.screen.sysadmin

import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.StoreType
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SysadminMappersTest {

    private fun account(
        id: String = "acc-1",
        storeId: String = "st-1",
        providerId: String = "prv-1",
        rate: String = "1500.00",
        status: AccountStatus = AccountStatus.ACTIVE,
    ) = Account(
        id = id,
        accountNumber = "ACC-$id",
        providerId = providerId,
        storeId = storeId,
        rate = rate,
        status = status,
    )

    private fun store(
        id: String = "st-1",
        name: String = "SHAW",
        city: String? = "Mandaluyong City",
        status: StoreStatus = StoreStatus.ACTIVE,
    ) = Store(
        id = id,
        storeType = StoreType.PUREGOLD,
        branchCode = "155",
        name = name,
        city = city,
        status = status,
    )

    private fun provider(
        id: String = "prv-1",
        name: String = "Globe",
        status: ProviderStatus = ProviderStatus.ACTIVE,
    ) = Provider(
        id = id,
        name = name,
        paymentScheduleDay = 5,
        status = status,
    )

    // region formatMoney

    @Test
    fun tc01_should_group_thousands() {
        assertEquals("2,489.99", formatMoney("2489.99"))
    }

    @Test
    fun tc02_should_leave_values_below_a_thousand_alone() {
        assertEquals("999.00", formatMoney("999.00"))
    }

    @Test
    fun tc03_should_group_every_three_digits() {
        assertEquals("1,234,567.89", formatMoney("1234567.89"))
    }

    @Test
    fun tc04_should_add_a_fraction_when_the_server_omits_one() {
        assertEquals("1,500.00", formatMoney("1500"))
    }

    @Test
    fun tc05_should_preserve_a_leading_group_of_one_digit() {
        assertEquals("1,000.00", formatMoney("1000.00"))
    }

    @Test
    fun tc06_should_return_a_malformed_value_untouched() {
        // Reformatting nonsense into something plausible would hide the problem.
        assertEquals("N/A", formatMoney("N/A"))
        assertEquals("12.3.4", formatMoney("12.3.4"))
    }

    @Test
    fun tc07_should_keep_the_sign_on_a_negative() {
        assertEquals("-1,200.50", formatMoney("-1200.50"))
    }

    @Test
    fun tc08_should_return_blank_input_unchanged() {
        assertEquals("", formatMoney("   "))
    }

    // endregion

    // region providerIdsByStore

    @Test
    fun tc11_should_collect_every_provider_at_a_store() {
        val accounts = listOf(
            account(id = "a", storeId = "st-1", providerId = "globe"),
            account(id = "b", storeId = "st-1", providerId = "pldt"),
            account(id = "c", storeId = "st-2", providerId = "globe"),
        )

        val index = providerIdsByStore(accounts)

        assertEquals(setOf("globe", "pldt"), index["st-1"])
        assertEquals(setOf("globe"), index["st-2"])
    }

    @Test
    fun tc12_should_deduplicate_repeat_providers_at_one_store() {
        val accounts = listOf(
            account(id = "a", storeId = "st-1", providerId = "globe"),
            account(id = "b", storeId = "st-1", providerId = "globe"),
        )

        assertEquals(setOf("globe"), providerIdsByStore(accounts)["st-1"])
    }

    // endregion

    // region Account -> IspAccountRow

    @Test
    fun tc21_should_resolve_the_store_name() {
        val row = account(storeId = "st-1").toRow(mapOf("st-1" to "SHAW"))

        assertEquals("SHAW", row.storeName)
    }

    @Test
    fun tc22_should_keep_an_account_whose_store_is_missing() {
        // A floating account is precisely the row a sysadmin needs to see.
        val row = account(storeId = "gone").toRow(mapOf("st-1" to "SHAW"))

        assertEquals("—", row.storeName)
    }

    @Test
    fun tc23_should_mark_a_non_active_account_inactive() {
        val row = account(status = AccountStatus.TERMINATED).toRow(emptyMap())

        assertFalse(row.isActive)
    }

    @Test
    fun tc24_should_format_the_rate_for_display() {
        val row = account(rate = "2489.99").toRow(emptyMap())

        assertEquals("2,489.99", row.monthlyRate)
    }

    // endregion

    // region Store -> BranchRow

    @Test
    fun tc31_should_carry_the_supplied_provider_ids() {
        val row = store().toRow(setOf("globe", "pldt"))

        assertEquals(setOf("globe", "pldt"), row.providerIds)
    }

    @Test
    fun tc32_should_mark_a_closed_store_inactive() {
        val row = store(status = StoreStatus.CLOSED).toRow(emptySet())

        assertFalse(row.isActive)
    }

    @Test
    fun tc33_should_file_a_store_under_its_initial() {
        assertEquals('S', store(name = "SHAW").toRow(emptySet()).indexLetter)
    }

    @Test
    fun tc34_should_file_a_numeric_name_under_hash() {
        // Otherwise "888 CHINA TOWN SQUARE" is unreachable from the A–Z rail.
        assertEquals('#', store(name = "888 CHINA TOWN SQUARE").toRow(emptySet()).indexLetter)
    }

    @Test
    fun tc35_should_survive_a_store_with_no_city() {
        val row = store(city = null).toRow(emptySet())

        assertTrue(row.city == null)
    }

    // endregion

    // region Provider -> IspProviderRow

    @Test
    fun tc41_should_keep_only_active_providers() {
        // Nothing in this app can deactivate a provider, so the panel lists the
        // active ones and has no section an inactive one could appear in.
        val rows = listOf(
            provider(id = "prv-1", status = ProviderStatus.ACTIVE),
            provider(id = "prv-2", status = ProviderStatus.INACTIVE),
        ).activeRows()

        assertEquals(listOf("prv-1"), rows.map { it.id })
    }

    // endregion

    // region UserProfile -> DirectoryUser

    @Test
    fun tc51_should_carry_the_fields_the_row_draws() {
        val row = UserProfile(
            id = "usr-1",
            username = "rlim",
            name = "Rosario D Lim",
            employeeNumber = "010007422",
            role = Role.PENDING,
            status = UserStatus.ACTIVE,
            mustChangePassword = true,
        ).toRow()

        assertEquals("rlim", row.username)
        assertEquals("010007422", row.employeeNumber)
        assertEquals(Role.PENDING, row.role)
        assertTrue(row.isActive)
        // Drives the TEMPORARY chip, and is the only trace a temporary password
        // leaves once the dialog that showed it is gone.
        assertTrue(row.mustChangePassword)
    }

    @Test
    fun tc52_should_mark_a_deactivated_user_inactive() {
        val row = UserProfile(
            id = "usr-1",
            username = "garciaga",
            name = "Gilbert Arciaga",
            role = Role.SECRETARY,
            status = UserStatus.INACTIVE,
            mustChangePassword = false,
        ).toRow()

        assertFalse(row.isActive)
    }

    @Test
    fun tc53_should_fall_back_to_the_username_for_a_blank_name() {
        val row = UserProfile(
            id = "usr-1",
            username = "jdoe",
            name = "",
            role = Role.PENDING,
            mustChangePassword = false,
        ).toRow()

        assertEquals("J", row.initial)
    }

    // endregion
}
