package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import kotlinx.coroutines.flow.Flow

/**
 * Repository for the two-phase topsheet compilation flow.
 * Implementation lives in :composeApp (data layer).
 *
 * Reads that drive a spinner return a [Flow] emitting [Resource.Loading] first;
 * the mutations are one-shot `suspend fun`s. The draft and confirm calls carry an
 * `Idempotency-Key` because a double-submit would bill an ISP twice.
 */
interface TopSheetRepository {

    /** `POST /topsheets/preview` — eligible accounts + total for a provider/period, no persistence. */
    fun preview(
        providerId: String,
        billingPeriod: String,
    ): Flow<Resource<BaseResponse<CompilePreview>>>

    /** `POST /topsheets/draft` — creates a DRAFT with all eligible accounts. Idempotent. */
    suspend fun createDraft(
        providerId: String,
        billingPeriod: String,
        idempotencyKey: String,
    ): Resource<BaseResponse<TopSheetSummary>>

    /** `GET /topsheets/{id}/lines` — all lines, sorted by rfpSortOrder ASC. */
    fun listLines(topsheetId: String): Flow<Resource<BaseResponse<List<TopSheetLine>>>>

    /** `PATCH /topsheets/{id}/lines/{lineId}` — set the RFP number and/or prorated amount. */
    suspend fun updateLine(
        topsheetId: String,
        lineId: String,
        rfpNumber: String? = null,
        proratedAmount: String? = null,
    ): Resource<BaseResponse<TopSheetLine>>

    /**
     * `DELETE /topsheets/{id}/lines/{lineId}` — removes a line from a DRAFT.
     *
     * Answers 204 with no body, so there is no envelope to deserialize:
     * [Resource.Success] carries [Unit], and a rejection (last line, not a draft)
     * surfaces as [Resource.Error] with the backend's message.
     */
    suspend fun deleteLine(topsheetId: String, lineId: String): Resource<Unit>

    /** `POST /topsheets/{id}/confirm` — re-validate, mint the invoice, DRAFT → COMPILED. Idempotent. */
    suspend fun confirm(
        topsheetId: String,
        idempotencyKey: String,
    ): Resource<BaseResponse<TopSheetSummary>>
}
