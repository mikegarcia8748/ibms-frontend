package com.puregoldgo.ibms.ui.screen.secretary

import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus

// ─── Row builders ────────────────────────────────────────────────────────────

internal fun buildProviderRows(providers: List<Provider>): List<SecretaryProviderRow> =
    providers.map { provider ->
        SecretaryProviderRow(
            id = provider.id,
            name = provider.name,
            isActive = provider.status == ProviderStatus.ACTIVE,
        )
    }

internal fun buildBranchRows(
    stores: List<Store>,
    accounts: List<Account>,
): List<SecretaryBranchRow> {
    val providersByStore = accounts.groupBy { it.storeId }
        .mapValues { (_, accts) -> accts.map { it.providerId }.toSet() }

    return stores.map { store ->
        SecretaryBranchRow(
            id = store.id,
            branchCode = store.branchCode,
            name = store.name,
            city = store.city,
            providerIds = providersByStore[store.id] ?: emptySet(),
            status = store.status.toBranchRecordStatus(),
            closureReason = store.closedReason,
        )
    }
}

internal fun buildAccountRows(
    accounts: List<Account>,
    stores: List<Store>,
): List<SecretaryAccountRow> {
    val storesById = stores.associateBy { it.id }

    return accounts.map { account ->
        val store = storesById[account.storeId]
        SecretaryAccountRow(
            id = account.id,
            accountNumber = account.accountNumber,
            storeId = account.storeId,
            storeName = store?.displayName() ?: EM_DASH,
            providerId = account.providerId,
            monthlyRate = account.rate.groupPeso(),
            status = account.status.toAccountRecordStatus(),
        )
    }
}

// ─── Detail builders ─────────────────────────────────────────────────────────

internal fun buildStoreDetail(
    store: Store,
    accounts: List<Account>,
    providers: List<Provider>,
): StoreDetail {
    val linkedAccounts = accounts
        .filter { it.storeId == store.id }
        .map { account ->
            AccountLink(
                accountId = account.id,
                accountNumber = account.accountNumber,
                circuitId = account.circuitId,
                status = account.status.toAccountRecordStatus(),
            )
        }

    return StoreDetail(
        branchCode = store.branchCode,
        name = store.name,
        status = store.status.toBranchRecordStatus(),
        region = store.region,
        province = store.province,
        city = store.city,
        barangay = store.barangay,
        postal = store.postal,
        registeredOn = store.createdAt,
        lastUpdated = store.updatedAt,
        linkedAccounts = linkedAccounts,
    )
}

internal fun buildAccountDetail(
    account: Account,
    store: Store?,
    provider: Provider?,
): AccountDetail = AccountDetail(
    accountNumber = account.accountNumber,
    providerName = provider?.name ?: EM_DASH,
    planName = account.planName,
    circuitId = account.circuitId,
    storeName = store?.displayName() ?: EM_DASH,
    branchCode = store?.branchCode ?: EM_DASH,
    monthlyRate = account.rate.groupPeso(),
    speed = account.speed,
    contractDurationMonths = account.contractDurationMonths,
    installationDate = account.installationDate,
    createdAt = account.createdAt,
    contractStartDate = account.contractStartDate,
    contractEndDate = account.contractEndDate,
    status = account.status.toAccountRecordStatus(),
)

// ─── Status mapping ──────────────────────────────────────────────────────────

private fun StoreStatus.toBranchRecordStatus(): BranchRecordStatus = when (this) {
    StoreStatus.ACTIVE -> BranchRecordStatus.Active
    StoreStatus.CLOSED -> BranchRecordStatus.Closed
    StoreStatus.INACTIVE -> BranchRecordStatus.Inactive
}

private fun AccountStatus.toAccountRecordStatus(): AccountRecordStatus = when (this) {
    AccountStatus.ACTIVE -> AccountRecordStatus.Active
    AccountStatus.TERMINATION_REQUESTED -> AccountRecordStatus.ForDeactivation
    AccountStatus.TERMINATED -> AccountRecordStatus.Terminated
    AccountStatus.TRANSFERRED -> AccountRecordStatus.Terminated
    AccountStatus.INACTIVE -> AccountRecordStatus.Terminated
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun Store.displayName(): String =
    if (city.isNullOrBlank()) name else "$name ($city)"

/** `"760172.68"` → `"760,172.68"` for display. Leaves an unparseable value as-is. */
internal fun String.groupPeso(): String {
    val cleaned = replace(",", "").trim()
    val negative = cleaned.startsWith("-")
    val digits = cleaned.trimStart('-')
    val parts = digits.split(".")
    val whole = parts[0].ifEmpty { "0" }
    val frac = parts.getOrNull(1).orEmpty().padEnd(2, '0').take(2)
    if (whole.any { !it.isDigit() }) return this
    val grouped = whole.reversed().chunked(3).joinToString(",").reversed()
    return (if (negative) "-" else "") + "$grouped.$frac"
}

private const val EM_DASH = "—"
