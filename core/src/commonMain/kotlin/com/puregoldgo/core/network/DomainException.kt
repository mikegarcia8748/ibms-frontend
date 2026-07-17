package com.puregoldgo.core.network

/**
 * Domain-level exceptions for structured error handling across the application.
 */
sealed class DomainException : Exception() {
    data class ApiException(val code: Int, override val message: String) : DomainException()
}
