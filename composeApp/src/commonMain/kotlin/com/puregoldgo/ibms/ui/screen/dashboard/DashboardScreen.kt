package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.theme.AppTheme
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_brand
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_brand_suffix
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_bulk_upload
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_logout
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_logout_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_tab_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_tab_delegations
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_tab_stores
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The sysadmin control panel — delegations, branch locations and the ISP
 * accounts database, behind one segmented switch.
 *
 * Runs on sample data this pass; see [DashboardSampleData] for why.
 */
@Composable
fun DashboardScreen(
    onSignedOut: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DashboardUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    DashboardContent(
        uiState = uiState,
        callback = DashboardCallback(
            onTabSelect = viewModel::onTabSelect,
            onBranchQueryChange = viewModel::onBranchQueryChange,
            onBranchLetterSelect = viewModel::onBranchLetterSelect,
            onBranchProviderSelect = viewModel::onBranchProviderSelect,
            onAccountQueryChange = viewModel::onAccountQueryChange,
            onAccountProviderSelect = viewModel::onAccountProviderSelect,
            onBulkUploadClick = viewModel::onBulkUploadClick,
            onBulkImportFilePicked = viewModel::onBulkImportFilePicked,
            onBulkImportStart = viewModel::onBulkImportStart,
            onBulkUploadDismiss = viewModel::onBulkUploadDismiss,
            onRetryLoad = { viewModel.loadPanel() },
            onLogoutClick = viewModel::onLogout,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 */
@Composable
private fun DashboardContent(
    uiState: DashboardUIState,
    callback: DashboardCallback,
) {
    Scaffold(
        topBar = {
            DashboardAppBar(
                userName = uiState.userName,
                userRole = uiState.userRole,
                onLogoutClick = callback.onLogoutClick,
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val isCompact = maxWidth <= Dimensions.viewWidth600
            val contentPadding = if (isCompact) {
                Dimensions.viewPadding16
            } else {
                Dimensions.viewPadding32
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    // A control panel stretched across an ultrawide browser is
                    // unreadable; cap it and centre the column instead.
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth1200),
                ) {
                    Spacer(Modifier.height(Dimensions.viewPadding32))

                    DashboardHeader(
                        isCompact = isCompact,
                        onBulkUploadClick = callback.onBulkUploadClick,
                    )

                    Spacer(Modifier.height(Dimensions.viewPadding24))

                    DashboardTabRow(
                        selected = uiState.selectedTab,
                        onSelect = callback.onTabSelect,
                        isCompact = isCompact,
                    )

                    Spacer(Modifier.height(Dimensions.viewPadding24))

                    when (uiState.selectedTab) {
                        DashboardTab.Delegations -> DelegationsTab(uiState, isCompact)
                        DashboardTab.Stores -> StoresTab(uiState, callback, isCompact)
                        DashboardTab.Accounts -> AccountsTab(uiState, callback, isCompact)
                    }

                    Spacer(Modifier.height(Dimensions.viewPadding48))
                }
            }

            if (uiState.isBulkImportOpen) {
                BulkImportDialog(
                    uiState = uiState,
                    onFilePicked = callback.onBulkImportFilePicked,
                    onStartImport = callback.onBulkImportStart,
                    onDismiss = callback.onBulkUploadDismiss,
                )
            }
        }
    }
}

/**
 * The dark bar across the top.
 *
 * `inverseSurface` rather than a hand-picked near-black: it is the scheme's own
 * answer to "a dark surface in a light app", so it stays coherent with the rest
 * of [AppTheme] and inverts correctly if a dark scheme is ever switched on.
 */
@Composable
private fun DashboardAppBar(
    userName: String,
    userRole: String,
    onLogoutClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        BoxWithConstraints {
            // The bar sits in the Scaffold's topBar, outside the body's own
            // measurement, so it has to ask about width itself.
            val isCompact = maxWidth <= Dimensions.viewWidth600

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.viewHeight64)
                    .padding(horizontal = Dimensions.viewPadding16),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_puregold_logo),
                    contentDescription = stringResource(Res.string.login_logo_content_description),
                    modifier = Modifier.size(Dimensions.viewSize32),
                )

                Text(
                    text = stringResource(Res.string.dashboard_brand),
                    style = MaterialTheme.typography.titleMedium,
                )

                // First thing to go when the bar is tight: it is decoration, and
                // dropping it keeps the identity and the sign-out both readable.
                if (!isCompact) {
                    Text(
                        text = stringResource(Res.string.dashboard_brand_suffix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = ALPHA_MUTED),
                    )
                }

                if (userName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .width(Dimensions.viewStroke1)
                            .height(Dimensions.viewHeight24)
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface
                                    .copy(alpha = ALPHA_DIVIDER),
                            ),
                    )

                    Column(
                        // Yields space to the sign-out button rather than
                        // pushing it off the edge; a long name ellipsizes.
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = userRole.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                                .copy(alpha = ALPHA_MUTED),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                val logoutDescription =
                    stringResource(Res.string.dashboard_logout_content_description)

                if (isCompact) {
                    // Icon only — the label does not fit, and the content
                    // description keeps it named for a screen reader.
                    IconButton(onClick = onLogoutClick) {
                        Icon(
                            imageVector = AppIcons.Logout,
                            contentDescription = logoutDescription,
                            modifier = Modifier.size(Dimensions.viewSize20),
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                } else {
                    TextButton(
                        onClick = onLogoutClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        ),
                    ) {
                        Icon(
                            imageVector = AppIcons.Logout,
                            contentDescription = logoutDescription,
                            modifier = Modifier.size(Dimensions.viewSize18),
                        )
                        Spacer(Modifier.width(Dimensions.viewPadding8))
                        Text(stringResource(Res.string.dashboard_logout))
                    }
                }
            }
        }
    }
}

