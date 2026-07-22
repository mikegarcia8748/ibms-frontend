package com.puregoldgo.ibms.shared.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BillingPeriodTest {

    @Test
    fun tc01_previous_steps_back_a_month() {
        assertEquals("2026-06", BillingPeriod.previous("2026-07"))
    }

    @Test
    fun tc02_previous_rolls_the_year_back_at_january() {
        assertEquals("2025-12", BillingPeriod.previous("2026-01"))
    }

    @Test
    fun tc03_next_steps_forward_a_month() {
        assertEquals("2026-08", BillingPeriod.next("2026-07"))
    }

    @Test
    fun tc04_next_rolls_the_year_forward_at_december() {
        assertEquals("2027-01", BillingPeriod.next("2026-12"))
    }

    @Test
    fun tc05_valid_accepts_well_formed_periods_and_rejects_junk() {
        assertTrue(BillingPeriod.isValid("2026-07"))
        assertFalse(BillingPeriod.isValid("2026-13"))
        assertFalse(BillingPeriod.isValid("2026"))
        assertFalse(BillingPeriod.isValid("not-a-month"))
    }

    @Test
    fun tc06_the_current_month_is_not_in_the_future_but_the_next_one_is() {
        val current = BillingPeriod.current()
        assertTrue(BillingPeriod.isValid(current))
        assertFalse(BillingPeriod.isFuture(current))
        assertFalse(BillingPeriod.isFuture(BillingPeriod.previous(current)))
        assertTrue(BillingPeriod.isFuture(BillingPeriod.next(current)))
    }
}
