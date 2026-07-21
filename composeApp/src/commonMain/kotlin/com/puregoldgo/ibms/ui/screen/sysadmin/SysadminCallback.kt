package com.puregoldgo.ibms.ui.screen.sysadmin

/**
 * What the console shell can do.
 *
 * Two lambdas, where this was thirty. The rest moved to the panel that owns
 * them — `DirectoryCallback` and `RegistryCallback` — so a tab is handed only
 * the actions it can actually perform.
 */
data class SysadminCallback(
    val onTabSelect: (SysadminTab) -> Unit,
    val onLogoutClick: () -> Unit,
)