/** Title, supporting line, and the bulk-upload action. Stacks when narrow. */
@Composable
private fun DashboardHeader(
    isCompact: Boolean,
    onBulkUploadClick: () -> Unit,
) {
    @Composable
    fun titleBlock() {
        Column {
            Text(
                text = stringResource(Res.string.dashboard_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Dimensions.viewPadding4))
            Text(
                text = stringResource(Res.string.dashboard_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @Composable
    fun uploadButton(modifier: Modifier = Modifier) {
        Button(
            onClick = onBulkUploadClick,
            modifier = modifier.height(Dimensions.viewHeight48),
            shape = RoundedCornerShape(Dimensions.viewRadius8),
        ) {
            Icon(
                imageVector = AppIcons.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.viewSize18),
            )
            Spacer(Modifier.width(Dimensions.viewPadding8))
            Text(stringResource(Res.string.dashboard_bulk_upload))
        }
    }

    if (isCompact) {
        Column(modifier = Modifier.fillMaxWidth()) {
            titleBlock()
            Spacer(Modifier.height(Dimensions.viewPadding16))
            uploadButton(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            titleBlock()
            uploadButton()
        }
    }
}

/**
 * The three-way switch.
 *
 * Built from plain surfaces rather than `TabRow`: these are free-width pills
 * inside a tray, not M3 tabs with an indicator, and forcing the component to
 * look like this costs more than drawing it.
 */
@Composable
private fun DashboardTabRow(
    selected: DashboardTab,
    onSelect: (DashboardTab) -> Unit,
    isCompact: Boolean,
) {
    val tabs = listOf(
        DashboardTab.Delegations to stringResource(Res.string.dashboard_tab_delegations),
        DashboardTab.Stores to stringResource(Res.string.dashboard_tab_stores),
        DashboardTab.Accounts to stringResource(Res.string.dashboard_tab_accounts),
    )

    val row = @Composable {
        Surface(
            shape = RoundedCornerShape(Dimensions.viewRadius12),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(Dimensions.viewPadding6),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding4),
            ) {
                tabs.forEach { (tab, label) ->
                    val isSelected = tab == selected
                    Surface(
                        modifier = Modifier.clickable { onSelect(tab) },
                        shape = RoundedCornerShape(Dimensions.viewRadius8),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        tonalElevation = if (isSelected) {
                            Dimensions.viewElevation2
                        } else {
                            Dimensions.viewElevation0
                        },
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(
                                horizontal = Dimensions.viewPadding16,
                                vertical = Dimensions.viewPadding10,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (isCompact) {
        // Three labels this long do not fit a phone; let the tray scroll rather
        // than truncating the one that is off-screen.
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) { row() }
    } else {
        Row { row() }
    }
}

/** Opacity for de-emphasised text and hairlines on the dark bar. */
private const val ALPHA_MUTED = 0.7f
private const val ALPHA_DIVIDER = 0.3f

@Preview(name = "Dashboard — delegations", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardDelegationsWidePreview() {
    AppTheme {
        DashboardContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Dashboard — stores", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardStoresWidePreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(selectedTab = DashboardTab.Stores),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — accounts", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardAccountsWidePreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(selectedTab = DashboardTab.Accounts),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — delegations", group = "MobileApp")
@Composable
private fun DashboardDelegationsPreview() {
    AppTheme {
        DashboardContent(uiState = previewState(), callback = previewCallback())
    }
}

@Preview(name = "Dashboard — stores", group = "MobileApp")
@Composable
private fun DashboardStoresPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(selectedTab = DashboardTab.Stores),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — empty states", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardEmptyPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                // No pending user, and a branch query that matches nothing.
                users = DashboardSampleData.users.filter { it.role != Role.PENDING },
                branchQuery = "zzzz",
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — import, no file", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardBulkImportEmptyPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(isBulkImportOpen = true),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — import, file chosen", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardBulkImportChosenPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "Globe Accounts.xlsx",
                bulkImportFileSize = 48_128,
            ),
            callback = previewCallback(),
        )
    }
}

@Preview(name = "Dashboard — import summary", group = "WebApp", device = Devices.DESKTOP)
@Composable
private fun DashboardBulkImportSummaryPreview() {
    AppTheme {
        DashboardContent(
            uiState = previewState().copy(
                isBulkImportOpen = true,
                bulkImportFileName = "Globe Accounts.xlsx",
                bulkImportFileSize = 48_128,
                bulkImportSummary = DashboardSampleData.bulkImportSummary,
            ),
            callback = previewCallback(),
        )
    }
}

private fun previewState() = DashboardUIState(
    userName = "Michael Garcia",
    userRole = "sysadmin",
    users = DashboardSampleData.users,
    activeProviders = DashboardSampleData.activeProviders,
    inactiveProviders = DashboardSampleData.inactiveProviders,
    branches = DashboardSampleData.branches,
    accounts = DashboardSampleData.accounts,
)

private fun previewCallback() = DashboardCallback(
    onTabSelect = {},
    onBranchQueryChange = {},
    onBranchLetterSelect = {},
    onBranchProviderSelect = {},
    onAccountQueryChange = {},
    onAccountProviderSelect = {},
    onBulkUploadClick = {},
    onBulkImportFilePicked = {},
    onBulkImportStart = {},
    onBulkUploadDismiss = {},
    onRetryLoad = {},
    onLogoutClick = {},
)
