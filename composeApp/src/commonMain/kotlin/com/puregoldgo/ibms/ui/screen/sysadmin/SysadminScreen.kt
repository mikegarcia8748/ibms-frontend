package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.SegmentedTabRow
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_brand
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_brand_suffix
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_logout
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_logout_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_tab_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_tab_directory
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_tab_stores
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The sysadmin control panel — delegations, branch locations and the ISP
 * accounts database, behind one segmented switch.
 *
 * Runs on sample data this pass; see [SysadminSampleData] for why.
 */
@Composable
fun SysadminScreen(
    onSignedOut: () -> Unit,
    viewModel: SysadminViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SysadminUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    SysadminContent(
        uiState = uiState,
        callback = SysadminCallback(
            onTabSelect = viewModel::onTabSelect,
            onUserQueryChange = viewModel::onUserQueryChange,
            onBranchQueryChange = viewModel::onBranchQueryChange,
            onBranchLetterSelect = viewModel::onBranchLetterSelect,
            onBranchProviderSelect = viewModel::onBranchProviderSelect,
            onAccountQueryChange = viewModel::onAccountQueryChange,
            onAccountProviderSelect = viewModel::onAccountProviderSelect,
            onBulkUploadClick = viewModel::onBulkUploadClick,
            onBulkImportFilePicked = viewModel::onBulkImportFilePicked,
            onBulkImportStart = viewModel::onBulkImportStart,
            onBulkUploadDismiss = viewModel::onBulkUploadDismiss,
            onAddUserClick = viewModel::onAddUserClick,
            onNewUserFirstNameChange = viewModel::onNewUserFirstNameChange,
            onNewUserMiddleInitialChange = viewModel::onNewUserMiddleInitialChange,
            onNewUserLastNameChange = viewModel::onNewUserLastNameChange,
            onNewUserUsernameChange = viewModel::onNewUserUsernameChange,
            onNewUserEmployeeNumberChange = viewModel::onNewUserEmployeeNumberChange,
            onNewUserRoleChange = viewModel::onNewUserRoleChange,
            onAddUserSubmit = viewModel::onAddUserSubmit,
            onAddUserDismiss = viewModel::onAddUserDismiss,
            onResetPasswordClick = viewModel::onResetPasswordClick,
            onResetPasswordConfirm = viewModel::onResetPasswordConfirm,
            onResetPasswordDismiss = viewModel::onResetPasswordDismiss,
            onChangeRoleClick = viewModel::onChangeRoleClick,
            onRoleSelectionChange = viewModel::onRoleSelectionChange,
            onChangeRoleConfirm = viewModel::onChangeRoleConfirm,
            onChangeRoleDismiss = viewModel::onChangeRoleDismiss,
            onUserStatusToggleClick = viewModel::onUserStatusToggleClick,
            onUserStatusConfirm = viewModel::onUserStatusConfirm,
            onUserStatusDismiss = viewModel::onUserStatusDismiss,
            onRetryLoad = { viewModel.loadPanel() },
            onLogoutClick = viewModel::onLogout,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` rather than private so [SysadminScreenPreview.kt] can draw it.
 */
@Composable
internal fun SysadminContent(
    uiState: SysadminUIState,
    callback: SysadminCallback,
) {
    Scaffold(
        topBar = {
            SysadminAppBar(
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

                    SysadminHeader(
                        isCompact = isCompact,
                        onBulkUploadClick = callback.onBulkUploadClick,
                    )

                    Spacer(Modifier.height(Dimensions.viewPadding24))

                    SegmentedTabRow(
                        tabs = listOf(
                            SysadminTab.Directory to
                                stringResource(Res.string.sysadmin_tab_directory),
                            SysadminTab.Stores to
                                stringResource(Res.string.sysadmin_tab_stores),
                            SysadminTab.Accounts to
                                stringResource(Res.string.sysadmin_tab_accounts),
                        ),
                        selected = uiState.selectedTab,
                        onSelect = callback.onTabSelect,
                        isCompact = isCompact,
                    )

                    Spacer(Modifier.height(Dimensions.viewPadding24))

                    when (uiState.selectedTab) {
                        SysadminTab.Directory -> DirectoryTab(uiState, callback, isCompact)
                        SysadminTab.Stores -> StoresTab(uiState, callback, isCompact)
                        SysadminTab.Accounts -> AccountsTab(uiState, callback, isCompact)
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

            // Mutually exclusive by construction: opening any of them resets the
            // whole user-admin block, so a credential on screen always belongs
            // to whichever one is showing.
            val userAdmin = uiState.userAdmin
            when {
                userAdmin.isAddOpen ->
                    AddUserDialog(uiState = userAdmin, callback = callback)

                userAdmin.resetTarget != null ->
                    ResetPasswordDialog(uiState = userAdmin, callback = callback)

                userAdmin.roleTarget != null ->
                    ChangeRoleDialog(uiState = userAdmin, callback = callback)

                userAdmin.statusTarget != null ->
                    UserStatusDialog(uiState = userAdmin, callback = callback)
            }
        }
    }
}

/**
 * The dark bar across the top.
 *
 * `inverseSurface` rather than a hand-picked near-black: it is the scheme's own
 * answer to "a dark surface in a light app", so it stays coherent with the rest
 * of the theme and inverts correctly if a dark scheme is ever switched on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SysadminAppBar(
    userName: String,
    userRole: String,
    onLogoutClick: () -> Unit,
) = BoxWithConstraints {
    // The bar sits in the Scaffold's topBar, outside the body's own
    // measurement, so it has to ask about width itself. Measured out here
    // rather than inside a slot because both the title and the actions need
    // the answer.
    val isCompact = maxWidth <= Dimensions.viewWidth600

    TopAppBar(
        // `TopAppBar` defaults to the surface colours; the inverse pair is what
        // makes this the dark bar the rest of the theme expects.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
        actions = {
            val logoutDescription =
                stringResource(Res.string.console_logout_content_description)

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
                    Text(stringResource(Res.string.console_logout))
                }
            }
        },
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.viewHeight64),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_puregold_logo),
                    contentDescription = stringResource(Res.string.login_logo_content_description),
                    modifier = Modifier.size(Dimensions.viewSize32),
                )

                Text(
                    text = stringResource(Res.string.console_brand),
                    style = MaterialTheme.typography.titleMedium,
                )

                // First thing to go when the bar is tight: it is decoration, and
                // dropping it keeps the identity and the sign-out both readable.
                if (!isCompact) {
                    Text(
                        text = stringResource(Res.string.console_brand_suffix),
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
            }
        },
    )
}

/** Title, supporting line, and the bulk-upload action. Stacks when narrow. */
@Composable
private fun SysadminHeader(
    isCompact: Boolean,
    onBulkUploadClick: () -> Unit,
) {
    @Composable
    fun titleBlock() {
        Column {
            Text(
                text = stringResource(Res.string.sysadmin_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Dimensions.viewPadding4))
            Text(
                text = stringResource(Res.string.sysadmin_subtitle),
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
            Text(stringResource(Res.string.sysadmin_bulk_upload))
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

/** Opacity for de-emphasised text and hairlines on the dark bar. */
private const val ALPHA_MUTED = 0.7f
private const val ALPHA_DIVIDER = 0.3f
