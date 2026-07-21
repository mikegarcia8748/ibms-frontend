package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Updates an existing provider after validating its fields.
 *
 * Same rules as [CreateProviderUseCase], plus an id. The endpoint accepts a
 * partial update, but this screen always sends both fields — it edits a form it
 * populated from the current values, so "unchanged" and "cleared" never need
 * telling apart.
 */
class UpdateProviderUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(
        id: String,
        name: String,
        paymentScheduleDay: Int,
    ): Flow<Resource<Provider>> = flow {
        if (id.isBlank()) {
            emit(Resource.Failed(message = "Provider ID is required for update"))
            return@flow
        }
        val nameError = Validation.validateRequired(name, "Provider name")
        if (nameError != null) {
            emit(Resource.Failed(message = nameError))
            return@flow
        }
        if (paymentScheduleDay !in PAYMENT_SCHEDULE_DAYS) {
            emit(Resource.Failed(message = "Payment schedule day must be between 1 and 31"))
            return@flow
        }

        when (val result = repository.updateProvider(id, name.trim(), paymentScheduleDay)) {
            is Resource.Loading -> emit(Resource.Loading)
            is Resource.Success -> {
                val updated = result.data?.data
                if (updated != null) {
                    emit(Resource.Success(updated))
                } else {
                    emit(Resource.Failed(message = result.data?.message ?: "No data returned from server"))
                }
            }
            is Resource.Failed -> emit(Resource.Failed(message = result.message ?: result.data?.message))
            is Resource.Error -> emit(Resource.Error(result.error))
        }
    }
}
