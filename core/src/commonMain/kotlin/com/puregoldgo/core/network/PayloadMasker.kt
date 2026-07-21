package com.puregoldgo.core.network

/**
 * Masks sensitive fields in request/response payloads before logging.
 */
object PayloadMasker {

    private val sensitiveKeys = listOf(
        "password",
        "token",
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
            // Match "key":"value" or "key": "value" patterns (JSON-like).
            //
            // The key is matched as a *suffix*, not in full: the wire is full of
            // compounds — `temporaryPassword`, `newPassword`, `challengeToken`,
            // `refreshToken` — and anchoring to the opening quote let every one
            // of them through. A generated temporary password reaching the log
            // is the failure this exists to prevent.
            val regex = Regex(
                "(\"[A-Za-z0-9_]*$key\"\\s*:\\s*\")([^\"]+)(\")",
                RegexOption.IGNORE_CASE,
            )
            masked = regex.replace(masked) { match ->
                "${match.groupValues[1]}$maskReplacement${match.groupValues[3]}"
            }
        }
        return masked
    }
}
