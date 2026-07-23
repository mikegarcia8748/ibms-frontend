package com.puregoldgo.core.network

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.logging.Logger

/** Tag every HTTP log line carries, so traffic is easy to filter for. */
private const val HTTP_LOG_TAG = "HttpClient"

/**
 * Routes Ktor's [io.ktor.client.plugins.logging.Logging] plugin output through
 * Napier — the app's standard logger — so HTTP traces land in the same place as
 * every other log line (browser console on wasmJs, Logcat on Android).
 */
internal class NapierKtorLogger : Logger {
    override fun log(message: String) {
        Napier.v(message = message, tag = HTTP_LOG_TAG)
    }
}

private var napierInitialized = false

/**
 * Registers a debug antilog with Napier exactly once, so logging actually has a
 * sink. Nothing else in the app calls [Napier.base], which means without this
 * every `Napier.x(...)` call — including the network layer's own error logs — is
 * silently dropped.
 *
 * Idempotent: safe to call from each client factory; only the first call takes
 * effect. Callers gate this on [ApiConfig.isProduction] so production builds
 * install no logging at all.
 */
internal fun initNetworkLogging() {
    if (napierInitialized) return
    napierInitialized = true
    Napier.base(DebugAntilog())
}
