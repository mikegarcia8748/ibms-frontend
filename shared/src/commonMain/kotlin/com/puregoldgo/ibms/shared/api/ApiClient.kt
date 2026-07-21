package com.puregoldgo.ibms.shared.api

import com.puregoldgo.ibms.shared.model.*

/**
 * Typed API client interface — contracts for all REST endpoints.
 * Implementations:
 *   - :composeApp provides a Ktor Client-based implementation.
 *   - :backend can use it in integration tests.
 */
interface IbmsApiClient {

    // ─── Auth ────────────────────────────────────────────────────────────────
    suspend fun authenticate(username: String, password: String): ApiEnvelope<LoginResponse>
    suspend fun completePasswordChange(newPassword: String): ApiEnvelope<LoginResponse>
    // Refresh answers with a full LoginResponse, not just the tokens: the rotated
    // session arrives with the current user attached, so a resumed launch needs
    // no second call to learn who is signed in.
    suspend fun refresh(refreshToken: String): ApiEnvelope<LoginResponse>
    suspend fun logout(): ApiEnvelope<String?>

    // ─── Users ───────────────────────────────────────────────────────────────
    suspend fun getCurrentUser(): UserProfile
    suspend fun listUsers(): List<UserProfile>
    suspend fun updateUserRole(userId: String, role: Role): UserProfile

    // ─── Providers ───────────────────────────────────────────────────────────
    suspend fun listProviders(): List<Provider>
    suspend fun createProvider(provider: Provider): Provider
    suspend fun updateProvider(provider: Provider): Provider

    // ─── Stores ──────────────────────────────────────────────────────────────
    suspend fun listStores(): List<Store>
    suspend fun createStore(store: Store): Store
    suspend fun updateStore(store: Store): Store
    suspend fun deactivateStore(storeId: String): Store

    // ─── Accounts ────────────────────────────────────────────────────────────
    suspend fun listAccounts(storeId: String? = null, providerId: String? = null): List<Account>
    suspend fun createAccount(account: Account): Account
    suspend fun updateAccount(account: Account): Account
    suspend fun deactivateAccount(accountId: String): Account

    // ─── Attachments ─────────────────────────────────────────────────────────
    suspend fun getUploadUrl(fileName: String, contentType: String): PresignedUrlResponse
    suspend fun getDownloadUrl(attachmentId: String): PresignedUrlResponse

    // ─── Topsheets ───────────────────────────────────────────────────────────
    suspend fun previewTopsheet(providerId: String, period: String): TopsheetPreview
    suspend fun compileTopsheet(providerId: String, period: String): Topsheet
    suspend fun approveTopsheet(topsheetId: String): Topsheet
    suspend fun payTopsheet(topsheetId: String): Topsheet
    suspend fun listTopsheets(providerId: String? = null, status: TopsheetStatus? = null): List<Topsheet>
    suspend fun getTopsheetDetails(topsheetId: String): List<TopsheetDetail>

    // ─── Transfers ───────────────────────────────────────────────────────────
    suspend fun createTransfer(transfer: Transfer): Transfer
    suspend fun listTransfers(accountId: String? = null): List<Transfer>

    // ─── OCR ─────────────────────────────────────────────────────────────────
    suspend fun extractOcr(batchId: String): OcrBatch
    suspend fun listOcrBatches(): List<OcrBatch>
    suspend fun getOcrRows(batchId: String): List<OcrExtractedRow>
    suspend fun listOcrTemplates(): List<OcrTemplate>
    suspend fun createOcrTemplate(template: OcrTemplate): OcrTemplate
    suspend fun updateOcrTemplate(template: OcrTemplate): OcrTemplate

    // ─── Activities ──────────────────────────────────────────────────────────
    suspend fun listActivities(entityId: String? = null, limit: Int = 50): List<Activity>

    // ─── Exports ─────────────────────────────────────────────────────────────
    suspend fun getExportUrl(topsheetId: String): String
}

// ─── Envelope & Response DTOs ───────────────────────────────────────────────

@kotlinx.serialization.Serializable
data class ApiEnvelope<T>(
    val result: String,
    val message: String,
    val status: String,
    val data: T? = null,
)

