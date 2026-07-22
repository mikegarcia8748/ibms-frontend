package com.puregoldgo.ibms.shared.domain

import kotlinx.datetime.FixedOffsetTimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Arithmetic and validation for a `YYYY-MM` billing period.
 *
 * Kept here — in the shared module that already carries `kotlinx-datetime` — so
 * the compile ViewModel deals only in plain strings and the month math stays
 * testable. [current] resolves the month at a fixed UTC+8 offset (the Philippines
 * observes no DST), matching the boundary the backend uses when it rejects a
 * future period. A fixed offset — rather than the named `Asia/Manila` zone — is
 * used deliberately: it needs no IANA timezone database, which Kotlin/Wasm
 * doesn't bundle, so this also works on the wasmJs webApp target.
 */
object BillingPeriod {

    private val MANILA = FixedOffsetTimeZone(UtcOffset(hours = 8))

    /** The current billing period, e.g. `"2026-07"`. */
    @OptIn(ExperimentalTime::class)
    fun current(): String {
        val now = Clock.System.now().toLocalDateTime(MANILA)
        // `month.ordinal + 1` rather than a possibly-renamed month-number property.
        return format(now.year, now.month.ordinal + 1)
    }

    /** True when [period] is a well-formed `YYYY-MM`. */
    fun isValid(period: String): Boolean {
        val (year, month) = parse(period) ?: return false
        return year in 1..9999 && month in 1..12
    }

    /** True when [period] is later than the current month — what the backend forbids. */
    fun isFuture(period: String): Boolean = isValid(period) && period > current()

    /** The month before [period]. Rolls the year back at January. */
    fun previous(period: String): String {
        val (year, month) = parse(period) ?: return period
        return if (month == 1) format(year - 1, 12) else format(year, month - 1)
    }

    /** The month after [period]. Rolls the year forward at December. */
    fun next(period: String): String {
        val (year, month) = parse(period) ?: return period
        return if (month == 12) format(year + 1, 1) else format(year, month + 1)
    }

    private fun parse(period: String): Pair<Int, Int>? {
        val parts = period.split("-")
        if (parts.size != 2) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        return year to month
    }

    private fun format(year: Int, month: Int): String =
        "$year-${month.toString().padStart(2, '0')}"
}
