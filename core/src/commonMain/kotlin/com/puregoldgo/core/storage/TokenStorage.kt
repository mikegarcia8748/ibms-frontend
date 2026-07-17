package com.puregoldgo.core.storage

/**
 * Simple in-memory token storage for the current JWT.
 * Replace with platform-specific secure storage (Keychain/Keystore) in a future iteration.
 */
object TokenStorage {
    private var token: String? = null

    fun getToken(): String? = token

    fun saveToken(newToken: String) {
        token = newToken
    }

    fun clearToken() {
        token = null
    }

    fun isLoggedIn(): Boolean = token != null
}
