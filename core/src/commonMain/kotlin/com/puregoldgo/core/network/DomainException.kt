package com.puregoldgo.core.network

/**
 * Domain-level exceptions for structured error handling across the application.
 */
sealed class DomainException : Exception() {
    data class ApiException(val code: Int, override val message: String) : DomainException()

    /**
     * A failed authentication call, already classified. Carries the [AuthFailure]
     * so a ViewModel can branch on the case (expired challenge, locked account,
     * weak password…) without re-reading status codes or matching on message text.
     */
    data class AuthException(
        val failure: AuthFailure,
        override val message: String,
        val status: Int,
    ) : DomainException()
}
