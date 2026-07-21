package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.domain.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Imports a master list spreadsheet of stores and accounts.
 *
 * Unwraps the response envelope so the ViewModel never sees `BaseResponse`. A
 * `success` envelope with no `data` is treated as a failure rather than an empty
 * success: the summary *is* the result, and reporting "0 imported" for a
 * malformed response would be a lie.
 */
class BulkImportAccountsUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Flow<Resource<BulkImportSummaryResponse>> = flow {
        repository.bulkImportAccounts(fileName, mimeType, bytes).collect { result ->
            when (result) {
                is Resource.Loading -> emit(Resource.Loading)

                is Resource.Success -> {
                    val summary = result.data?.data
                    if (summary != null) {
                        emit(Resource.Success(summary))
                    } else {
                        emit(Resource.Failed(message = result.data?.message))
                    }
                }

                is Resource.Failed ->
                    emit(Resource.Failed(message = result.message ?: result.data?.message))

                is Resource.Error -> emit(Resource.Error(result.error))
            }
        }
    }
}
