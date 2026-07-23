package com.puregoldgo.ibms.shared.api

import com.puregoldgo.ibms.shared.model.TopsheetLineStatus
import com.puregoldgo.ibms.shared.model.TopsheetStatus
import kotlinx.serialization.Serializable

/**
 * DTOs for the two-phase topsheet compilation flow — preview → draft →
 * per-line RFP entry → confirm.
 *
 * These mirror the backend's own models exactly (field names are JSON keys):
 * `CompilablePreview`/`CompilableLine`, `TopSheet`, `TopSheetDetail`, and the two
 * request bodies. They are deliberately separate from the older `Topsheet` /
 * `TopsheetDetail` / `TopsheetPreview` types in this module, which predate the
 * two-phase contract and carry a different shape. Money crosses the wire as a 2dp
 * decimal string — never a Double, this is billing.
 */

/** Body for `POST /topsheets/preview` and `POST /topsheets/draft`. */
@Serializable
data class CompileRequest(
    val providerId: String,
    val billingPeriod: String, // YYYY-MM
)

/**
 * Body for `PATCH /topsheets/{id}/lines/{lineId}`.
 *
 * Both fields are optional; the client `Json` encodes defaults, so a null is
 * written explicitly and the backend reads it as "leave alone". At least one must
 * be non-null for the call to be meaningful.
 */
@Serializable
data class UpdateTopSheetLineRequest(
    val rfpNumber: String? = null,
    val proratedAmount: String? = null,
)

/**
 * Body for `POST /topsheets/{id}/assign-rfp`.
 *
 * Bulk-assigns a contiguous RFP range across a draft's lines: each distinct store
 * code claims the next number in `[startRfpNumber, endRfpNumber]`. Both are numeric
 * strings (`^\d+$`) whose leading zeros are significant, so they cross the wire as
 * strings — never Ints, which would drop the padding the RFP system expects.
 */
@Serializable
data class AssignRfpRequest(
    val startRfpNumber: String,
    val endRfpNumber: String,
)

/**
 * The header returned by `POST /topsheets/draft` and `POST /topsheets/{id}/confirm`.
 *
 * [invoiceNumber] is null during DRAFT and minted at confirm. [batchNumber] is
 * assigned at draft creation and references the compilation batch in the external
 * RFP system.
 */
@Serializable
data class TopSheetSummary(
    val id: String,
    val invoiceNumber: String? = null,
    val batchNumber: String? = null,
    val billingPeriod: String, // YYYY-MM
    val providerId: String? = null,
    val providerName: String? = null,
    val accountCount: Int,
    val totalAmount: String, // decimal-as-string
    val status: TopsheetStatus = TopsheetStatus.DRAFT,
    val compilerId: String,
    val approvedByFinanceId: String? = null,
    val approvedAt: String? = null, // ISO-8601
    val paidAt: String? = null, // ISO-8601
    val compilationDate: String, // ISO-8601
)

/**
 * One line of `GET /topsheets/{id}/lines` and the body of a line PATCH response.
 *
 * [rfpNumber] is null until the secretary enters it; [rfpSortOrder] is the
 * 1-based display order the backend assigns (store branch code descending), and
 * lines must be shown in that order so the RFP numbers align.
 */
@Serializable
data class TopSheetLine(
    val id: String,
    val topsheetId: String,
    val accountId: String,
    val billingPeriod: String, // YYYY-MM
    val proratedAmount: String, // decimal-as-string
    val fullAmount: String, // decimal-as-string (MRC)
    val status: TopsheetLineStatus = TopsheetLineStatus.BILLED,
    val branchCode: String? = null,
    val storeName: String? = null,
    val circuitId: String? = null,
    val accountNumber: String? = null,
    val accountStatus: String? = null,
    val rfpNumber: String? = null,
    val rfpSortOrder: Int? = null,
)

/** Response of `POST /topsheets/preview` — the eligible lines + total, no persistence. */
@Serializable
data class CompilePreview(
    val providerId: String,
    val billingPeriod: String, // YYYY-MM
    val lines: List<CompilePreviewLine> = emptyList(),
    val totalAmount: String, // decimal-as-string
)

/** One eligible account in a [CompilePreview]. */
@Serializable
data class CompilePreviewLine(
    val accountId: String,
    val accountNumber: String,
    val branchCode: String? = null,
    val storeName: String? = null,
    val circuitId: String? = null,
    val fullAmount: String, // decimal-as-string (MRC)
    val proratedAmount: String, // decimal-as-string
    val isProrated: Boolean = false,
    val storeId: String? = null,
)
