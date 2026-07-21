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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.shared.model.wireValue
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_active_directory
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_active_directory_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_assign_role
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_employee_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_inactive_providers
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_inactive_providers_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_isp_providers
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_payment_day
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_pending_delegations
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_pending_delegations_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_specify_provider
import org.jetbrains.compose.resources.stringResource

/**
 * Who works here and which ISPs are configured.
 *
 * Two columns side by side when there is room — delegations carry more content
 * so they take the wider share — and stacked otherwise.
 */
@Composable
internal fun DelegationsTab(
    uiState: DashboardUIState,
    isCompact: Boolean,
) {
    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            PendingDelegationsCard(uiState.pendingUsers)
            ActiveDirectoryCard(uiState.directoryUsers)
            IspProvidersCard(uiState.activeProviders)
            InactiveProvidersCard(uiState.inactiveProviders)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            Column(
                modifier = Modifier.weight(1.6f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
            ) {
                PendingDelegationsCard(uiState.pendingUsers)
                ActiveDirectoryCard(uiState.directoryUsers)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
            ) {
                IspProvidersCard(uiState.activeProviders)
                InactiveProvidersCard(uiState.inactiveProviders)
            }
        }
    }
}

@Composable
private fun PendingDelegationsCard(pending: List<DirectoryUser>) {
    SectionCard(
        title = stringResource(Res.string.dashboard_pending_delegations),
        icon = AppIcons.Key,
        trailing = { StatusChip(label = pending.size.toString(), tone = ChipTone.Neutral) },
    ) {
        if (pending.isEmpty()) {
            SectionEmptyState(stringResource(Res.string.dashboard_pending_delegations_empty))
        } else {
            pending.forEachIndexed { index, user ->
                if (index > 0) HorizontalDivider(thickness = Dimensions.viewHeight1)
                UserRow(
                    user = user,
                    trailing = {
                        // TODO: PATCH /users/{id}/role. Writes are out of scope
                        //  on this UI-only pass.
                        TextButton(onClick = {}, enabled = false) {
                            Text(stringResource(Res.string.dashboard_assign_role))
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ActiveDirectoryCard(users: List<DirectoryUser>) {
    SectionCard(
        title = stringResource(Res.string.dashboard_active_directory),
        icon = AppIcons.Group,
    ) {
        if (users.isEmpty()) {
            SectionEmptyState(stringResource(Res.string.dashboard_active_directory_empty))
        } else {
            users.forEachIndexed { index, user ->
                if (index > 0) HorizontalDivider(thickness = Dimensions.viewHeight1)
                UserRow(
                    user = user,
                    trailing = {
                        StatusChip(
                            label = user.role.wireValue.uppercase(),
                            tone = if (user.role == Role.SYSADMIN) {
                                ChipTone.Accent
                            } else {
                                ChipTone.Positive
                            },
                        )
                    },
                )
            }
        }
    }
}

/** Avatar, identity, and whatever the caller wants on the right. */
@Composable
private fun UserRow(
    user: DirectoryUser,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.viewPadding12),
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
                color = MaterialTheme.colorScheme.onSurface,
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

        trailing()
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
private fun InactiveProvidersCard(providers: List<IspProviderRow>) {
    SectionCard(
        title = stringResource(Res.string.dashboard_inactive_providers),
        icon = AppIcons.Power,
    ) {
        if (providers.isEmpty()) {
            SectionEmptyState(stringResource(Res.string.dashboard_inactive_providers_empty))
        } else {
            providers.forEach { provider ->
                ProviderRow(provider)
                Spacer(Modifier.height(Dimensions.viewPadding8))
            }
        }
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
