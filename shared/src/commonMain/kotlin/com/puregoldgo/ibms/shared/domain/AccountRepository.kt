package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.CursorPage
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Account operations.
 * Implementation lives in :composeApp (data layer).
 */
interface AccountRepository {

    /**
     * One page of `GET /accounts`.
     *
     * A page, not the whole list: the endpoint is cursor-paginated and caps
     * [limit] at 100 server-side. Callers that need everything loop on
     * [CursorPage.nextCursor] — see `LoadAccountsUseCase`.
     */
    fun listAccounts(
        providerId: String? = null,
        storeId: String? = null,
        status: AccountStatus? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): Flow<Resource<BaseResponse<CursorPage<Account>>>>

    /**
     * Uploads a master list to `POST /accounts/bulk-import`.
     *
     * A [Flow] rather than a plain `suspend fun` because the call is slow enough
     * to need a spinner — a 172-row spreadsheet takes seconds — and emitting
     * [Resource.Loading] first is how the rest of this app drives one.
     *
     * Takes raw bytes rather than a file handle: there is no common file type
     * across wasmJs, Android and iOS, and the picker has already read the file
     * by the time this is called.
     */
    fun bulkImportAccounts(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Flow<Resource<BaseResponse<BulkImportSummaryResponse>>>
}
