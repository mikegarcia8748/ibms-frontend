package com.puregoldgo.core.network

import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A process-wide count of HTTP requests currently in flight.
 *
 * The per-section spinners tell you a *particular* list is loading; this tells
 * the app whether *anything* is talking to the backend, which is what the global
 * top progress bar draws. A count rather than a boolean so overlapping requests
 * — the secretary console fans out four at once — each hold the bar up until the
 * last of them settles.
 */
object NetworkActivity {
    private val _inFlight = MutableStateFlow(0)

    /** The number of requests currently outstanding. Zero means the app is idle. */
    val inFlight: StateFlow<Int> = _inFlight.asStateFlow()

    /** Marks a request as started. Pair every call with exactly one [end]. */
    fun begin() = _inFlight.update { it + 1 }

    /** Marks a request as finished. Floored at zero so a stray [end] cannot go negative. */
    fun end() = _inFlight.update { (it - 1).coerceAtLeast(0) }
}

/**
 * Counts every request the client sends into [NetworkActivity].
 *
 * Wraps the whole send/receive in the `Send` hook so the count covers the round
 * trip, and decrements in a `finally` that runs however the call ends — including
 * the wasmJs case where a dropped fetch surfaces as a `kotlin.Error` rather than
 * an `Exception`, which a `catch` would miss and leak the counter over.
 */
val NetworkActivityPlugin = createClientPlugin("NetworkActivity") {
    on(Send) { request ->
        NetworkActivity.begin()
        try {
            proceed(request)
        } finally {
            NetworkActivity.end()
        }
    }
}
