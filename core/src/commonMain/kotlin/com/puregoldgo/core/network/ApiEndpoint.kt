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

    // ─── Users ───────────────────────────────────────────────────────────────
    data object Users : ApiEndpoint("/users")

    // ─── Providers ───────────────────────────────────────────────────────────
    data object Providers : ApiEndpoint("/providers")

    // ─── Stores ──────────────────────────────────────────────────────────────
    data object Stores : ApiEndpoint("/stores")

    // ─── Accounts ────────────────────────────────────────────────────────────
    data object Accounts : ApiEndpoint("/accounts")
    data object AccountsBulkImport : ApiEndpoint("/accounts/bulk-import")

    // ─── Attachments ───────────────────────────────────────────────────────────
    data object AttachmentsPresignUpload : ApiEndpoint("/attachments/presign/upload")

    // ─── Topsheets ─────────────────────────────────────────────────────────────
    data object TopsheetsPreview : ApiEndpoint("/topsheets/preview")
    data object TopsheetsDraft : ApiEndpoint("/topsheets/draft")

    /** Resolves this endpoint against [baseUrl], e.g. `http://localhost:8080/auth/login`. */
    fun url(baseUrl: String): String = baseUrl.trimEnd('/') + path

    /**
     * Id-scoped topsheet paths.
     *
     * Functions rather than [ApiEndpoint] objects because the id (and line id) are
     * runtime values — a `data object` can only hold a fixed path.
     */
    companion object {
        fun topsheetLines(baseUrl: String, id: String): String =
            "${baseUrl.trimEnd('/')}/topsheets/$id/lines"

        fun topsheetLine(baseUrl: String, id: String, lineId: String): String =
            "${baseUrl.trimEnd('/')}/topsheets/$id/lines/$lineId"

        fun topsheetAssignRfp(baseUrl: String, id: String): String =
            "${baseUrl.trimEnd('/')}/topsheets/$id/assign-rfp"

        fun topsheetConfirm(baseUrl: String, id: String): String =
            "${baseUrl.trimEnd('/')}/topsheets/$id/confirm"
    }
}
