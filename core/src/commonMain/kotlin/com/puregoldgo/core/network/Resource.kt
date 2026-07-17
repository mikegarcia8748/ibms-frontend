package com.puregoldgo.core.network

/**
 * Generic wrapper for async operations — used by repositories and consumed by UseCases/ViewModels.
 */
sealed class Resource<out T>(
    val data: T? = null,
    val error: Throwable? = null,
) {
    data object Loading : Resource<Nothing>()
    class Success<T>(data: T) : Resource<T>(data)
    class Failed<T>(data: T? = null, val message: String? = null) : Resource<T>(data)
    class Error<T>(throwable: Throwable?, data: T? = null) : Resource<T>(data, throwable)
}
