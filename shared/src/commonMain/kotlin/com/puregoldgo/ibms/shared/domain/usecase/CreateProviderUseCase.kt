package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Creates a new provider after validating required fields.
 */
class CreateProviderUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(provider: Provider): Flow<Resource<Provider>> = flow {
        val nameError = Validation.validateRequired(provider.name, "Provider name")
        if (nameError != null) {
            emit(Resource.Failed(message = nameError))
            return@flow
        }
        val codeError = Validation.validateRequired(provider.code, "Provider code")
        if (codeError != null) {
            emit(Resource.Failed(message = codeError))
            return@flow
        }
        if (provider.contactEmail != null) {
            val emailError = Validation.validateEmail(provider.contactEmail)
            if (emailError != null) {
                emit(Resource.Failed(message = emailError))
                return@flow
            }
        }
        when (val result = repository.createProvider(provider)) {
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
