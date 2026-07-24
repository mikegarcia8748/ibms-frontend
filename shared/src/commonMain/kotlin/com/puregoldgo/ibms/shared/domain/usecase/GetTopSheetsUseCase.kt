package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.TopSheetSummary
import com.puregoldgo.ibms.shared.domain.TopSheetRepository
import com.puregoldgo.ibms.shared.model.TopsheetStatus
import kotlinx.coroutines.flow.Flow

/**
 * Fetches the compiled billing history, walking `GET /topsheets` to exhaustion.
 *
 * Leaves the filters unset by default: Billing History shows every topsheet
 * (all providers, periods and statuses — DRAFT included), and the panel's own
 * search filters a list this small more cheaply than a second round trip.
 */
class GetTopSheetsUseCase(
    private val repository: TopSheetRepository,
) {
    operator fun invoke(
        providerId: String? = null,
        billingPeriod: String? = null,
        status: TopsheetStatus? = null,
    ): Flow<Resource<List<TopSheetSummary>>> =
        loadAllPages { cursor ->
            repository.listTopSheets(
                providerId = providerId,
                billingPeriod = billingPeriod,
                status = status,
                cursor = cursor,
                limit = PAGE_SIZE,
            )
        }
}
