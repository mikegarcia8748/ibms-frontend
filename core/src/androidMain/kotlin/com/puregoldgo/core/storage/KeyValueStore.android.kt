package com.puregoldgo.core.storage

import android.annotation.SuppressLint
import android.content.Context

/**
 * Application context for the platform stores.
 *
 * Set it once from the Android entry point (`MainActivity.onCreate`) before the
 * first API call. Until it is set, [createKeyValueStore] degrades to memory
 * rather than throwing — a missed initialization should cost a re-login, not a
 * crash on launch.
 */
object AndroidPlatformContext {
    @SuppressLint("StaticFieldLeak") // application context only — no activity is retained
    @Volatile
    var applicationContext: Context? = null
}

/**
 * Resolves SharedPreferences lazily on every call rather than once at
 * construction, so a store created before [AndroidPlatformContext] is populated
 * starts persisting as soon as it is.
 */
private class AndroidKeyValueStore : KeyValueStore {

    private val fallback = InMemoryKeyValueStore()

    private fun prefs() = AndroidPlatformContext.applicationContext
        ?.getSharedPreferences(SESSION_STORE_NAME, Context.MODE_PRIVATE)

    override fun read(key: String): String? =
        prefs()?.getString(key, null) ?: fallback.read(key)

    override fun write(key: String, value: String) {
        val prefs = prefs()
        if (prefs == null) {
            fallback.write(key, value)
        } else {
            prefs.edit().putString(key, value).apply()
        }
    }

    override fun remove(key: String) {
        fallback.remove(key)
        prefs()?.edit()?.remove(key)?.apply()
    }
}

actual fun createKeyValueStore(): KeyValueStore = AndroidKeyValueStore()
