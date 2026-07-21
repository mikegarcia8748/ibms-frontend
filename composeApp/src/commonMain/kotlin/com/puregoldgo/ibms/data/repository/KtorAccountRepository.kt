package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.queryValue
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Ktor-based implementation of [AccountRepository].
 * Calls the IBMS backend REST API.
 */
class KtorAccountRepository(
    private val client: HttpClient,
) : AccountRepository {
    private val tag = "KtorAccountRepository"

    override fun listAccounts(
        providerId: String?,
        storeId: String?,
        status: AccountStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Account>>>> = flow {
        emit(Resource.Loading)

        emit(
            client.sendRequest<BaseResponse<CursorPage<Account>>>(tag) {
                method = HttpMethod.Get
                url(ApiEndpoint.Accounts.url(ApiConfig.baseUrl))
                // `parameter` drops nulls, so an unset filter is an absent query
                // key rather than `?status=null`.
                parameter("providerId", providerId)
                parameter("storeId", storeId)
                parameter("status", status?.queryValue)
                parameter("cursor", cursor)
                parameter("limit", limit)
            },
        )
    }

    override fun bulkImportAccounts(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Flow<Resource<BaseResponse<BulkImportSummaryResponse>>> = flow {
        emit(Resource.Loading)

        emit(
            client.sendRequest<BaseResponse<BulkImportSummaryResponse>>(tag) {
                method = HttpMethod.Post
                url(ApiEndpoint.AccountsBulkImport.url(ApiConfig.baseUrl))

                // No `contentType(...)` on the request: Ktor writes the
                // multipart type *with the boundary it generated*, and setting
                // it by hand would replace that with a boundary-less header the
                // server cannot split.
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append(
                                key = "file",
                                value = bytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, mimeType)
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"$fileName\"",
                                    )
                                },
                            )
                        },
                    ),
                )
            },
        )
    }
}
