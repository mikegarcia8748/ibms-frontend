package com.puregoldgo.ibms.ui.screen.sysadmin.directory

import androidx.compose.runtime.Immutable
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.UserStatus

/**
 * The rows the directory panel draws. See `SysadminModels.kt` for why these are
 * not the shared domain models.
 */

@Immutable
data class DirectoryUser(
    val id: String,
    val name: String,
    val username: String,
    val employeeNumber: String?,
    val role: Role,
    val status: UserStatus,
    /**
     * True while the account is still holding a temporary password.
     *
     * The backend flips this to false the moment the user sets a permanent one,
     * which is why the row can say "this password is still temporary" without
     * the password itself ever being readable again. See [IssuedCredential].
     */
    val mustChangePassword: Boolean,
) {
    /** The avatar letter. Falls back to the username so a blank name still renders. */
    val initial: String
        get() = (name.firstOrNull() ?: username.firstOrNull() ?: '?').uppercase()

    val isActive: Boolean
        get() = status == UserStatus.ACTIVE
}

/**
 * A temporary password, in the one moment it is readable.
 *
 * The backend stores only a bcrypt hash, so this arrives exactly once — in the
 * response to the provision or reset that minted it — and can never be fetched
 * again. It therefore lives in UI state and nowhere else: not in `SessionStore`,
 * not in `CurrentUserStore`, not in any saved-state bundle. It is dropped when
 * the dialog showing it closes, and when the panel holding it is torn down —
 * which is what sign-out does. See `DirectoryViewModel.onCleared`.
 *
 * [isNewUser] only picks the wording — provisioned versus re-issued.
 */
@Immutable
data class IssuedCredential(
    val username: String,
    val name: String,
    val temporaryPassword: String,
    val expiresAt: String,
    val isNewUser: Boolean,
)
