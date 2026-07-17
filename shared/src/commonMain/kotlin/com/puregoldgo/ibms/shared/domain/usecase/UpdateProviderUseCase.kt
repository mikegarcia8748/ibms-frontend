package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.Resource
import com.puregoldgo.ibms.shared.model.Provider
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Updates an existing provider after validating required fields.
 */
class UpdateProviderUseCase(
    private val repository: ProviderRepository,
) {
    operator fun invoke(provider: Provider): Flow<Resource<Provider>> = flow {
        if (provider.id.isBlank()) {
            emit(Resource.Failed("Provider ID is required for update"))
            return@flow
        }
        val nameError = Validation.validateRequired(provider.name, "Provider name")
        if (nameError != null) {
            emit(Resource.Failed(nameError))
            return@flow
        }
        val codeError = Validation.validateRequired(provider.code, "Provider code")
        if (codeError != null) {
            emit(Resource.Failed(codeError))
            return@flow
        }
        if (provider.contactEmail != null) {
            val emailError = Validation.validateEmail(provider.contactEmail)
            if (emailError != null) {
                emit(Resource.Failed(emailError))
                return@flow
            }
        }
        emitAll(repository.updateProvider(provider))
    }
}
