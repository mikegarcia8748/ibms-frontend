package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.runtime.Immutable

/** Which of the three panels the control panel is showing. */
enum class SysadminTab { Directory, Stores, Accounts }

/**
 * The console shell's own state: who is signed in, and which panel is open.
 *
 * Deliberately this small. It used to be one 237-line class holding the staff
 * directory, the user-administration dialogs, both registry filters and the bulk
 * import — four unrelated jobs whose only shared field was the signed-in name in
 * the app bar. The panels now own their own state (`DirectoryUIState`,
 * `RegistryUIState`) and this holds what is genuinely the shell's.
 */
@Immutable
data class SysadminUIState(
    val userName: String = "",
    val userRole: String = "",
    val selectedTab: SysadminTab = SysadminTab.Directory,
)
