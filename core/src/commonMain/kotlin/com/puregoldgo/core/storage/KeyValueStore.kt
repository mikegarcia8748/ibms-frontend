package com.puregoldgo.core.storage

/**
 * The smallest persistent store the app needs: string values, keyed by name.
 *
 * Only one thing is ever written through it — the refresh token — so the
 * interface stays deliberately narrow. Each platform backs it with the most
 * appropriate store it has (Keychain, SharedPreferences, Preferences,
 * localStorage); see the `actual` of [createKeyValueStore] per target.
 */
interface KeyValueStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}

/**
 * Fallback used when a platform store is unavailable (e.g. Android before the
 * application context is installed). Losing it costs the user one sign-in
 * rather than crashing the app, which is the right trade for a token store.
 */
class InMemoryKeyValueStore : KeyValueStore {
    private val values = mutableMapOf<String, String>()

    override fun read(key: String): String? = values[key]

    override fun write(key: String, value: String) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}

/** Name the platform store registers itself under, where the concept exists. */
const val SESSION_STORE_NAME: String = "com.puregoldgo.ibms.session"

expect fun createKeyValueStore(): KeyValueStore