// ─── Auth DTOs ──────────────────────────────────────────────────────────────
// Mirrors ibms-backend/src/domain/model/DomainModels.kt (Authentication section).
//
// Accounts are provisioned by a sysadmin, never self-registered. Holding a
// temporary password is NOT being authenticated: logging in with one returns a
// change-password challenge and `session = null`, and only a successful
// password change mints tokens.

@kotlinx.serialization.Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@kotlinx.serialization.Serializable
enum class LoginOutcome {
    /** Credentials accepted and a session exists — `session` is populated. */
    @kotlinx.serialization.SerialName("authenticated")
    AUTHENTICATED,

    /** Temporary password accepted — `passwordChange` is populated, no session yet. */
    @kotlinx.serialization.SerialName("password_change_required")
    PASSWORD_CHANGE_REQUIRED,
}

/**
 * Returned by both `POST /auth/login` and `POST /auth/password/change`.
 *
 * [user] is populated even when [session] is null — that is, *while the caller
 * is still unauthenticated*. Authenticated means `session != null`; never branch
 * on the presence of [user].
 */
@kotlinx.serialization.Serializable
data class LoginResponse(
    val outcome: LoginOutcome,
    val user: UserProfile,
    val session: SessionTokens? = null,
    val passwordChange: PasswordChangeChallenge? = null,
)

@kotlinx.serialization.Serializable
data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresInSeconds: Long,
)

/**
 * A single-purpose token authorizing exactly one call to
 * `POST /auth/password/change`. It is not a bearer token and is rejected
 * everywhere else. Must never be persisted — it lives only in in-memory UI state.
 */
@kotlinx.serialization.Serializable
data class PasswordChangeChallenge(
    val challengeToken: String,
    val expiresInSeconds: Long,
    val reason: String,
)

/** Redeems a change-password challenge; authorized by the challenge token. */
@kotlinx.serialization.Serializable
data class ChangePasswordRequest(
    val newPassword: String,
)

@kotlinx.serialization.Serializable
data class RefreshRequest(
    val refreshToken: String,
)

// ─── Provider DTOs ──────────────────────────────────────────────────────────

/** Body for `POST /providers`. */
@kotlinx.serialization.Serializable
data class CreateProviderRequest(
    val name: String,
    val paymentScheduleDay: Int,
)

/**
 * Body for `PUT /providers/{id}`.
 *
 * Both fields are nullable and omitted-when-null, which is the endpoint's way of
 * saying "leave this alone". The client `Json` is configured with
 * `encodeDefaults = true`, so a null here is written as an explicit `null` — the
 * backend treats that the same as absent.
 */
@kotlinx.serialization.Serializable
data class UpdateProviderRequest(
    val name: String? = null,
    val paymentScheduleDay: Int? = null,
)

@kotlinx.serialization.Serializable
data class PresignedUrlResponse(
    val url: String,
    val attachmentId: String? = null,
)

@kotlinx.serialization.Serializable
data class TopsheetPreview(
    val providerId: String,
    val period: String,
    val lines: List<TopsheetDetail>,
    val totalAmount: String,
    val lineCount: Int,
)

/**
 * What `POST /accounts/bulk-import` answers with.
 *
 * The import is idempotent, so every count comes in a created/reused pair: a
 * second upload of the same spreadsheet reports `0 created, N reused` and that
 * is a success, not a no-op to apologise for.
 *
 * [providers] is a list, not a single name: one spreadsheet routinely carries
 * rows for several ISPs, and each is matched or created independently.
 *
 * Every field defaults rather than being required. A response that omits a key
 * must degrade to a zero, not fail the whole parse and turn a completed import
 * into an error message.
 */
@kotlinx.serialization.Serializable
data class BulkImportSummaryResponse(
    val providers: List<ProviderImportSummaryResponse> = emptyList(),
    val storesCreated: Int = 0,
    val storesReused: Int = 0,
    val accountsCreated: Int = 0,
    val accountsReused: Int = 0,
    val rowsSkipped: Int = 0,
    val skipReasons: List<String> = emptyList(),
    val totalRows: Int = 0,
)

/** One ISP's share of an import. Sorted by name by the backend. */
@kotlinx.serialization.Serializable
data class ProviderImportSummaryResponse(
    val name: String,
    val created: Boolean = false,
    val accountsCreated: Int = 0,
    val accountsReused: Int = 0,
)
