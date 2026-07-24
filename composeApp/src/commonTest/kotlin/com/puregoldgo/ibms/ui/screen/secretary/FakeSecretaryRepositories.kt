package com.puregoldgo.ibms.ui.screen.secretary

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.LoginResponse
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.CreateAccountRequest
import com.puregoldgo.ibms.shared.model.CursorPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In-memory stand-ins for the repositories the secretary panel talks to.
 *
 * The panel is still sample-fed for browse lists, but the create-account path is
 * real, so these fakes only need to record requests and emit controlled
 * responses for `POST /accounts` and the attachment presign/upload flow.
 */
class FakeAccountRepository : AccountRepository {

    var createResult: Account? = null
    var createError: String? = null
    var uploadError: Throwable? = null
    var nextAttachmentId: Int = 1

    val createdRequests = mutableListOf<CreateAccountRequest>()
    val uploadedFiles = mutableListOf<Triple<String, String, ByteArray>>()

    override fun listAccounts(
        providerId: String?,
        storeId: String?,
        status: AccountStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Account>>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(BaseResponse(data = CursorPage(items = emptyList(), nextCursor = null))))
    }

    override fun createAccount(
        request: CreateAccountRequest,
    ): Flow<Resource<BaseResponse<Account>>> = flow {
        emit(Resource.Loading)
        createdRequests += request
        if (createError != null) {
            emit(Resource.Failed(message = createError))
        } else {
            val account = createResult ?: Account(
                id = "acc-new-${nextAttachmentId++}",
                accountNumber = request.accountNumber,
                circuitId = request.circuitId,
                providerId = request.providerId,
                storeId = request.storeId,
                planName = request.planName,
                rate = request.rate,
                installationDate = request.installationDate,
                billingPeriodLabel = request.billingPeriodLabel,
                subscriptionProofIds = request.subscriptionProofIds,
                status = AccountStatus.ACTIVE,
            )
            emit(Resource.Success(BaseResponse(data = account)))
        }
    }

    override suspend fun uploadAttachment(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): String {
        uploadedFiles += Triple(fileName, mimeType, bytes)
        uploadError?.let { throw it }
        return "att-${nextAttachmentId++}"
    }

    override fun bulkImportAccounts(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Flow<Resource<BaseResponse<com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse>>> =
        error("not used by the secretary panel")
}

class FakeAuthRepository : AuthRepository {
    override fun login(username: String, password: String) =
        flow<Resource<LoginResponse>> { error("not used by the secretary panel") }

    override fun completePasswordChange(challengeToken: String, newPassword: String) =
        flow<Resource<LoginResponse>> { error("not used by the secretary panel") }

    override fun refreshSession() =
        flow<Resource<LoginResponse>> { error("not used by the secretary panel") }

    override fun signOut() = Unit
}
