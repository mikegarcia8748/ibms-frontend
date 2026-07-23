package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.domain.TopSheetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Use cases for the two-phase topsheet compilation flow.
 *
 * Each returns `Flow<Resource<T>>` with the API envelope already unwrapped, so
 * the ViewModel deals in domain types and the usual `Loading → Success/Failed`
 * shape. The idempotency keys are built here rather than passed in: they are a
 * deterministic function of the request, and generating them at the call site
 * only invites two spellings of the same key.
 */

/** `POST /topsheets/preview` — eligible accounts + total, no persistence. */
class PreviewTopSheetUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(providerId: String, billingPeriod: String): Flow<Resource<CompilePreview>> =
        repository.preview(providerId, billingPeriod).unwrap()
}

/** `POST /topsheets/draft` — creates a DRAFT of all eligible accounts. */
class CreateTopSheetDraftUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(providerId: String, billingPeriod: String): Flow<Resource<TopSheetSummary>> = flow {
        emit(Resource.Loading)
        emit(
            repository.createDraft(
                providerId = providerId,
                billingPeriod = billingPeriod,
                idempotencyKey = "draft-$providerId-$billingPeriod",
            ).unwrapOnce(),
        )
    }
}

/** `GET /topsheets/{id}/lines` — all lines, sorted by rfpSortOrder ASC. */
class GetTopSheetLinesUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(topsheetId: String): Flow<Resource<List<TopSheetLine>>> =
        repository.listLines(topsheetId).unwrap()
}

/** `PATCH /topsheets/{id}/lines/{lineId}` — set the RFP number and/or prorated amount. */
class UpdateTopSheetLineUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(
        topsheetId: String,
        lineId: String,
        rfpNumber: String? = null,
        proratedAmount: String? = null,
    ): Flow<Resource<TopSheetLine>> = flow {
        emit(Resource.Loading)
        emit(repository.updateLine(topsheetId, lineId, rfpNumber, proratedAmount).unwrapOnce())
    }
}

/** `POST /topsheets/{id}/assign-rfp` — bulk-assign an RFP range across a draft's lines. */
class AssignRfpUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(
        topsheetId: String,
        startRfpNumber: String,
        endRfpNumber: String,
    ): Flow<Resource<List<TopSheetLine>>> = flow {
        emit(Resource.Loading)
        emit(repository.assignRfp(topsheetId, startRfpNumber, endRfpNumber).unwrapOnce())
    }
}

/** `DELETE /topsheets/{id}/lines/{lineId}` — removes a line from a DRAFT. */
class DeleteTopSheetLineUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(topsheetId: String, lineId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading)
        emit(repository.deleteLine(topsheetId, lineId))
    }
}

/** `POST /topsheets/{id}/confirm` — re-validate, mint the invoice, DRAFT → COMPILED. */
class ConfirmTopSheetUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(topsheetId: String): Flow<Resource<TopSheetSummary>> = flow {
        emit(Resource.Loading)
        emit(repository.confirm(topsheetId, idempotencyKey = "confirm-$topsheetId").unwrapOnce())
    }
}

// ─── Envelope unwrapping ──────────────────────────────────────────────────────

/** Maps a stream of enveloped resources to the unwrapped payload. */
private fun <T> Flow<Resource<BaseResponse<T>>>.unwrap(): Flow<Resource<T>> =
    map { it.unwrapOnce() }

/** Unwraps one enveloped resource, turning a success with no `data` into a failure. */
private fun <T> Resource<BaseResponse<T>>.unwrapOnce(): Resource<T> = when (this) {
    is Resource.Loading -> Resource.Loading
    is Resource.Success -> {
        val payload = data?.data
        if (payload != null) {
            Resource.Success(payload)
        } else {
            Resource.Failed(message = data?.message ?: "No data returned from server")
        }
    }
    is Resource.Failed -> Resource.Failed(message = message ?: data?.message)
    is Resource.Error -> Resource.Error(error)
}
