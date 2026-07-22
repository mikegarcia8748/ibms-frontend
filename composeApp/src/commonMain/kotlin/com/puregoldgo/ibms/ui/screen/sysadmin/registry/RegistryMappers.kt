package com.puregoldgo.ibms.ui.screen.sysadmin.registry

import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.ui.format.formatMoney

/**
 * Turns API models into the rows the registry panels draw.
 *
 * Two of these joins are the client's to make because the backend does not model
 * them: an account carries a store *id* but the list shows a store *name*, and a
 * store has no ISP at all — that relation exists only through its accounts. It
 * is also why both panels share a ViewModel; see [RegistryUIState].
 */

/** The label shown when an account points at a store that is not in the list. */
private const val UNKNOWN_STORE = "—"

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
