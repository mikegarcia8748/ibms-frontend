package com.puregoldgo.ibms.shared.engine

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * Billing proration engine — shared across all platforms (client + server).
 *
 * Formula:  proratedAmount = rate * activeDays / totalDaysInPeriod
 * Rounding: HALF_UP to 2 decimal places.
 *
 * This replaces the JS implementation in SecretaryDashboard.tsx, using explicit
 * LocalDate arithmetic instead of locale-sensitive `new Date(...)`.
 */
object ProrationEngine {

    /**
     * Computes the prorated billing amount for a given account in a period.
     *
     * @param rate         Monthly rate as a decimal string (e.g. "1500.00")
     * @param installDate  Account installation date (YYYY-MM-DD)
     * @param periodYear   Billing period year
     * @param periodMonth  Billing period month (1-12)
     * @return Prorated amount as a decimal string rounded to 2dp, or the full rate
     *         if the account was installed before the billing period.
     */
    fun computeProration(
        rate: String,
        installDate: LocalDate,
        periodYear: Int,
        periodMonth: Int,
    ): String {
        val totalDays = daysInMonth(periodYear, periodMonth)
        val periodStart = LocalDate(periodYear, periodMonth, 1)

        // If installed before or on the first day of the period, full rate applies
        if (installDate <= periodStart) {
            return formatDecimal(rate)
        }

        // If installed after the period entirely, zero
        val periodEnd = LocalDate(periodYear, periodMonth, totalDays)
        if (installDate > periodEnd) {
            return "0.00"
        }

        // Active days = from install date to end of month (inclusive)
        val activeDays = totalDays - installDate.dayOfMonth + 1

        return prorate(rate, activeDays, totalDays)
    }

    /**
     * Overload accepting string period format "YYYY-MM".
     */
    fun computeProration(
        rate: String,
        installDate: LocalDate,
        period: String,
    ): String {
        val (year, month) = parsePeriod(period)
        return computeProration(rate, installDate, year, month)
    }

    /**
     * Core proration math: rate * activeDays / totalDays, HALF_UP to 2dp.
     * Uses Long arithmetic scaled to 4dp for precision, then rounds to 2dp.
     */
    fun prorate(rate: String, activeDays: Int, totalDays: Int): String {
        // Parse rate to cents (2dp) then scale to 4dp for intermediate precision
        val rateCents = parseDecimalToCents(rate) // rate * 100
        val scaled = rateCents.toLong() * activeDays * 100L // rate * 100 * activeDays * 100 = rate * activeDays * 10000
        val rawResult = scaled / totalDays // this is result * 10000

        // Round HALF_UP from 4dp to 2dp
        val remainder = rawResult % 100
        val truncated = rawResult / 100 // result * 100
        val rounded = if (remainder >= 50) truncated + 1 else truncated

        return centsToString(rounded)
    }

    /**
     * Returns the number of days in the given month/year.
     */
    fun daysInMonth(year: Int, month: Int): Int {
        val m = Month(month)
        return when (m) {
            Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
            Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
            Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
            Month.FEBRUARY -> if (isLeapYear(year)) 29 else 28
        }
    }

    fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    /**
     * Parses "YYYY-MM" into (year, month).
     */
    fun parsePeriod(period: String): Pair<Int, Int> {
        val parts = period.split("-")
        require(parts.size == 2) { "Period must be in YYYY-MM format, got: $period" }
        return parts[0].toInt() to parts[1].toInt()
    }

    /**
     * Parses a decimal string like "1500.00" or "1500" into integer cents (×100).
     */
    private fun parseDecimalToCents(value: String): Int {
        val trimmed = value.trim()
        val dotIndex = trimmed.indexOf('.')
        return if (dotIndex < 0) {
            trimmed.toInt() * 100
        } else {
            val intPart = trimmed.substring(0, dotIndex).toInt()
            val fracStr = trimmed.substring(dotIndex + 1).padEnd(2, '0').take(2)
            intPart * 100 + fracStr.toInt()
        }
    }

    /**
     * Converts cents (×100) back to a formatted decimal string with 2dp.
     */
    private fun centsToString(cents: Long): String {
        val sign = if (cents < 0) "-" else ""
        val absCents = if (cents < 0) -cents else cents
        val intPart = absCents / 100
        val fracPart = absCents % 100
        return "$sign$intPart.${fracPart.toString().padStart(2, '0')}"
    }

    /**
     * Ensures a rate string is formatted to 2dp.
     */
    private fun formatDecimal(value: String): String {
        val cents = parseDecimalToCents(value).toLong()
        return centsToString(cents)
    }
}
