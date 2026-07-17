package com.puregoldgo.ibms.shared.domain

/**
 * Generic wrapper for async operations — used by repositories and consumed by UseCases/ViewModels.
 */
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Failed(val message: String, val code: String? = null) : Resource<Nothing>()
}
