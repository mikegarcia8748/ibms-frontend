package com.puregoldgo.ibms.shared.model

import kotlinx.serialization.Serializable

/**
 * Request body for `POST /accounts`.
 *
 * Money travels as a decimal string to preserve precision across platforms.
 * [subscriptionProofIds] must contain at least one attachment id uploaded via
 * the presigned URL flow before the account is created.
 *
 * [isProrated] is deliberately absent: the backend derives it from
 * [installationDate] and the topsheet processing date.
 */
@Serializable
data class CreateAccountRequest(
    val accountNumber: String,
    val providerId: String,
    val storeId: String,
    val rate: String,
    val installationDate: String,
    val circuitId: String,
    val billingPeriodLabel: String? = null,
    val planName: String? = null,
    val subscriptionProofIds: List<String>,
)
