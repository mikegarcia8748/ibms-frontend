package com.puregoldgo.core.storage

/**
 * The signed-in session, split by lifetime on purpose.
 *
 * The access token is short-lived and stays in memory: losing it on restart
 * costs one silent `/auth/refresh`, nothing more. The refresh token is what
 * keeps a user signed in across restarts, so it is the only piece written to
 * platform storage — and the backend rotates it on every use, so a copy that
 * outlives its turn is already worthless.
 *
 * A password-change challenge token is deliberately *not* handled here. It is
 * not a session credential and must never be persisted; it lives in
 * `ChallengeHolder` in memory for the length of the set-password screen.
 */
object SessionStore {

    private const val KEY_REFRESH_TOKEN = "refreshToken"

    private val store: KeyValueStore by lazy { createKeyValueStore() }

    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var refreshTokenLoaded = false

    fun accessToken(): String? = accessToken

    fun refreshToken(): String? {
        if (!refreshTokenLoaded) {
            refreshToken = store.read(KEY_REFRESH_TOKEN)?.takeIf { it.isNotBlank() }
            refreshTokenLoaded = true
        }
        return refreshToken
    }

    /** True when a restart can be resumed silently instead of showing sign-in. */
    fun hasPersistedSession(): Boolean = refreshToken() != null

    fun save(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        refreshTokenLoaded = true
        store.write(KEY_REFRESH_TOKEN, refreshToken)
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        refreshTokenLoaded = true
        store.remove(KEY_REFRESH_TOKEN)
    }
}
