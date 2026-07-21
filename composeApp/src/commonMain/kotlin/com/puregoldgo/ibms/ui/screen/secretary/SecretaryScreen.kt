package com.puregoldgo.ibms.ui.screen.secretary

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_logout
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_logout_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_archive
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_billing
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_branches
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_compile
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_tab_floating
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The secretary console — topsheet compilation, the branch and ISP account
 * registries, billing history, and the two panels that catch what the others
 * leave behind.
 *
 * Runs on sample data this pass; see [SecretarySampleData] for why.
 */
@Composable
fun SecretaryScreen(
    onSignedOut: () -> Unit,
    viewModel: SecretaryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SecretaryUiEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    SecretaryContent(
        uiState = uiState,
        callback = SecretaryCallback(
            onTabSelect = viewModel::onTabSelect,
            onBranchQueryChange = viewModel::onBranchQueryChange,
            onBranchLetterSelect = viewModel::onBranchLetterSelect,
            onBranchProviderSelect = viewModel::onBranchProviderSelect,
            onAccountQueryChange = viewModel::onAccountQueryChange,
            onAccountLetterSelect = viewModel::onAccountLetterSelect,
            onAccountProviderSelect = viewModel::onAccountProviderSelect,
            onAccountStatusSelect = viewModel::onAccountStatusSelect,
            onExportAccounts = viewModel::onExportAccounts,
            onInvoiceQueryChange = viewModel::onInvoiceQueryChange,
            onAddBranchClick = viewModel::onAddBranchClick,
            onNewBranchCodeChange = viewModel::onNewBranchCodeChange,
            onNewBranchNameChange = viewModel::onNewBranchNameChange,
            onNewBranchCityChange = viewModel::onNewBranchCityChange,
            onNewBranchProviderChange = viewModel::onNewBranchProviderChange,
            onAddBranchSubmit = viewModel::onAddBranchSubmit,
            onAddBranchDismiss = viewModel::onAddBranchDismiss,
            onAddAccountClick = viewModel::onAddAccountClick,
            onNewAccountNumberChange = viewModel::onNewAccountNumberChange,
            onNewAccountStoreChange = viewModel::onNewAccountStoreChange,
            onNewAccountProviderChange = viewModel::onNewAccountProviderChange,
            onNewAccountRateChange = viewModel::onNewAccountRateChange,
            onAddAccountSubmit = viewModel::onAddAccountSubmit,
            onAddAccountDismiss = viewModel::onAddAccountDismiss,
            onRetryLoad = viewModel::loadPanel,
            onLogoutClick = viewModel::onLogout,
        ),
    )
}

/**
 * Pure UI content — no ViewModel dependency.
 *
 * `internal` rather than private so `SecretaryScreenPreview.kt` can draw it.
 */
@Composable
internal fun SecretaryContent(
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
) {
    Scaffold(
        topBar = {
            SecretaryAppBar(
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
            // The six labels fit only at the full capped width; anything
            // narrower has to scroll, well before the body needs its compact
            // layout. Measured against the same cap the content column uses.
            val isTabTrayCompact = maxWidth < Dimensions.viewWidth1200
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
                    // A console stretched across an ultrawide browser is
                    // unreadable; cap it and centre the column instead.
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth1200),
                ) {
                    Spacer(Modifier.height(Dimensions.viewPadding32))

                    SecretaryHeader()

                    Spacer(Modifier.height(Dimensions.viewPadding24))

                    SegmentedTabRow(
                        tabs = listOf(
                            SecretaryTab.CompileTopSheet to
                                stringResource(Res.string.secretary_tab_compile),
                            SecretaryTab.BranchLocations to
                                stringResource(Res.string.secretary_tab_branches),
                            SecretaryTab.IspAccounts to
                                stringResource(Res.string.secretary_tab_accounts),
                            SecretaryTab.BillingHistory to
                                stringResource(Res.string.secretary_tab_billing),
                            SecretaryTab.Archive to
                                stringResource(Res.string.secretary_tab_archive),
                            SecretaryTab.FloatingAccounts to
                                stringResource(Res.string.secretary_tab_floating),
                        ),
                        selected = uiState.selectedTab,
                        onSelect = callback.onTabSelect,
                        isCompact = isTabTrayCompact,
                    )

                    Spacer(Modifier.height(Dimensions.viewPadding24))

                    when (uiState.selectedTab) {
                        SecretaryTab.CompileTopSheet -> CompileTopSheetTab()
                        SecretaryTab.BranchLocations ->
                            BranchLocationsTab(uiState, callback, isCompact)
                        SecretaryTab.IspAccounts -> IspAccountsTab(uiState, callback, isCompact)
                        SecretaryTab.BillingHistory ->
                            BillingHistoryTab(uiState, callback, isCompact)
                        SecretaryTab.Archive -> ArchiveTab(uiState, isCompact)
                        SecretaryTab.FloatingAccounts -> FloatingAccountsTab(uiState)
                    }

                    Spacer(Modifier.height(Dimensions.viewPadding48))
                }
            }

            // Mutually exclusive by construction: the ViewModel clears one when
            // it opens the other, so only ever one form is on screen.
            uiState.addBranch?.let { form ->
                AddBranchDialog(
                    form = form,
                    canSubmit = uiState.canSubmitBranch,
                    providers = uiState.activeProviders,
                    callback = callback,
                )
            }
            uiState.addAccount?.let { form ->
                AddAccountDialog(
                    form = form,
                    canSubmit = uiState.canSubmitAccount,
                    branches = uiState.visibleBranches,
                    providers = uiState.activeProviders,
                    callback = callback,
                )
            }
        }
    }
}

/**
 * The dark bar across the top.
 *
 * Restated rather than shared with the sysadmin panel: the two consoles will
 * grow different actions up here — this one has none yet — and a component with
 * a slot for every screen's bar is harder to read than two short bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecretaryAppBar(
    userName: String,
    userRole: String,
    onLogoutClick: () -> Unit,
) = BoxWithConstraints {
    // The bar sits in the Scaffold's topBar, outside the body's own
    // measurement, so it has to ask about width itself.
    val isCompact = maxWidth <= Dimensions.viewWidth600

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
        actions = {
            val logoutDescription =
                stringResource(Res.string.console_logout_content_description)

            if (isCompact) {
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

                if (!isCompact) {
                    Text(
                        text = stringResource(Res.string.console_brand_suffix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                            .copy(alpha = ALPHA_MUTED),
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

/** Title and supporting line. No trailing action — this console has none. */
@Composable
private fun SecretaryHeader() {
    Column {
        Text(
            text = stringResource(Res.string.secretary_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(Dimensions.viewPadding4))
        Text(
            text = stringResource(Res.string.secretary_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Opacity for de-emphasised text and hairlines on the dark bar. */
private const val ALPHA_MUTED = 0.7f
private const val ALPHA_DIVIDER = 0.3f
