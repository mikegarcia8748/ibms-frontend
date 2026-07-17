package com.puregoldgo.ibms.shared.domain

import com.puregoldgo.core.network.Resource
import com.puregoldgo.ibms.shared.api.AuthResponse
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 * Implementation lives in :composeApp (data layer).
 */
interface AuthRepository {
    fun login(username: String, password: String): Flow<Resource<AuthResponse>>
}
