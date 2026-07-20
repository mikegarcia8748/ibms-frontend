package com.puregoldgo.ibms.shared.validation

/**
 * The rules a user-chosen password must satisfy, mirroring the backend's
 * `PasswordPolicy` (ibms-backend/src/domain/service/PasswordPolicy.kt).
 *
 * The server is still the authority — this copy exists so the set-password
 * screen can show which rules are met *while the user types* instead of turning
 * every attempt into a round trip. Both sides must agree; if the backend policy
 * moves, this moves with it.
 *
 * Length does more for strength than character-class rules do, hence the 12
 * character floor. The upper bound is not arbitrary either: bcrypt silently
 * truncates past 72 bytes, so a longer password would only look stronger.
 */
object PasswordPolicy {

    const val MIN_LENGTH = 12
    const val MAX_LENGTH = 72

    /** One policy rule, resolved against a candidate password. */
    data class Rule(
        val id: RuleId,
        val satisfied: Boolean,
    )

    enum class RuleId {
        MinLength,
        MaxLength,
        Uppercase,
        Lowercase,
        Digit,
        NoSpaces,
        NotUsername,
    }

    /**
     * Evaluates every rule. Returns all of them — met and unmet — because the
     * checklist shows the full set from the moment the screen opens rather than
     * revealing requirements one failure at a time.
     */
    fun evaluate(password: String, username: String): List<Rule> = listOf(
        Rule(RuleId.MinLength, password.length >= MIN_LENGTH),
        Rule(RuleId.MaxLength, password.length <= MAX_LENGTH),
        Rule(RuleId.Uppercase, password.any { it.isUpperCase() }),
        Rule(RuleId.Lowercase, password.any { it.isLowerCase() }),
        Rule(RuleId.Digit, password.any { it.isDigit() }),
        Rule(RuleId.NoSpaces, password.none { it.isWhitespace() }),
        Rule(
            RuleId.NotUsername,
            username.isBlank() || !password.contains(username, ignoreCase = true),
        ),
    )

    fun isSatisfiedBy(password: String, username: String): Boolean =
        password.isNotEmpty() && evaluate(password, username).all { it.satisfied }

    /**
     * A rough strength read for the meter — advisory only, never a gate. Counts
     * length and character variety, and discounts passwords that are mostly one
     * repeated character.
     */
    fun strength(password: String): Strength {
        if (password.isEmpty()) return Strength.None

        val classes = listOf(
            password.any { it.isUpperCase() },
            password.any { it.isLowerCase() },
            password.any { it.isDigit() },
            password.any { !it.isLetterOrDigit() },
        ).count { it }

        val distinctRatio = password.toSet().size.toFloat() / password.length
        var score = when {
            password.length >= 20 -> 3
            password.length >= 16 -> 2
            password.length >= MIN_LENGTH -> 1
            else -> 0
        }
        score += when {
            classes >= 4 -> 2
            classes == 3 -> 1
            else -> 0
        }
        if (distinctRatio < 0.5f) score -= 1

        return when {
            password.length < MIN_LENGTH -> Strength.Weak
            score <= 1 -> Strength.Weak
            score == 2 -> Strength.Fair
            score == 3 -> Strength.Good
            else -> Strength.Strong
        }
    }

    enum class Strength { None, Weak, Fair, Good, Strong }
}
