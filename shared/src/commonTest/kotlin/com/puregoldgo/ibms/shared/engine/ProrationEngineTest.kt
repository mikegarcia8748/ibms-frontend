package com.puregoldgo.ibms.shared.engine

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ProrationEngineTest {

    @Test
    fun goldenCase_rate1000_install20_august2026() {
        // Golden test case from plan:
        // rate=1000, install=2026-08-20, period=2026-08 → 387.10
        // August has 31 days; active days = 31 - 20 + 1 = 12
        // 1000 * 12 / 31 = 387.096... rounds to 387.10
        val result = ProrationEngine.computeProration(
            rate = "1000",
            installDate = LocalDate(2026, 8, 20),
            periodYear = 2026,
            periodMonth = 8,
        )
        assertEquals("387.10", result)
    }

    @Test
    fun fullRate_whenInstalledBeforePeriod() {
        val result = ProrationEngine.computeProration(
            rate = "1500.00",
            installDate = LocalDate(2026, 5, 15),
            periodYear = 2026,
            periodMonth = 8,
        )
        assertEquals("1500.00", result)
    }

    @Test
    fun fullRate_whenInstalledOnFirstDayOfPeriod() {
        val result = ProrationEngine.computeProration(
            rate = "2000.00",
            installDate = LocalDate(2026, 8, 1),
            periodYear = 2026,
            periodMonth = 8,
        )
        assertEquals("2000.00", result)
    }

    @Test
    fun zero_whenInstalledAfterPeriod() {
        val result = ProrationEngine.computeProration(
            rate = "1000.00",
            installDate = LocalDate(2026, 9, 5),
            periodYear = 2026,
            periodMonth = 8,
        )
        assertEquals("0.00", result)
    }

    @Test
    fun lastDayInstallation_oneDay() {
        // Install on last day of 31-day month => 1 active day
        // 1000 * 1 / 31 = 32.258... rounds to 32.26
        val result = ProrationEngine.computeProration(
            rate = "1000",
            installDate = LocalDate(2026, 8, 31),
            periodYear = 2026,
            periodMonth = 8,
        )
        assertEquals("32.26", result)
    }

    @Test
    fun february_leapYear() {
        // 2024 is a leap year, Feb has 29 days
        // Install on Feb 15: active days = 29 - 15 + 1 = 15
        // 1000 * 15 / 29 = 517.241... rounds to 517.24
        val result = ProrationEngine.computeProration(
            rate = "1000",
            installDate = LocalDate(2024, 2, 15),
            periodYear = 2024,
            periodMonth = 2,
        )
        assertEquals("517.24", result)
    }

    @Test
    fun february_nonLeapYear() {
        // 2025 is not a leap year, Feb has 28 days
        // Install on Feb 15: active days = 28 - 15 + 1 = 14
        // 1000 * 14 / 28 = 500.00
        val result = ProrationEngine.computeProration(
            rate = "1000",
            installDate = LocalDate(2025, 2, 15),
            periodYear = 2025,
            periodMonth = 2,
        )
        assertEquals("500.00", result)
    }

    @Test
    fun periodStringOverload() {
        val result = ProrationEngine.computeProration(
            rate = "1000",
            installDate = LocalDate(2026, 8, 20),
            period = "2026-08",
        )
        assertEquals("387.10", result)
    }

    @Test
    fun daysInMonth_basics() {
        assertEquals(31, ProrationEngine.daysInMonth(2026, 1))
        assertEquals(28, ProrationEngine.daysInMonth(2026, 2))
        assertEquals(29, ProrationEngine.daysInMonth(2024, 2))
        assertEquals(30, ProrationEngine.daysInMonth(2026, 4))
        assertEquals(31, ProrationEngine.daysInMonth(2026, 12))
    }
}
