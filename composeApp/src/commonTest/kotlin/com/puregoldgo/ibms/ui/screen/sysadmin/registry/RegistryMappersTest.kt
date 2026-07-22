package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.StoreType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The store→account joins the backend does not model. */
class RegistryMappersTest {


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
}
