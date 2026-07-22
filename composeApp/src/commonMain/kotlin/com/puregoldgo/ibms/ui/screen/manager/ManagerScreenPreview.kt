package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the oversight console.
 *
 * Kept beside [ManagerScreen] rather than inside it, the convention the other
 * consoles follow. Nothing here ships — it draws [ManagerContent] against
 * [ManagerSampleData].
 */

@Preview(name = "Manager — overview", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun ManagerOverviewWidePreview() {
    AppTheme {
        ManagerContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Manager — topsheets", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun ManagerTopSheetsWidePreview() {
    AppTheme {
        ManagerContent(
            uiState = previewState().copy(selectedTab = ManagerTab.TopSheets),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Manager — activity", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun ManagerActivityWidePreview() {
    AppTheme {
        ManagerContent(
            uiState = previewState().copy(selectedTab = ManagerTab.Activity),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Manager — nothing compiled", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun ManagerEmptyPreview() {
    AppTheme {
        ManagerContent(
            uiState = ManagerUIState(userName = "Michael Garcia", userRole = "manager"),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Manager — overview", group = "MobileApp")
@Composable
private fun ManagerOverviewPreview() {
    AppTheme {
        ManagerContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Manager — activity", group = "MobileApp")
@Composable
private fun ManagerActivityPreview() {
    AppTheme {
        ManagerContent(
            uiState = previewState().copy(selectedTab = ManagerTab.Activity),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = ManagerUIState(
    userName = "Michael Garcia",
    userRole = "manager",
    topSheets = ManagerSampleData.topSheets,
    activities = ManagerSampleData.activities,
)

private fun previewCallback() = ManagerCallback(
    onTabSelect = {},
    onTopSheetQueryChange = {},
    onActivityQueryChange = {},
    onRetryLoad = {},
    onLogoutClick = {},
)
