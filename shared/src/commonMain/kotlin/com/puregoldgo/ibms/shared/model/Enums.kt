package com.puregoldgo.ibms.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── Enums ───────────────────────────────────────────────────────────────────
// Constants that cross the wire carry an explicit @SerialName matching the
// backend. kotlinx defaults the serial name to the constant name ("SYSADMIN"),
// but the API emits lowercase ("sysadmin") — without these the response fails
// to deserialize outright rather than degrading gracefully.
// Source of truth: ibms-backend/src/domain/model/DomainModels.kt

@Serializable
enum class Role {
    @SerialName("sysadmin") SYSADMIN,
    @SerialName("secretary") SECRETARY,

    /**
     * Retired. Kept only so responses still deserialize.
     *
     * The role does the finance job with a slice of the secretary's — it may
     * create and update accounts — and that split has no owner. It is no longer
     * offered when provisioning a user (see `assignableRoles`) and existing
     * holders land on the finance console, so it drains as sysadmins move
     * people off it.
     *
     * It cannot simply be deleted here: `payables` is a value in the backend's
     * `user_role` Postgres enum, it travels in JWT payloads, and
     * `AccountController` authorizes `POST /accounts` and `PUT /accounts/{id}`
     * for it. Dropping the constant would make any response naming a payables
     * user fail to deserialize outright — their sign-in, and the whole
     * `GET /users` list behind the sysadmin directory.
     *
     * TODO: remove once the backend migration lands — reassign remaining
     *  holders to `finance`, drop the enum value, re-authorize the two account
     *  endpoints. This constant and its `role_payables` label go with it.
     */
    @SerialName("payables") PAYABLES,

    @SerialName("finance") FINANCE,
    @SerialName("manager") MANAGER,

    /** Default for a freshly provisioned account — carries no permissions yet. */
    @SerialName("pending") PENDING,
}

/**
 * The value this constant travels as, read back off its own serializer instead
 * of being restated in a `when`. Needed because `:core` stores the signed-in
 * role as a plain string — it sits below this module and cannot see [Role].
 */
val Role.wireValue: String
    get() = Role.serializer().descriptor.getElementName(ordinal)

/**
 * The inverse of [wireValue] — the constant a stored wire value names, or null
 * when it names none.
 *
 * Needed because `CurrentUserStore.role` in `:core` is a plain string, so every
 * decision made on the signed-in role has to come back through here. Null rather
 * than a default: a value this app does not recognise is one the backend added
 * without it, and guessing a role for it would be the one guess that matters.
 */
fun roleFromWire(wire: String?): Role? =
    Role.entries.firstOrNull { it.wireValue == wire }

/**
 * How an enum constant travels as a *query parameter* — `?status=active`.
 *
 * Deliberately the constant name lowercased rather than the `@SerialName`: the
 * backend parses these with `enumValueOf(value.uppercase())`, so it is matching
 * the Kotlin name, not the serial name. The two agree today; this stays correct
 * if one of them ever drifts.
 */
val Enum<*>.queryValue: String
    get() = name.lowercase()

@Serializable
enum class UserStatus {
    @SerialName("active") ACTIVE,
    @SerialName("inactive") INACTIVE,
}

@Serializable
enum class StoreType {
    @SerialName("puregold") PUREGOLD,
    @SerialName("puremart") PUREMART,
}

@Serializable
enum class StoreStatus {
    @SerialName("active") ACTIVE,
    @SerialName("closed") CLOSED,
    @SerialName("inactive") INACTIVE,
}

@Serializable
enum class ProviderStatus {
    @SerialName("active") ACTIVE,
    @SerialName("inactive") INACTIVE,
}

@Serializable
enum class AccountStatus {
    @SerialName("active") ACTIVE,
    @SerialName("termination_requested") TERMINATION_REQUESTED,
    @SerialName("terminated") TERMINATED,
    @SerialName("transferred") TRANSFERRED,
    @SerialName("inactive") INACTIVE,
}

@Serializable
enum class TopsheetStatus {
    @SerialName("compiled") COMPILED,
    @SerialName("approved") APPROVED,
    @SerialName("paid") PAID,
}

@Serializable
enum class TopsheetLineStatus {
    @SerialName("billed") BILLED,
    @SerialName("paid") PAID,
}

// ─── Scaffold ────────────────────────────────────────────────────────────────
// The following have no backend counterpart yet and are not exchanged with the
// API. They need the same @SerialName treatment — and their constants need
// checking against the backend — at the point those features are actually wired.

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
