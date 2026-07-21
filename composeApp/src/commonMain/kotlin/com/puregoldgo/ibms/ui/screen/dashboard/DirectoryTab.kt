package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.component.roleLabel
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_active_directory
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_active_directory_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_add_user
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_directory_no_matches
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_directory_search
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_employee_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_isp_providers
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_payment_day
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_specify_provider
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_status_inactive
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_actions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_change_role
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_deactivate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_deactivate_last_sysadmin
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_deactivate_self
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_reactivate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_reset_password
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_role_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_user_temporary
import org.jetbrains.compose.resources.stringResource

/**
 * Who works here and which ISPs are configured.
 *
 * Two columns side by side when there is room — the directory carries more
 * content so it takes the wider share — and stacked otherwise.
 *
 * This tab used to lead with a *Pending Delegations* queue of accounts waiting
 * for a role. Nothing could ever land in it: accounts are provisioned by a
 * sysadmin, never self-registered, so an account and its role are decided in the
 * same act. The queue is gone and the act it stood for lives here instead — see
 * [AddUserDialog].
 */
@Composable
internal fun DirectoryTab(
    uiState: DashboardUIState,
    callback: DashboardCallback,
    isCompact: Boolean,
) {
    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            DirectoryCard(uiState, callback, isCompact)
            IspProvidersCard(uiState.activeProviders)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            Column(modifier = Modifier.weight(1.6f)) {
                DirectoryCard(uiState, callback, isCompact)
            }
            Column(modifier = Modifier.weight(1f)) {
                IspProvidersCard(uiState.activeProviders)
            }
        }
    }
}

/**
 * The staff directory, and the only place a user is created.
 *
 * *Add User* sits in this card's own header rather than beside the page-level
 * Bulk Upload: it acts on this list, and a global action bar that mixes "import
 * a spreadsheet of accounts" with "create a person" invites the wrong one to be
 * clicked.
 *
 * The search matches the employee number as well as the name and username. It
 * filters what is already loaded — the whole directory arrives in one walk — so
 * it costs nothing and stays correct.
 */
@Composable
private fun DirectoryCard(
    uiState: DashboardUIState,
    callback: DashboardCallback,
    isCompact: Boolean,
) {
    val users = uiState.directoryUsers

    SectionCard(
        title = stringResource(Res.string.dashboard_active_directory),
        icon = AppIcons.Group,
        trailing = {
            TextButton(
                onClick = callback.onAddUserClick,
                enabled = !uiState.isLoading && uiState.loadError == null,
            ) {
                Icon(
                    imageVector = AppIcons.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.viewSize18),
                )
                Spacer(Modifier.width(Dimensions.viewPadding8))
                Text(stringResource(Res.string.dashboard_add_user))
            }
        },
    ) {
        if (!uiState.isLoading && uiState.loadError == null) {
            SearchField(
                value = uiState.userQuery,
                onValueChange = callback.onUserQueryChange,
                placeholder = stringResource(Res.string.dashboard_directory_search),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimensions.viewPadding12))
        }

        when {
            // The panel loads its four lists together, so this is the shared
            // spinner — without it an empty directory and one still in flight
            // look the same, and "nobody works here" is alarming to read.
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.dashboard_retry),
            )

            // "Nobody works here" and "nobody matches what you typed" are
            // different facts, and only one of them is a reason to worry.
            users.isEmpty() -> SectionEmptyState(
                stringResource(
                    if (uiState.userQuery.isBlank()) {
                        Res.string.dashboard_active_directory_empty
                    } else {
                        Res.string.dashboard_directory_no_matches
                    },
                ),
            )

            else -> users.forEachIndexed { index, user ->
                if (index > 0) HorizontalDivider(thickness = Dimensions.viewHeight1)
                UserRow(
                    user = user,
                    callback = callback,
                    deactivateBlock = uiState.deactivateBlockedReason(user),
                    isCompact = isCompact,
                )
            }
        }
    }
}

/**
 * Avatar, identity, standing, role and the row's own menu.
 *
 * Stacks its controls under the identity when compact: the chips and a menu
 * button do not fit beside a name on a phone, and truncating the name to make
 * them fit would hide the one thing that says whose row this is.
 */
