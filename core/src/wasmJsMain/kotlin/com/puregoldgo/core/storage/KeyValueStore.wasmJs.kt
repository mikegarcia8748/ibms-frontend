package com.puregoldgo.core.storage

/**
 * Browser store backed by `localStorage`.
 *
 * Unlike the mobile targets there is no OS-level secret store to reach for: a
 * refresh token kept here is readable by any script running on the origin. It is
 * still the right place for it — the alternative is signing the user out on every
 * reload — but it is why the backend rotates the token on every use, so a copy
 * that leaks is spent the moment the legitimate client refreshes.
 *
 * Values are read back through a `?? ''` so the interop stays on non-null
 * strings; an absent key and an empty value are equivalent for our one key.
 */
private fun localStorageRead(key: String): String =
    js("(globalThis.localStorage.getItem(key) ?? '')")

private fun localStorageWrite(key: String, value: String): Unit =
    js("globalThis.localStorage.setItem(key, value)")

private fun localStorageRemove(key: String): Unit =
    js("globalThis.localStorage.removeItem(key)")

private class LocalStorageKeyValueStore : KeyValueStore {

    private val fallback = InMemoryKeyValueStore()

    /** Private browsing modes can make `localStorage` throw on access. */
    private inline fun <T> guarded(fallbackValue: () -> T, block: () -> T): T =
        try {
            block()
        } catch (_: Throwable) {
            fallbackValue()
        }

    override fun read(key: String): String? = guarded({ fallback.read(key) }) {
        localStorageRead(prefixed(key)).takeIf { it.isNotEmpty() }
    }

    override fun write(key: String, value: String) = guarded({ fallback.write(key, value) }) {
        localStorageWrite(prefixed(key), value)
    }

    override fun remove(key: String) = guarded({ fallback.remove(key) }) {
        localStorageRemove(prefixed(key))
    }

    private fun prefixed(key: String) = "$SESSION_STORE_NAME.$key"
}

actual fun createKeyValueStore(): KeyValueStore = LocalStorageKeyValueStore()
