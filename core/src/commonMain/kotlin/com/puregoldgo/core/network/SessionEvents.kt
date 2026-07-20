package com.puregoldgo.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Announces that the session ended somewhere below the UI.
 *
 * Token refresh happens inside the HTTP client, so the screen that discovers a
 * dead session is whichever one happened to make a request — it has no business
 * deciding what to do about it. The client publishes here instead and the
 * navigation host, which does own that decision, resets to sign-in.
 */
object SessionEvents {

    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}
