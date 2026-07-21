package com.puregoldgo.ibms.ui.screen.sysadmin

import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import kotlin.test.Test
import kotlin.test.assertEquals

/** The provider rows both panels draw. The panel-specific mappers test beside them. */
class SysadminMappersTest {

    private fun provider(
        id: String = "prv-1",
        status: ProviderStatus = ProviderStatus.ACTIVE,
    ) = Provider(
        id = id,
        name = "Globe",
        paymentScheduleDay = 5,
        status = status,
    )

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
}
