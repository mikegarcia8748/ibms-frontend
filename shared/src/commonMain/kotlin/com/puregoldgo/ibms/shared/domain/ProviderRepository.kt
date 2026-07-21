package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.core.network.dto.BaseResponse
import com.puregoldgo.ibms.shared.model.CursorPage
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.model.ProviderStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Provider (ISP) operations.
 * Implementation lives in :composeApp (data layer).
 */
interface ProviderRepository {

    /**
     * One page of `GET /providers`.
     *
     * A page, not the whole list: the endpoint is cursor-paginated and caps
     * `limit` at 100 server-side. Callers that need everything loop on
     * [CursorPage.nextCursor] — see [usecase.GetProvidersUseCase].
     *
     * Omitting [status] returns active and inactive alike, which is what the
     * control panel wants: it lists both, in separate sections.
     */
    fun listProviders(
        status: ProviderStatus? = null,
        cursor: String? = null,
        limit: Int? = null,
    ): Flow<Resource<BaseResponse<CursorPage<Provider>>>>

    /**
     * `POST /providers`.
     *
     * Takes the two fields the endpoint accepts rather than a whole [Provider]:
     * the id, status and timestamps are the server's to assign, and passing a
     * half-built [Provider] with a blank id only invites someone to believe one
     * of those fields is honoured.
     */
    suspend fun createProvider(
        name: String,
        paymentScheduleDay: Int,
    ): Resource<BaseResponse<Provider>>

    /** `PUT /providers/{id}`. Both fields are optional server-side; a null is "leave alone". */
    suspend fun updateProvider(
        id: String,
        name: String? = null,
        paymentScheduleDay: Int? = null,
    ): Resource<BaseResponse<Provider>>
}
