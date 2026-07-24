package com.puregoldgo.ibms.ui.screen.secretary.compile

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.api.BulkImportSummaryResponse
import com.puregoldgo.ibms.shared.api.CompilePreview
import com.puregoldgo.ibms.shared.api.TopSheetLine
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.domain.TopSheetRepository
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreStatus
import com.puregoldgo.ibms.shared.model.TopsheetStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** In-memory stand-ins for the four repositories the compile panel talks to. */

class FakeTopSheetRepository : TopSheetRepository {

    var previewResult: CompilePreview? = null
    var previewError: String? = null
    var draftResult: TopSheetSummary? = null
    var createError: String? = null
    var confirmResult: TopSheetSummary? = null
    var confirmError: String? = null
    var assignRfpError: String? = null
    val lines: MutableList<TopSheetLine> = mutableListOf()

    val patchedRfp: MutableList<Pair<String, String?>> = mutableListOf()
    val removedLines: MutableList<String> = mutableListOf()
    val assignedRanges: MutableList<Pair<String, String>> = mutableListOf()

    var topSheets: List<TopSheetSummary> = emptyList()

    override fun listTopSheets(
        providerId: String?,
        billingPeriod: String?,
        status: TopsheetStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<TopSheetSummary>>>> = flow {
        emit(Resource.Loading)
        // Honors the filters the way the backend does, so a status=DRAFT query
        // returns only drafts — the resume-draft flow depends on it.
        val filtered = topSheets
            .filter { status == null || it.status == status }
            .filter { providerId == null || it.providerId == providerId }
            .filter { billingPeriod == null || it.billingPeriod == billingPeriod }
        emit(Resource.Success(BaseResponse(data = CursorPage(items = filtered, nextCursor = null))))
    }

    override fun preview(providerId: String, billingPeriod: String): Flow<Resource<BaseResponse<CompilePreview>>> = flow {
        emit(Resource.Loading)
        previewError?.let { emit(Resource.Failed(message = it)); return@flow }
        emit(Resource.Success(BaseResponse(data = previewResult)))
    }

    override suspend fun createDraft(
        providerId: String,
        billingPeriod: String,
        idempotencyKey: String,
    ): Resource<BaseResponse<TopSheetSummary>> {
        createError?.let { return Resource.Failed(message = it) }
        return Resource.Success(BaseResponse(data = draftResult))
    }

    override fun listLines(topsheetId: String): Flow<Resource<BaseResponse<List<TopSheetLine>>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(BaseResponse(data = lines.toList())))
    }

    override suspend fun updateLine(
        topsheetId: String,
        lineId: String,
        rfpNumber: String?,
        proratedAmount: String?,
    ): Resource<BaseResponse<TopSheetLine>> {
        patchedRfp += lineId to rfpNumber
        val index = lines.indexOfFirst { it.id == lineId }
        val updated = lines[index].copy(
            rfpNumber = rfpNumber ?: lines[index].rfpNumber,
            proratedAmount = proratedAmount ?: lines[index].proratedAmount,
        )
        lines[index] = updated
        return Resource.Success(BaseResponse(data = updated))
    }

    override suspend fun assignRfp(
        topsheetId: String,
        startRfpNumber: String,
        endRfpNumber: String,
    ): Resource<BaseResponse<List<TopSheetLine>>> {
        assignRfpError?.let { return Resource.Failed(message = it) }
        assignedRanges += startRfpNumber to endRfpNumber
        // Number each distinct store code from the range start, keeping the wider
        // input's width — mirrors what the backend returns for the test to map.
        val width = maxOf(startRfpNumber.length, endRfpNumber.length)
        val start = startRfpNumber.toLong()
        val numberByCode = lines.map { it.branchCode }.distinct()
            .mapIndexed { i, code -> code to (start + i).toString().padStart(width, '0') }
            .toMap()
        val assigned = lines.map { it.copy(rfpNumber = numberByCode[it.branchCode]) }
        lines.clear()
        lines.addAll(assigned)
        return Resource.Success(BaseResponse(data = lines.toList()))
    }

    override suspend fun deleteLine(topsheetId: String, lineId: String): Resource<Unit> {
        removedLines += lineId
        lines.removeAll { it.id == lineId }
        return Resource.Success(Unit)
    }

    override suspend fun confirm(topsheetId: String, idempotencyKey: String): Resource<BaseResponse<TopSheetSummary>> {
        confirmError?.let { return Resource.Failed(message = it) }
        return Resource.Success(BaseResponse(data = confirmResult))
    }
}

class FakeCompileProviderRepository(var providers: List<Provider> = emptyList()) : ProviderRepository {
    override fun listProviders(
        status: ProviderStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Provider>>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(BaseResponse(data = CursorPage(items = providers, nextCursor = null))))
    }

    override suspend fun createProvider(name: String, paymentScheduleDay: Int) =
        error("not used by the compile panel")

    override suspend fun updateProvider(id: String, name: String?, paymentScheduleDay: Int?) =
        error("not used by the compile panel")
}

class FakeCompileAccountRepository(var accounts: List<Account> = emptyList()) : AccountRepository {
    override fun listAccounts(
        providerId: String?,
        storeId: String?,
        status: AccountStatus?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Account>>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(BaseResponse(data = CursorPage(items = accounts, nextCursor = null))))
    }

    override fun bulkImportAccounts(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): Flow<Resource<BaseResponse<BulkImportSummaryResponse>>> = error("not used by the compile panel")
}

class FakeCompileStoreRepository(var stores: List<Store> = emptyList()) : StoreRepository {
    override fun listStores(
        status: StoreStatus?,
        query: String?,
        cursor: String?,
        limit: Int?,
    ): Flow<Resource<BaseResponse<CursorPage<Store>>>> = flow {
        emit(Resource.Loading)
        emit(Resource.Success(BaseResponse(data = CursorPage(items = stores, nextCursor = null))))
    }
}
