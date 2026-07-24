package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.puregoldgo.ibms.ui.theme.AppTheme

/**
 * Every `@Preview` for the topsheet detail screen.
 *
 * Mirrors [SecretaryScreenPreview]: the screen file is long enough, and a stack
 * of preview functions between the reader and the composables they exercise is
 * noise. Nothing here ships — it draws [TopSheetDetailContent] against
 * hand-crafted sample state.
 */

// region Sample data

private val sampleHeader = TopSheetRow(
    id = "ts-glob-202607",
    invoiceNumber = "GLOB-202607-0007",
    providerName = "Globe",
    period = "2026-07",
    generatedOn = "7/19/2026",
    accountCount = 214,
    totalValidated = "760,172.68",
    status = TopSheetRecordStatus.Compiled,
)

private val sampleLines = listOf(
    TopSheetLineRow(
        accountId = "acc-1",
        rfpNumber = "1001",
        storeCode = "3041",
        storeName = "BALIBAGO (Sta. Rosa Laguna)",
        accountNumber = "ZTEGCE4188D1",
        circuitId = "CIR-00182",
        fullMrc = "2,489.99",
        prorated = "2,074.99",
        proratedCents = 207499,
        rfpSortOrder = 1,
    ),
    TopSheetLineRow(
        accountId = "acc-2",
        rfpNumber = "1002",
        storeCode = "5823",
        storeName = "ALAPAN 1B (Imus Cavite)",
        accountNumber = "443086277",
        circuitId = "CIR-00391",
        fullMrc = "3,150.00",
        prorated = "3,150.00",
        proratedCents = 315000,
        rfpSortOrder = 2,
    ),
    TopSheetLineRow(
        accountId = "acc-3",
        rfpNumber = null,
        storeCode = "1205",
        storeName = "888 CHINA TOWN SQUARE",
        accountNumber = "IC-AUZ-1874",
        circuitId = null,
        fullMrc = "1,998.00",
        prorated = "1,532.47",
        proratedCents = 153247,
        rfpSortOrder = 3,
    ),
    TopSheetLineRow(
        accountId = "acc-4",
        rfpNumber = "1004",
        storeCode = "7710",
        storeName = "ADMIRAL (Las Pinas)",
        accountNumber = "FBR-9912-XPQ",
        circuitId = "CIR-00587",
        fullMrc = "2,750.00",
        prorated = "2,750.00",
        proratedCents = 275000,
        rfpSortOrder = 4,
    ),
    TopSheetLineRow(
        accountId = "acc-5",
        rfpNumber = "1005",
        storeCode = "4492",
        storeName = "MOLINO (Bacoor Cavite)",
        accountNumber = "GLOB-4492-001",
        circuitId = "CIR-00612",
        fullMrc = "4,200.00",
        prorated = "3,850.00",
        proratedCents = 385000,
        rfpSortOrder = 5,
    ),
)

// endregion

// region Helpers

private fun previewState(
    lines: List<TopSheetLineRow> = sampleLines,
    isLoading: Boolean = false,
    error: String? = null,
    query: String = "",
    sortAsc: Boolean = true,
) = TopSheetDetailUIState(
    header = sampleHeader,
    lines = lines,
    isLoadingLines = isLoading,
    linesError = error,
    lineQuery = query,
    lineSortAsc = sortAsc,
)

private fun previewCallback() = TopSheetDetailCallback(
    onBackClick = {},
    onLineQueryChange = {},
    onLineSortSelect = {},
    onLineSortDirectionToggle = {},
    onAccountClick = {},
    onAccountDetailDismiss = {},
    onRetryLines = {},
)

// endregion

// region Desktop previews

@Preview(name = "TopSheet Detail — populated", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun TopSheetDetailPopulatedPreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "TopSheet Detail — loading", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun TopSheetDetailLoadingPreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(isLoading = true),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "TopSheet Detail — error", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun TopSheetDetailErrorPreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(error = "The account lines could not be loaded."),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "TopSheet Detail — empty", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun TopSheetDetailEmptyPreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(lines = emptyList()),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "TopSheet Detail — no match", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun TopSheetDetailNoMatchPreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(query = "zzzz"),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "TopSheet Detail — descending", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun TopSheetDetailDescendingPreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(sortAsc = false),
            callback = previewCallback(),
        )
    }
}

// endregion

// region Mobile previews

@Preview(name = "TopSheet Detail — populated", group = "MobileApp")
@Composable
private fun TopSheetDetailPopulatedMobilePreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "TopSheet Detail — empty", group = "MobileApp")
@Composable
private fun TopSheetDetailEmptyMobilePreview() {
    AppTheme {
        TopSheetDetailContent(
            uiState = previewState(lines = emptyList()),
            callback = previewCallback(),
        )
    }
}

// endregion
