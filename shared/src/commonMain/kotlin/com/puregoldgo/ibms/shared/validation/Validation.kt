package com.puregoldgo.ibms.shared.validation

/**
 * Shared field validators reusable on both client and server.
 * Returns null if valid, or an error message string if invalid.
 */
object Validation {

    private val PERIOD_REGEX = Regex("""^\d{4}-(0[1-9]|1[0-2])$""")
    private val BRANCH_CODE_REGEX = Regex("""^[A-Z0-9]{2,10}$""")
    private val ACCOUNT_NUMBER_REGEX = Regex("""^[A-Za-z0-9\-]{3,50}$""")

    /** Mirrors the backend's `UsernamePolicy` and the `users_username_format` CHECK. */
    private val USERNAME_REGEX = Regex("""^[a-zA-Z0-9._-]{3,32}$""")

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

    /**
     * Gate for a username being provisioned, phrased like the backend's rejection
     * so the two never contradict each other in front of the admin.
     *
     * Checks the *normalised* form. The column is CITEXT and the backend
     * lowercases on the way in, so "M.Garcia" and "m.garcia" are the same
     * account — validating the raw input would let a form accept a name that
     * then collides on submit.
     */
    fun validateUsername(username: String): String? {
        if (username.isBlank()) return "Username is required"
        if (!USERNAME_REGEX.matches(normalizeUsername(username))) {
            return "Username must be 3-32 characters using only letters, digits, " +
                "dot, underscore or hyphen"
        }
        return null
    }

    /** Trim + lowercase, matching the backend's `UsernamePolicy.normalize`. */
    fun normalizeUsername(username: String): String = username.trim().lowercase()

    /**
     * Gate for a self-chosen password, phrased like the backend's rejection so
     * the two never contradict each other in front of the user.
     *
     * This is the last check before the request goes out; the screen shows the
     * same rules live via [PasswordPolicy.evaluate].
     */
    fun validateNewPassword(password: String, username: String): String? {
        if (password.isBlank()) return "New password is required"
        val unmet = PasswordPolicy.evaluate(password, username).filterNot { it.satisfied }
        if (unmet.isEmpty()) return null
        return "Password must " + unmet.joinToString(", ") { describe(it.id) }
    }

    fun validatePasswordConfirmation(password: String, confirmation: String): String? {
        if (confirmation.isBlank()) return "Please re-enter your new password"
        if (password != confirmation) return "Passwords do not match"
        return null
    }

    private fun describe(rule: PasswordPolicy.RuleId): String = when (rule) {
        PasswordPolicy.RuleId.MinLength -> "be at least ${PasswordPolicy.MIN_LENGTH} characters"
        PasswordPolicy.RuleId.MaxLength -> "be at most ${PasswordPolicy.MAX_LENGTH} characters"
        PasswordPolicy.RuleId.Uppercase -> "contain an uppercase letter"
        PasswordPolicy.RuleId.Lowercase -> "contain a lowercase letter"
        PasswordPolicy.RuleId.Digit -> "contain a digit"
        PasswordPolicy.RuleId.NoSpaces -> "not contain spaces"
        PasswordPolicy.RuleId.NotUsername -> "not contain your username"
    }
}
