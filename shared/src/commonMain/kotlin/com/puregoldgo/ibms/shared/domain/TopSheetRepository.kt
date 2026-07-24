package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.TopsheetStatus
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

    /**
     * One page of `GET /topsheets` — the compiled billing history.
     *
     * A page, not the whole list: the endpoint is cursor-paginated and caps
     * `limit` at 100 server-side. Callers that need everything loop on
     * [CursorPage.nextCursor] — see [usecase.GetTopSheetsUseCase]. Omitting the
     * filters returns every topsheet (all providers, periods and statuses, DRAFT
     * included), which is what Billing History lists.
     */
    fun listTopSheets(
        providerId: String? = null,
        billingPeriod: String? = null,
        status: TopsheetStatus? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): Flow<Resource<BaseResponse<CursorPage<TopSheetSummary>>>>

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
     * `POST /topsheets/{id}/assign-rfp` — bulk-assign a contiguous RFP range across
     * a DRAFT's lines. Each distinct store code claims the next number in the range;
     * returns the lines re-sorted by store code (same shape as [listLines]).
     */
    suspend fun assignRfp(
        topsheetId: String,
        startRfpNumber: String,
        endRfpNumber: String,
    ): Resource<BaseResponse<List<TopSheetLine>>>

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
