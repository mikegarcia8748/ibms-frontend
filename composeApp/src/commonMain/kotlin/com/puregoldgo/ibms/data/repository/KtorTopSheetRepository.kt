package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.DomainException
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.getMessageValue
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.api.AssignRfpRequest
import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.CompileRequest
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.api.UpdateTopSheetLineRequest
import com.puregoldgo.ibms.shared.domain.TopSheetRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ktor-based implementation of [TopSheetRepository].
 *
 * Every call is wrapped by [guard]: the generic [sendRequest] catches only
 * `Exception`, but the Wasm/JS engine surfaces a dropped fetch as a `kotlin.Error`
 * (a `Throwable`, not an `Exception`), which would otherwise escape uncaught and
 * leave the offline case unhandled — the same reason `KtorAuthRepository` catches
 * `Throwable`.
 */
class KtorTopSheetRepository(
    private val client: HttpClient,
) : TopSheetRepository {
    private val tag = "KtorTopSheetRepository"

    override fun preview(
        providerId: String,
        billingPeriod: String,
    ): Flow<Resource<BaseResponse<CompilePreview>>> = flow {
        emit(Resource.Loading)
        emit(
            guard {
                client.sendRequest<BaseResponse<CompilePreview>>(tag) {
                    method = HttpMethod.Post
                    url(ApiEndpoint.TopsheetsPreview.url(ApiConfig.baseUrl))
                    contentType(ContentType.Application.Json)
                    setBody(CompileRequest(providerId, billingPeriod))
                }
            },
        )
    }

    override suspend fun createDraft(
        providerId: String,
        billingPeriod: String,
        idempotencyKey: String,
    ): Resource<BaseResponse<TopSheetSummary>> = guard {
        client.sendRequest<BaseResponse<TopSheetSummary>>(tag) {
            method = HttpMethod.Post
            url(ApiEndpoint.TopsheetsDraft.url(ApiConfig.baseUrl))
            contentType(ContentType.Application.Json)
            // A double-submit would create a second draft (and bill twice); the
            // key makes the retry return the original.
            header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
            setBody(CompileRequest(providerId, billingPeriod))
        }
    }

    override fun listLines(topsheetId: String): Flow<Resource<BaseResponse<List<TopSheetLine>>>> = flow {
        emit(Resource.Loading)
        emit(
            guard {
                client.sendRequest<BaseResponse<List<TopSheetLine>>>(tag) {
                    method = HttpMethod.Get
                    url(ApiEndpoint.topsheetLines(ApiConfig.baseUrl, topsheetId))
                }
            },
        )
    }

    override suspend fun updateLine(
        topsheetId: String,
        lineId: String,
        rfpNumber: String?,
        proratedAmount: String?,
    ): Resource<BaseResponse<TopSheetLine>> = guard {
        client.sendRequest<BaseResponse<TopSheetLine>>(tag) {
            method = HttpMethod.Patch
            url(ApiEndpoint.topsheetLine(ApiConfig.baseUrl, topsheetId, lineId))
            contentType(ContentType.Application.Json)
            setBody(UpdateTopSheetLineRequest(rfpNumber = rfpNumber, proratedAmount = proratedAmount))
        }
    }

    override suspend fun assignRfp(
        topsheetId: String,
        startRfpNumber: String,
        endRfpNumber: String,
    ): Resource<BaseResponse<List<TopSheetLine>>> = guard {
        client.sendRequest<BaseResponse<List<TopSheetLine>>>(tag) {
            method = HttpMethod.Post
            url(ApiEndpoint.topsheetAssignRfp(ApiConfig.baseUrl, topsheetId))
            contentType(ContentType.Application.Json)
            // Not idempotency-keyed: assign is a re-runnable draft edit (like the
            // PATCH), not a money-minting step — re-sending simply re-numbers.
            setBody(AssignRfpRequest(startRfpNumber, endRfpNumber))
        }
    }

    override suspend fun deleteLine(topsheetId: String, lineId: String): Resource<Unit> = try {
        val response: HttpResponse = client.request {
            method = HttpMethod.Delete
            url(ApiEndpoint.topsheetLine(ApiConfig.baseUrl, topsheetId, lineId))
        }
        // 204 carries no envelope to parse; a rejection (last line, not a draft)
        // arrives as a 4xx whose body still holds the backend's own message.
        if (response.status.value in 200..299) {
            Resource.Success(Unit)
        } else {
            Resource.Error(
                DomainException.ApiException(
                    code = response.status.value,
                    message = getMessageValue(response.bodyAsText()) ?: "The line could not be removed.",
                ),
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Resource.Error(DomainException.ApiException(code = 0, message = e.message ?: "The line could not be removed."))
    }

    override suspend fun confirm(
        topsheetId: String,
        idempotencyKey: String,
    ): Resource<BaseResponse<TopSheetSummary>> = guard {
        client.sendRequest<BaseResponse<TopSheetSummary>>(tag) {
            method = HttpMethod.Post
            url(ApiEndpoint.topsheetConfirm(ApiConfig.baseUrl, topsheetId))
            // Confirm is safe to retry on a network timeout; the key makes the
            // retry return the already-minted invoice rather than re-running.
            header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
        }
    }

    /**
     * Runs [block], turning an un-answered request into [Resource.Error] rather
     * than an escaping `Throwable`. Cancellation is re-thrown so structured
     * concurrency stays intact.
     */
    private suspend fun <T> guard(block: suspend () -> Resource<T>): Resource<T> = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Resource.Error(DomainException.ApiException(code = 0, message = e.message ?: "Something went wrong"))
    }
}

private const val IDEMPOTENCY_KEY_HEADER = "Idempotency-Key"
