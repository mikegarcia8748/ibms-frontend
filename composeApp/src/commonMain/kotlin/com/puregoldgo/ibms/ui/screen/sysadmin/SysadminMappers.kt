package com.puregoldgo.ibms.ui.screen.sysadmin

import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.UserProfile

/**
 * Turns API models into the rows this screen draws.
 *
 * Two of these joins are the client's to make because the backend does not model
 * them: an account carries a store *id* but the list shows a store *name*, and a
 * store has no ISP at all — that relation exists only through its accounts.
 */

/** The label shown when an account points at a store that is not in the list. */
private const val UNKNOWN_STORE = "—"

internal fun Provider.toRow() = IspProviderRow(
    id = id,
    name = name,
    paymentScheduleDay = paymentScheduleDay,
)

internal fun UserProfile.toRow() = DirectoryUser(
    id = id,
    name = name,
    username = username,
    employeeNumber = employeeNumber,
    role = role,
    status = status,
    mustChangePassword = mustChangePassword,
)

/**
 * [providerIds] cannot be read off a [Store] — it is derived from the accounts
 * installed at it, so the caller has to supply the index. See
 * [providerIdsByStore].
 */
internal fun Store.toRow(providerIds: Set<String>) = BranchRow(
    id = id,
    branchCode = branchCode,
    name = name,
    city = city,
    providerIds = providerIds,
    isActive = status == StoreStatus.ACTIVE,
)

/**
 * [storeNames] maps store id to display name. A missing id is not dropped: a
 * "floating" account — one whose store was closed or never imported — is exactly
 * the row a sysadmin needs to see, so it renders with a placeholder name instead
 * of vanishing from the list.
 */
internal fun Account.toRow(storeNames: Map<String, String>) = IspAccountRow(
    id = id,
    accountNumber = accountNumber,
    storeName = storeNames[storeId] ?: UNKNOWN_STORE,
    providerId = providerId,
    monthlyRate = formatMoney(rate),
    isActive = status == AccountStatus.ACTIVE,
)

/** Which ISPs each store has an account with, keyed by store id. */
internal fun providerIdsByStore(accounts: List<Account>): Map<String, Set<String>> =
    accounts.groupBy { it.storeId }
        .mapValues { (_, forStore) -> forStore.map { it.providerId }.toSet() }

/**
 * The providers the panel draws.
 *
 * Inactive ones are dropped rather than listed separately: nothing in this app
 * can deactivate a provider, so a section for them could only ever be empty.
 */
internal fun List<Provider>.activeRows(): List<IspProviderRow> =
    filter { it.status == ProviderStatus.ACTIVE }.map { it.toRow() }

/**
 * Groups a decimal string for display: `"2489.99"` → `"2,489.99"`.
 *
 * Hand-rolled because Kotlin Multiplatform has no `String.format` and no locale
 * number formatting in common code. It works on the string rather than parsing
 * to a number on purpose — this is a billing figure, and a round trip through
 * Double could change it.
 *
 * Anything that does not look like a decimal is returned untouched. A rate that
 * arrives malformed should appear as the server sent it, not silently reformatted
 * into something plausible.
 */
internal fun formatMoney(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed

    val isNegative = trimmed.startsWith("-")
    val unsigned = trimmed.removePrefix("-")

    val dot = unsigned.indexOf('.')
    val whole = if (dot >= 0) unsigned.substring(0, dot) else unsigned
    val fraction = if (dot >= 0) unsigned.substring(dot + 1) else ""

    if (whole.isEmpty() || !whole.all { it.isDigit() }) return trimmed
    if (!fraction.all { it.isDigit() }) return trimmed

    val grouped = whole
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return buildString {
        if (isNegative) append('-')
        append(grouped)
        append('.')
        append(fraction.ifEmpty { "00" })
    }
}
