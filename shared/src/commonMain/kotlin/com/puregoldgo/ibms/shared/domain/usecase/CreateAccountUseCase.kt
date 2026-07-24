package com.puregoldgo.ibms.shared.domain.usecase

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.model.Account
import com.puregoldgo.ibms.shared.model.CreateAccountRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Creates a single ISP account.
 *
 * Field validation lives in the ViewModel so errors can be shown per field; this
 * use case only forwards the request and unwraps the envelope.
 */
class CreateAccountUseCase(
    private val repository: AccountRepository,
) {
    operator fun invoke(request: CreateAccountRequest): Flow<Resource<Account>> = flow {
        repository.createAccount(request).collect { result ->
            when (result) {
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
}
