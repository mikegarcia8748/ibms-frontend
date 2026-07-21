package com.puregoldgo.core.storage

/**
 * Who is signed in, for the length of the process.
 *
 * Deliberately not persisted, and deliberately not a mirror of the session. The
 * profile arrives attached to every response that mints a session — sign-in, the
 * forced password change, and the silent `/auth/refresh` on launch — so there is
 * never a moment where a valid session exists and this is empty. Writing it to
 * disk would only create a second copy to keep honest, and a stale one at that:
 * a role can be changed or revoked by a sysadmin between two launches.
 *
 * Fields are plain values rather than the shared `UserProfile` type because
 * `:core` sits below `:shared` and must not depend on it. [role] is the raw wire
 * value ("sysadmin", "secretary", …) for the same reason; callers that need the
 * enum map it themselves.
 *
 * This answers "who is this" only. Whether that user may do something is
 * `hasOperationalAccess()` in `:shared`, and the server remains the authority
 * either way — routing on [role] is a convenience, not an access control.
 */
object CurrentUserStore {

    var name: String? = null
        private set

    var username: String? = null
        private set

    var employeeNumber: String? = null
        private set

    /** Lowercase wire value, e.g. `"sysadmin"`. Null when nobody is signed in. */
    var role: String? = null
        private set

    fun save(
        name: String,
        username: String,
        role: String,
        employeeNumber: String? = null,
    ) {
        this.name = name
        this.username = username
        this.role = role
        this.employeeNumber = employeeNumber
    }

    fun clear() {
        name = null
        username = null
        role = null
        employeeNumber = null
    }
}
