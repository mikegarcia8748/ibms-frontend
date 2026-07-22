package com.puregoldgo.ibms.ui.screen.manager

/**
 * Everything the oversight console can do, bundled so the content composables
 * stay free of the ViewModel and remain previewable.
 *
 * Short by design: the role has no write endpoint in the contract, so there is
 * nothing here but navigation, two searches and a retry.
 */
data class ManagerCallback(
    val onTabSelect: (ManagerTab) -> Unit,
    val onTopSheetQueryChange: (String) -> Unit,
    val onActivityQueryChange: (String) -> Unit,
    val onRetryLoad: () -> Unit,
    val onLogoutClick: () -> Unit,
)
