package com.puregoldgo.ibms.ui.screen.access

import androidx.lifecycle.ViewModel
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.model.UserProfile
import com.puregoldgo.ibms.shared.model.UserStatus
import kotlinx.serialization.Serializable

/**
 * Why an authenticated user is being held at the door.
 *
 * Carried as a navigation argument rather than in memory: unlike the
 * change-password challenge there is no credential here, just a reason string,
 * so it is safe to persist in the back stack.
 */
@Serializable
enum class NoAccessReason {
    /** Provisioned but not yet given a role — `POST /users` defaults to `pending`. */
    PendingRole,

    /** The account was deactivated by a sysadmin. */
    Deactivated,
}

/** Which of the two doors closed on this user. */
fun UserProfile.noAccessReason(): NoAccessReason =
    if (status != UserStatus.ACTIVE) NoAccessReason.Deactivated else NoAccessReason.PendingRole

class NoAccessViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    /** Drops the stored session. There is no logout endpoint to call. */
    fun onSignOut() {
        repository.signOut()
    }
}
