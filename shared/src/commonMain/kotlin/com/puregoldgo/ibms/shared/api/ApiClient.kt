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
    suspend fun authenticateGoogle(idToken: String): ApiEnvelope<AuthResponse>
    suspend fun authenticate(username: String, password: String): ApiEnvelope<AuthResponse>

    // ─── Users ───────────────────────────────────────────────────────────────
    suspend fun getCurrentUser(): User
    suspend fun listUsers(): List<User>
    suspend fun updateUserRole(userId: String, role: Role): User

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

@kotlinx.serialization.Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@kotlinx.serialization.Serializable
data class AuthResponse(
    val token: String,
    val user: User,
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
