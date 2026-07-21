package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** The day-of-month range a provider can be paid on. */
val PAYMENT_SCHEDULE_DAYS = 1..31

/**
 * Creates a new provider after validating its fields.
 *
 * A provider is only a name and a payment day — there is no code, email or phone
 * on the backend. The day is validated here rather than left to the server
 * because "31" is a legitimate answer that behaves badly in February, and the
 * form should say so before a round trip.
 */
class CreateProviderUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(name: String, paymentScheduleDay: Int): Flow<Resource<Provider>> = flow {
        val nameError = Validation.validateRequired(name, "Provider name")
        if (nameError != null) {
            emit(Resource.Failed(message = nameError))
            return@flow
        }
        if (paymentScheduleDay !in PAYMENT_SCHEDULE_DAYS) {
            emit(Resource.Failed(message = "Payment schedule day must be between 1 and 31"))
            return@flow
        }

        when (val result = repository.createProvider(name.trim(), paymentScheduleDay)) {
            is Resource.Loading -> emit(Resource.Loading)
            is Resource.Success -> {
                val created = result.data?.data
                if (created != null) {
                    emit(Resource.Success(created))
                } else {
                    emit(Resource.Failed(message = result.data?.message ?: "No data returned from server"))
                }
            }
            is Resource.Failed -> emit(Resource.Failed(message = result.message ?: result.data?.message))
            is Resource.Error -> emit(Resource.Error(result.error))
        }
    }
}
