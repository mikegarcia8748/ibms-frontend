package com.puregoldgo.ibms.shared.validation

/**
 * Shared field validators reusable on both client and server.
 * Returns null if valid, or an error message string if invalid.
 */
object Validation {

    private val PERIOD_REGEX = Regex("""^\d{4}-(0[1-9]|1[0-2])$""")
    private val BRANCH_CODE_REGEX = Regex("""^[A-Z0-9]{2,10}$""")
    private val ACCOUNT_NUMBER_REGEX = Regex("""^[A-Za-z0-9\-]{3,50}$""")

    fun validatePeriod(period: String): String? {
        if (!PERIOD_REGEX.matches(period)) {
            return "Period must be in YYYY-MM format (e.g. 2026-08)"
        }
        return null
    }

    fun validateBranchCode(code: String): String? {
        if (code.isBlank()) return "Branch code is required"
        if (!BRANCH_CODE_REGEX.matches(code)) {
            return "Branch code must be 2-10 uppercase alphanumeric characters"
        }
        return null
    }

    fun validateAccountNumber(number: String): String? {
        if (number.isBlank()) return "Account number is required"
        if (!ACCOUNT_NUMBER_REGEX.matches(number)) {
            return "Account number must be 3-50 alphanumeric characters (hyphens allowed)"
        }
        return null
    }

    fun validateAmount(amount: String): String? {
        val value = amount.trim().toDoubleOrNull()
        if (value == null) return "Amount must be a valid number"
        if (value <= 0) return "Amount must be greater than zero"
        return null
    }

    fun validateRate(rate: String): String? {
        val value = rate.trim().toDoubleOrNull()
        if (value == null) return "Rate must be a valid number"
        if (value <= 0) return "Rate must be greater than zero"
        return null
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email is required"
        if (!email.contains("@") || !email.contains(".")) {
            return "Invalid email format"
        }
        return null
    }

    fun validateRequired(value: String?, fieldName: String): String? {
        if (value.isNullOrBlank()) return "$fieldName is required"
        return null
    }
}
