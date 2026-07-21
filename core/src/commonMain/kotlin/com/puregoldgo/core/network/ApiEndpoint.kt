package com.puregoldgo.core.network

/**
 * Paths for the IBMS backend REST API.
 *
 * These are paths, not absolute URLs — the base URL varies per environment and
 * is supplied by the caller via [url] rather than being baked in here.
 */
sealed class ApiEndpoint(val path: String) {

    // ─── Auth ────────────────────────────────────────────────────────────────
    data object Login : ApiEndpoint("/auth/login")
    data object Refresh : ApiEndpoint("/auth/refresh")
    data object PasswordChange : ApiEndpoint("/auth/password/change")
    data object CurrentUser : ApiEndpoint("/auth/me")
    data object Logout : ApiEndpoint("/auth/logout")

    // ─── Providers ───────────────────────────────────────────────────────────
    data object Providers : ApiEndpoint("/providers")

    // ─── Stores ──────────────────────────────────────────────────────────────
    data object Stores : ApiEndpoint("/stores")

    // ─── Accounts ────────────────────────────────────────────────────────────
    data object Accounts : ApiEndpoint("/accounts")
    data object AccountsBulkImport : ApiEndpoint("/accounts/bulk-import")

    /** Resolves this endpoint against [baseUrl], e.g. `http://localhost:8080/auth/login`. */
    fun url(baseUrl: String): String = baseUrl.trimEnd('/') + path
}
