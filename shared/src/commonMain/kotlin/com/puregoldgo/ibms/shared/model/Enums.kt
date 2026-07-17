package com.puregoldgo.ibms.shared.model

import kotlinx.serialization.Serializable

// ─── Enums ───────────────────────────────────────────────────────────────────

@Serializable
enum class Role {
    SECRETARY, FINANCE, PAYABLES, SYSADMIN
}

@Serializable
enum class AccountStatus {
    ACTIVE, INACTIVE, TERMINATED, SUSPENDED
}

@Serializable
enum class TopsheetStatus {
    DRAFT, COMPILED, APPROVED, PAID, CANCELLED
}

@Serializable
enum class TransferKind {
    STORE_TRANSFER, PROVIDER_TRANSFER
}

@Serializable
enum class AttachmentKind {
    SUBSCRIPTION_PROOF, STORE_PROOF, TRANSFER_PROOF, TOPSHEET_EXPORT, OCR_UPLOAD, OTHER
}

@Serializable
enum class ActivityAction {
    CREATE, UPDATE, DELETE, COMPILE, APPROVE, PAY, TRANSFER, DEACTIVATE, REACTIVATE, LOGIN
}
