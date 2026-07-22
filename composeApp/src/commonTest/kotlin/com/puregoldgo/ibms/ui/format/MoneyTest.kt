package com.puregoldgo.ibms.ui.format

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Peso grouping, which every console that shows money depends on.
 *
 * Followed `formatMoney` out of the sysadmin mappers when it was lifted for the
 * finance console to share.
 */
class MoneyTest {

    // region formatMoney

    @Test
    fun tc01_should_group_thousands() {
        assertEquals("2,489.99", formatMoney("2489.99"))
    }

    @Test
    fun tc02_should_leave_values_below_a_thousand_alone() {
        assertEquals("999.00", formatMoney("999.00"))
    }

    @Test
    fun tc03_should_group_every_three_digits() {
        assertEquals("1,234,567.89", formatMoney("1234567.89"))
    }

    @Test
    fun tc04_should_add_a_fraction_when_the_server_omits_one() {
        assertEquals("1,500.00", formatMoney("1500"))
    }

    @Test
    fun tc05_should_preserve_a_leading_group_of_one_digit() {
        assertEquals("1,000.00", formatMoney("1000.00"))
    }

    @Test
    fun tc06_should_return_a_malformed_value_untouched() {
        // Reformatting nonsense into something plausible would hide the problem.
        assertEquals("N/A", formatMoney("N/A"))
        assertEquals("12.3.4", formatMoney("12.3.4"))
    }

    @Test
    fun tc07_should_keep_the_sign_on_a_negative() {
        assertEquals("-1,200.50", formatMoney("-1200.50"))
    }

    @Test
    fun tc08_should_return_blank_input_unchanged() {
        assertEquals("", formatMoney("   "))
    }

    // endregion
}
