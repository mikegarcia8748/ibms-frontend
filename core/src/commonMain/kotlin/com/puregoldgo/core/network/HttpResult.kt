package com.puregoldgo.core.network

/**
 * Represents the business-logic result values returned by the IBMS backend API envelope.
 */
enum class HttpResult(val value: String) {
    Success("success"),
    Failed("failed"),
}
