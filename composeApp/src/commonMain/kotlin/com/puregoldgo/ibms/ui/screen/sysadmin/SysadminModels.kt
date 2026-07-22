package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.runtime.Immutable

/**
 * The rows this console draws.
 *
 * Deliberately *not* the shared domain models. Those have drifted from the
 * backend and cannot express what this console shows — there is no
 * `paymentScheduleDay` on `Provider` for the `DAY 5` chip, and no `city` on
 * `Store` for a branch's location. Rather than bend the UI around models that
 * are already scheduled to be replaced, the console states what it needs and the
 * mappers supply them. `Role` and `UserStatus` are the exception: they already
 * match the backend exactly, so they are used directly.
 *
 * Most of these rows belong to one panel and live with it — see
 * `directory/DirectoryModels.kt` and `registry/RegistryModels.kt`. This file is
 * for the ones both panels draw.
 */

/**
 * An ISP.
 *
 * Shared: the directory panel lists providers beside the staff, and both
 * registry panels filter on them.
 */
@Immutable
data class IspProviderRow(
    val id: String,
    val name: String,
    /** Day of month the provider is paid on, 1..31. */
    val paymentScheduleDay: Int,
)