@Composable
private fun UserRow(
    user: DirectoryUser,
    callback: DashboardCallback,
    deactivateBlock: DeactivateBlock?,
    isCompact: Boolean,
) {
    // A deactivated account is still listed — it has to be findable to be turned
    // back on — but it is not a live account, and reading it at full strength
    // beside the others says otherwise.
    val nameColor = if (user.isActive) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    @Composable
    fun identity() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.viewSize36)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = user.initial,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = nameColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Dimensions.viewPadding2))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (user.employeeNumber != null) {
                        StatusChip(
                            label = stringResource(
                                Res.string.dashboard_employee_number,
                                user.employeeNumber,
                            ),
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun controls() {
        // Every row carries a role chip; only the name tells them apart, and a
        // screen reader has no column header to fall back on.
        val roleDescription = stringResource(Res.string.dashboard_user_role_label, user.name)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            if (!user.isActive) {
                StatusChip(
                    label = stringResource(Res.string.dashboard_status_inactive),
                    tone = ChipTone.Negative,
                )
            }

            // The visible half of "the temporary password is hidden once it
            // becomes permanent": the password itself is never readable again,
            // but this says the account is still holding one. The backend clears
            // `mustChangePassword` when the user sets their own, and the chip
            // goes with it.
            if (user.mustChangePassword) {
                StatusChip(
                    label = stringResource(Res.string.dashboard_user_temporary),
                    tone = ChipTone.Accent,
                )
            }

            // Read-only. This was a live dropdown, which put "grant this person
            // sysadmin" one stray click away on every row of a scrolling list;
            // the change now happens in a dialog that names who it is for. An
            // account with no role is toned differently — it is the one state
            // here that means "cannot use the app".
            StatusChip(
                label = roleLabel(user.role),
                tone = if (user.role == Role.PENDING) ChipTone.Accent else ChipTone.Neutral,
                modifier = Modifier.semantics {
                    contentDescription = roleDescription
                },
            )

            UserRowMenu(
                user = user,
                callback = callback,
                deactivateBlock = deactivateBlock,
            )
        }
    }

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding12),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            identity()
            controls()
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Box(modifier = Modifier.weight(1f)) { identity() }
            controls()
        }
    }
}

/**
 * Change role, reset password and deactivate, behind an overflow.
 *
 * All three are low-frequency and consequential — one rewrites what the user may
 * do, one revokes every session they hold, the last locks them out entirely — so
 * none earns a permanent control on every row where it could be hit by accident.
 * Each opens a dialog; nothing here sends a request on its own.
 *
 * [deactivateBlock] disables that one item rather than hiding it. A missing
 * option reads as a bug; a disabled one that says "last sysadmin" explains a
 * rule the admin is better off knowing before they go looking for a way around
 * it.
 */
@Composable
private fun UserRowMenu(
    user: DirectoryUser,
    callback: DashboardCallback,
    deactivateBlock: DeactivateBlock?,
) {
    var expanded by remember { mutableStateOf(false) }

    // Only deactivation can lock the company out of its own administration —
    // turning an account back on never does.
    val block = deactivateBlock.takeIf { user.isActive }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = AppIcons.MoreVert,
                contentDescription = stringResource(
                    Res.string.dashboard_user_actions,
                    user.name,
                ),
                modifier = Modifier.size(Dimensions.viewSize20),
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.dashboard_user_change_role)) },
                onClick = {
                    expanded = false
                    callback.onChangeRoleClick(user)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.dashboard_user_reset_password)) },
                onClick = {
                    expanded = false
                    callback.onResetPasswordClick(user)
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(
                            when {
                                block == DeactivateBlock.Self ->
                                    Res.string.dashboard_user_deactivate_self

                                block == DeactivateBlock.LastSysadmin ->
                                    Res.string.dashboard_user_deactivate_last_sysadmin

                                user.isActive -> Res.string.dashboard_user_deactivate

                                else -> Res.string.dashboard_user_reactivate
                            },
                        ),
                    )
                },
                enabled = block == null,
                onClick = {
                    expanded = false
                    callback.onUserStatusToggleClick(user)
                },
            )
        }
    }
}

@Composable
private fun IspProvidersCard(providers: List<IspProviderRow>) {
    SectionCard(
        title = stringResource(Res.string.dashboard_isp_providers),
        icon = AppIcons.Tune,
    ) {
        providers.forEach { provider ->
            ProviderRow(provider)
            Spacer(Modifier.height(Dimensions.viewPadding8))
        }

        // TODO: POST /providers (name + paymentScheduleDay). Writes are out of
        //  scope on this UI-only pass.
        DashedAddButton(
            label = stringResource(Res.string.dashboard_specify_provider),
            onClick = {},
            enabled = false,
        )
    }
}

@Composable
private fun ProviderRow(provider: IspProviderRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius8))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(
                horizontal = Dimensions.viewPadding16,
                vertical = Dimensions.viewPadding12,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = provider.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        StatusChip(
            label = stringResource(Res.string.dashboard_payment_day, provider.paymentScheduleDay),
            tone = ChipTone.Neutral,
        )
    }
}

/**
 * The dashed "add one" affordance. Drawn rather than composed from a component
 * because Material has no dashed outline, and a dotted border is what signals
 * "this slot is empty and can be filled" in the rest of the mockup.
 */
@Composable
private fun DashedAddButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val outline = MaterialTheme.colorScheme.outline
    val shape = RoundedCornerShape(Dimensions.viewRadius8)

    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimensions.viewHeight56)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    style = Stroke(
                        width = Dimensions.viewStroke1.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(
                                Dimensions.viewDashOn.toPx(),
                                Dimensions.viewDashOff.toPx(),
                            ),
                        ),
                    ),
                    cornerRadius = CornerRadius(Dimensions.viewRadius8.toPx()),
                )
            },
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}
