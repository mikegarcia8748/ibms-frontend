package com.puregoldgo.core.network

/**
 * Masks sensitive fields in request/response payloads before logging.
 */
object PayloadMasker {

    private val sensitiveKeys = listOf(
        "password",
        "token",
        "accessToken",
        "refreshToken",
        "secret",
        "authorization",
    )

    private val maskReplacement = "***MASKED***"

    /**
     * Returns the input string with sensitive field values replaced by a mask.
     * Returns null if the input is null.
     */
    fun mask(payload: String?): String? {
        if (payload.isNullOrBlank()) return payload
        var masked: String = payload
        for (key in sensitiveKeys) {
            // Match "key":"value" or "key": "value" patterns (JSON-like)
            val regex = Regex(
                "(\"$key\"\\s*:\\s*\")([^\"]+)(\")",
                RegexOption.IGNORE_CASE,
            )
            masked = regex.replace(masked) { match ->
                "${match.groupValues[1]}$maskReplacement${match.groupValues[3]}"
            }
        }
        return masked
    }
}
