package com.puregoldgo.ibms.shared.model

import kotlinx.serialization.Serializable

// ─── Domain Models / DTOs ────────────────────────────────────────────────────
// Money fields use String to preserve decimal precision across platforms.
// Timestamps use String (ISO-8601) for serialization safety.

/**
 * The public view of a user, mirroring the backend's `UserProfile`.
 *
 * Accounts are provisioned by a sysadmin and identified by [username] — there is
 * no email and no Google identity. [mustChangePassword] is always on the wire
 * (the backend gives it no default) because a silently-omitted `false` would be
 * indistinguishable from absent, and it decides whether the user is routed into
 * the change-password screen.
 */
@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val name: String,
    val firstName: String? = null,
    val middleInitial: String? = null,
    val lastName: String? = null,
    val employeeNumber: String? = null,
    val role: Role,
    val status: UserStatus = UserStatus.ACTIVE,
    val mustChangePassword: Boolean,
)

@Serializable
data class Provider(
    val id: String,
    val name: String,
    val code: String,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class Store(
    val id: String,
    val name: String,
    val branchCode: String,
    val address: String? = null,
    val isActive: Boolean = true,
    val proofAttachmentIds: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class Account(
    val id: String,
    val providerId: String,
    val storeId: String,
    val accountNumber: String,
    val accountName: String? = null,
    val rate: String, // decimal-as-string, e.g. "1500.00"
    val installationDate: String? = null, // ISO date YYYY-MM-DD
    val status: AccountStatus = AccountStatus.ACTIVE,
    val terminationRequestedAt: String? = null,
    val proofAttachmentIds: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class Attachment(
    val id: String,
    val kind: AttachmentKind,
    val fileName: String,
    val contentType: String? = null,
    val storagePath: String, // object-storage key
    val uploadedById: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class Topsheet(
    val id: String,
    val providerId: String,
    val billingPeriod: String, // YYYY-MM
    val invoiceNumber: String? = null,
    val status: TopsheetStatus = TopsheetStatus.COMPILED,
    val totalAmount: String? = null, // decimal-as-string
    val lineCount: Int = 0,
    val compiledById: String? = null,
    val compiledAt: String? = null,
    val approvedById: String? = null,
    val approvedAt: String? = null,
    val paidById: String? = null,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class TopsheetDetail(
    val id: String,
    val topsheetId: String,
    val accountId: String,
    val billingPeriod: String, // YYYY-MM
    val rate: String, // decimal-as-string
    val activeDays: Int,
    val totalDays: Int,
    val proratedAmount: String, // decimal-as-string
    val installationDate: String? = null,
    val accountNumber: String? = null,
    val storeName: String? = null,
    val branchCode: String? = null,
)

@Serializable
data class Transfer(
    val id: String,
    val accountId: String,
    val kind: TransferKind,
    val fromStoreId: String? = null,
    val toStoreId: String? = null,
    val fromProviderId: String? = null,
    val toProviderId: String? = null,
    val newAccountNumber: String? = null,
    val proofAttachmentId: String? = null,
    val transferredById: String? = null,
    val transferredAt: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class InvoiceSequence(
    val id: String,
    val providerId: String,
    val prefix: String,
    val nextNumber: Int,
)

@Serializable
data class Activity(
    val id: String,
    val actorId: String,
    val action: ActivityAction,
    val entityType: String,
    val entityId: String,
    val description: String? = null,
    val metadata: String? = null, // JSON string for before/after
    val createdAt: String? = null,
)

@Serializable
data class EmailLog(
    val id: String,
    val recipientEmail: String,
    val subject: String,
    val templateName: String? = null,
    val status: String? = null, // sent, failed, pending
    val sentAt: String? = null,
    val errorMessage: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class OcrTemplate(
    val id: String,
    val providerName: String,
    val templateKey: String,
    val extractionSchema: String? = null, // JSON schema definition
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class OcrBatch(
    val id: String,
    val uploadedById: String,
    val templateId: String? = null,
    val fileName: String,
    val status: String? = null, // processing, completed, failed
    val rowCount: Int = 0,
    val matchedCount: Int = 0,
    val createdAt: String? = null,
)

@Serializable
data class OcrExtractedRow(
    val id: String,
    val batchId: String,
    val accountNumber: String? = null,
    val extractedAmount: String? = null, // decimal-as-string
    val matchedAccountId: String? = null,
    val varianceFlag: Boolean = false,
    val rawData: String? = null, // JSON
    val createdAt: String? = null,
)
