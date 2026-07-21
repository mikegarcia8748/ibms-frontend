package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.AccountStatus
import kotlinx.coroutines.flow.Flow

/**
 * Fetches accounts, walking the endpoint's pages to exhaustion.
 *
 * The filters are the endpoint's own, applied server-side. The control panel
 * passes none of them — its ISP dropdown and search box compose client-side over
 * the full set — but a caller asking about one store or one provider should not
 * have to download the whole table to find out.
 */
class GetAccountsUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(
        providerId: String? = null,
        storeId: String? = null,
        status: AccountStatus? = null,
    ): Flow<Resource<List<Account>>> = loadAllPages { cursor ->
        repository.listAccounts(
            providerId = providerId,
            storeId = storeId,
            status = status,
            cursor = cursor,
            limit = PAGE_SIZE,
        )
    }
}
