package com.puregoldgo.core.network

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [NetworkActivity] is what the global progress bar reads. Its whole contract is
 * that `begin`/`end` stay balanced — overlapping requests hold the bar up until
 * the last settles, and a stray `end` never drives the count below zero and hides
 * the bar while work is still running. These pin that down.
 *
 * The object is process-wide, so each case resets it to idle afterwards.
 */
class NetworkActivityTest {

    @AfterTest
    fun drain() {
        while (NetworkActivity.inFlight.value > 0) NetworkActivity.end()
    }

    @Test
    fun tc01_starts_idle() {
        assertEquals(0, NetworkActivity.inFlight.value)
    }

    @Test
    fun tc02_a_balanced_begin_and_end_returns_to_idle() {
        NetworkActivity.begin()
        assertEquals(1, NetworkActivity.inFlight.value)

        NetworkActivity.end()
        assertEquals(0, NetworkActivity.inFlight.value)
    }

    @Test
    fun tc03_overlapping_requests_each_hold_the_count() {
        NetworkActivity.begin()
        NetworkActivity.begin()
        NetworkActivity.begin()
        assertEquals(3, NetworkActivity.inFlight.value)

        // The bar stays up until the *last* of them finishes.
        NetworkActivity.end()
        assertEquals(2, NetworkActivity.inFlight.value)
        NetworkActivity.end()
        NetworkActivity.end()
        assertEquals(0, NetworkActivity.inFlight.value)
    }

    @Test
    fun tc04_an_extra_end_cannot_go_negative() {
        NetworkActivity.end()
        assertEquals(0, NetworkActivity.inFlight.value)

        // A later begin must still be the count's first request, not a recovery
        // from -1 that would leave the bar hidden while a call is in flight.
        NetworkActivity.begin()
        assertEquals(1, NetworkActivity.inFlight.value)
    }
}
