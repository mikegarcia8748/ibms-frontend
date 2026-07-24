package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.CreateStoreRequest
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.model.Store
import com.puregoldgo.ibms.shared.model.StoreType
import com.puregoldgo.ibms.shared.validation.Validation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Creates a new store after validating the required fields.
 *
 * Branch code and name are required. Location fields are optional but are
 * passed through when present so a branch can be registered with its full
 * address up front.
 */
class CreateStoreUseCase(
    private val repository: StoreRepository,
) {
    operator fun invoke(
        storeType: StoreType,
        branchCode: String,
        name: String,
        region: String? = null,
        province: String? = null,
        city: String? = null,
        barangay: String? = null,
        postal: String? = null,
    ): Flow<Resource<Store>> = flow {
        val normalizedCode = branchCode.trim().uppercase()
        val codeError = Validation.validateBranchCode(normalizedCode)
        if (codeError != null) {
            emit(Resource.Failed(message = codeError))
            return@flow
        }

        val normalizedName = name.trim()
        val nameError = Validation.validateRequired(normalizedName, "Branch name")
        if (nameError != null) {
            emit(Resource.Failed(message = nameError))
            return@flow
        }

        val request = CreateStoreRequest(
            storeType = storeType,
            branchCode = normalizedCode,
            name = normalizedName,
            region = region?.takeIf { it.isNotBlank() },
            province = province?.takeIf { it.isNotBlank() },
            city = city?.takeIf { it.isNotBlank() },
            barangay = barangay?.takeIf { it.isNotBlank() },
            postal = postal?.takeIf { it.isNotBlank() },
        )

        when (val result = repository.createStore(request)) {
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
