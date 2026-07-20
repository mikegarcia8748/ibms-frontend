package com.puregoldgo.core.storage

import java.util.prefs.Preferences

/**
 * Desktop/JVM store backed by user preferences. Not encrypted — the JVM target
 * exists for tooling and tests rather than for distribution to staff machines.
 */
private class PreferencesKeyValueStore : KeyValueStore {

    private val prefs: Preferences = Preferences.userRoot().node(SESSION_STORE_NAME)

    override fun read(key: String): String? = prefs.get(key, null)

    override fun write(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }

    override fun remove(key: String) {
        prefs.remove(key)
        prefs.flush()
    }
}

actual fun createKeyValueStore(): KeyValueStore = PreferencesKeyValueStore()
