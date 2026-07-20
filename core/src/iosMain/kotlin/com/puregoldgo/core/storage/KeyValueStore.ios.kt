package com.puregoldgo.core.storage

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlock
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Keychain-backed store.
 *
 * The refresh token is the one credential that outlives the process, so it goes
 * to the Keychain rather than `NSUserDefaults`: it stays out of the app's plist
 * and out of unencrypted device backups. Items are stored
 * `kSecAttrAccessibleAfterFirstUnlock` — readable after the user has unlocked
 * once since boot, which is what a background refresh needs, without leaving the
 * token readable on a device that has never been unlocked.
 *
 * Writes are delete-then-add: that makes them an upsert, and it is why a rotated
 * token never leaves the previous one behind.
 *
 * Note the bridging style. A Kotlin `String` is not an `NSString` at the type
 * level, so it is converted explicitly via `NSString.create(string = …)` — an
 * `as NSString` cast compiles but throws at runtime.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class KeychainKeyValueStore(private val service: String) : KeyValueStore {

    private fun nsString(value: String): NSString = NSString.create(string = value)

    override fun read(key: String): String? = memScoped {
        val serviceRef = CFBridgingRetain(nsString(service))
        val accountRef = CFBridgingRetain(nsString(key))
        try {
            val query = newQuery(serviceRef, accountRef, capacity = 5)
            CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

            val found = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, found.ptr)
            CFRelease(query)

            if (status != errSecSuccess) return@memScoped null
            val data = CFBridgingRelease(found.value) as? NSData ?: return@memScoped null
            NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
        } finally {
            CFRelease(serviceRef)
            CFRelease(accountRef)
        }
    }

    override fun write(key: String, value: String) {
        val encoded = nsString(value).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val serviceRef = CFBridgingRetain(nsString(service))
        val accountRef = CFBridgingRetain(nsString(key))
        val valueRef = CFBridgingRetain(encoded)
        try {
            deleteItem(serviceRef, accountRef)
            val insert = newQuery(serviceRef, accountRef, capacity = 5)
            CFDictionaryAddValue(insert, kSecValueData, valueRef)
            CFDictionaryAddValue(insert, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
            SecItemAdd(insert, null)
            CFRelease(insert)
        } finally {
            CFRelease(serviceRef)
            CFRelease(accountRef)
            CFRelease(valueRef)
        }
    }

    override fun remove(key: String) {
        val serviceRef = CFBridgingRetain(nsString(service))
        val accountRef = CFBridgingRetain(nsString(key))
        try {
            deleteItem(serviceRef, accountRef)
        } finally {
            CFRelease(serviceRef)
            CFRelease(accountRef)
        }
    }

    private fun deleteItem(serviceRef: CPointer<out CPointed>?, accountRef: CPointer<out CPointed>?) {
        val query = newQuery(serviceRef, accountRef, capacity = 3)
        SecItemDelete(query)
        CFRelease(query)
    }

    /** The three attributes that identify one item: class, service, account. */
    private fun newQuery(
        serviceRef: CPointer<out CPointed>?,
        accountRef: CPointer<out CPointed>?,
        capacity: Long,
    ): CFMutableDictionaryRef? {
        val query = CFDictionaryCreateMutable(null, capacity, null, null)
        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrService, serviceRef)
        CFDictionaryAddValue(query, kSecAttrAccount, accountRef)
        return query
    }
}

actual fun createKeyValueStore(): KeyValueStore = KeychainKeyValueStore(SESSION_STORE_NAME)
