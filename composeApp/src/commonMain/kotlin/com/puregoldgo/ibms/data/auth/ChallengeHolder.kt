package com.puregoldgo.ibms.data.auth

import com.puregoldgo.ibms.shared.api.PasswordChangeChallenge
import com.puregoldgo.ibms.shared.model.UserProfile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Holds the live change-password challenge between the sign-in screen and the
 * set-password screen.
 *
 * It exists because the challenge token must not travel the way navigation
 * arguments do. Routes here are `@Serializable` `NavKey`s persisted through
 * `navSavedStateConfig`, so a token passed as a route argument would be written
 * into saved state and survive process death on disk — a bearer credential
 * outliving the screen that needed it. Keeping it in memory means the worst case
 * of a killed process is that the user signs in again, which is the correct
 * outcome anyway: the challenge is only good for ten minutes.
 *
 * The deadline is read from a [TimeSource.Monotonic] mark rather than wall-clock
 * time, so backgrounding the app or a clock change cannot appear to extend it.
 */
class ChallengeHolder(
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {

    data class Pending(
        val user: UserProfile,
        val challengeToken: String,
        val expiresAt: TimeMark,
    ) {
        /** Time left before the challenge dies; never negative. */
        val remaining: Duration
            get() = (-expiresAt.elapsedNow()).coerceAtLeast(Duration.ZERO)
    }

    private var pending: Pending? = null

    fun put(user: UserProfile, challenge: PasswordChangeChallenge) {
        pending = Pending(
            user = user,
            challengeToken = challenge.challengeToken,
            expiresAt = timeSource.markNow() + challenge.expiresInSeconds.seconds,
        )
    }

    /** The live challenge, or null once it has expired or been cleared. */
    fun current(): Pending? = pending?.takeUnless { it.expiresAt.hasPassedNow() }

    fun clear() {
        pending = null
    }
}
