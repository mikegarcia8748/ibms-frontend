package com.puregoldgo.ibms.ui.screen.sysadmin

import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus

/**
 * Turns API providers into the rows both panels draw.
 *
 * The panel-specific mappers live with their panels — see
 * `directory/DirectoryMappers.kt` and `registry/RegistryMappers.kt`.
 */

internal fun Provider.toRow() = IspProviderRow(
    id = id,
    name = name,
    paymentScheduleDay = paymentScheduleDay,
)

/**
 * The providers the console draws.
 *
 * Inactive ones are dropped rather than listed separately: nothing in this app
 * can deactivate a provider, so a section for them could only ever be empty.
 */
internal fun List<Provider>.activeRows(): List<IspProviderRow> =
    filter { it.status == ProviderStatus.ACTIVE }.map { it.toRow() }
