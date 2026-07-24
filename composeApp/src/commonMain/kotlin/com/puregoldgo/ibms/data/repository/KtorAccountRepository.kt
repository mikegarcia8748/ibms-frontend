package com.puregoldgo.ibms.data.repository

import com.puregoldgo.core.network.ApiConfig
import com.puregoldgo.core.network.ApiEndpoint
import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.core.network.sendExternalRequest
import com.puregoldgo.core.network.sendRequest
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.api.PresignedUrlResponse
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.CreateAccountRequest
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.queryValue
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

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

    override fun createAccount(
        request: CreateAccountRequest,
    ): Flow<Resource<BaseResponse<Account>>> = flow {
        emit(Resource.Loading)

        emit(
            client.sendRequest<BaseResponse<Account>>(tag) {
                method = HttpMethod.Post
                url(ApiEndpoint.Accounts.url(ApiConfig.baseUrl))
                setBody(request)
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

    /**
     * Uploads a proof file through the presigned URL flow.
     *
     * Returns the attachment id on success, or throws an exception the caller
     * can turn into a user-facing error.
     */
    override suspend fun uploadAttachment(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String {
        val presignResult = client.sendRequest<BaseResponse<PresignedUrlResponse>>(tag) {
            method = HttpMethod.Post
            url(ApiEndpoint.AttachmentsPresignUpload.url(ApiConfig.baseUrl))
            setBody(PresignUploadRequest(fileName = fileName, contentType = mimeType))
        }

        val presign = when (presignResult) {
            is Resource.Success -> presignResult.data?.data
            is Resource.Failed -> throw IllegalStateException(
                presignResult.message ?: presignResult.data?.message ?: "Presign request failed",
            )
            is Resource.Error -> throw presignResult.error ?: IllegalStateException("Presign request failed")
            is Resource.Loading -> error("Unexpected loading state after presign request")
        }

        val uploadUrl = presign?.url ?: throw IllegalStateException("No presigned URL returned")
        val attachmentId = presign.attachmentId ?: throw IllegalStateException("No attachment id returned")

        val uploadResult = client.sendExternalRequest<Unit> {
            method = HttpMethod.Put
            url(uploadUrl)
            setBody(bytes)
            headers {
                append(HttpHeaders.ContentType, mimeType)
            }
        }

        when (uploadResult) {
            is Resource.Success -> Unit
            is Resource.Failed -> throw IllegalStateException(uploadResult.message ?: "Upload failed")
            is Resource.Error -> throw uploadResult.error ?: IllegalStateException("Upload failed")
            is Resource.Loading -> error("Unexpected loading state after upload")
        }

        return attachmentId
    }
}

@Serializable
private data class PresignUploadRequest(
    val fileName: String,
    val contentType: String,
)
