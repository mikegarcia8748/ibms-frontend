package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Immutable

@Immutable
data class StoreDetail(
    val branchCode: String,
    val name: String,
    val status: BranchRecordStatus,
    val region: String?,
    val province: String?,
    val city: String?,
    val barangay: String?,
    val postal: String?,
    val registeredOn: String?,
    val lastUpdated: String?,
    val linkedAccounts: List<AccountLink>,
)

@Immutable
data class AccountLink(
    val accountId: String,
    val accountNumber: String,
    val circuitId: String?,
    val status: AccountRecordStatus,
)

@Immutable
data class AccountDetail(
    val accountNumber: String,
    val providerName: String,
    val planName: String?,
    val circuitId: String?,
    val storeName: String,
    val branchCode: String,
    val monthlyRate: String,
    val speed: String?,
    val contractDurationMonths: Int?,
    val installationDate: String?,
    val createdAt: String?,
    val contractStartDate: String?,
    val contractEndDate: String?,
    val status: AccountRecordStatus,
)
