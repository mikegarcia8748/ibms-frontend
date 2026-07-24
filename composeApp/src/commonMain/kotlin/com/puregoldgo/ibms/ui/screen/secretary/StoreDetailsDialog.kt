package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.format.formatDate
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_barangay
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_circuit_id
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_key_dates
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_last_updated
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_linked_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_location_data
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_not_available
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_postal
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_region_province
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_city_municipality
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_registered_on
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_store_detail_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_store_detail_title
import org.jetbrains.compose.resources.stringResource

/**
 * A read-only detail dialog showing a store's full record.
 *
 * Reachable from any branch card in the locations tab. The linked-account rows
 * are themselves tappable, which opens [AccountDetailsDialog] — the same data,
 * seen from the other side of the relationship.
 */
@Composable
internal fun StoreDetailsDialog(
    detail: StoreDetail,
    callback: SecretaryCallback,
) {
    val na = stringResource(Res.string.secretary_detail_not_available)

    AppDialog(onDismissRequest = callback.onStoreDetailDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_store_detail_title),
            icon = AppIcons.Domain,
            onClose = callback.onStoreDetailDismiss,
            closeDescription = stringResource(Res.string.secretary_detail_close),
            subtitle = stringResource(Res.string.secretary_store_detail_subtitle, detail.branchCode),
        )

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            // Header: big branch code + status chip on same row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = detail.branchCode,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                BranchStatusChip(detail.status)
            }

            // Display name below the code.
            Text(
                text = detail.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Dimensions.viewPadding24))

            // Two cards side by side: Location Data + Key Dates.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
            ) {
                // Left card: Location Data.
                DetailCard(modifier = Modifier.weight(1f)) {
                    DetailSectionHeader(
                        icon = AppIcons.LocationOn,
                        title = stringResource(Res.string.secretary_detail_location_data),
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_region_province),
                        value = buildString {
                            append(detail.region ?: na)
                            append(" \u2013 ")
                            append(detail.province ?: na)
                        },
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_city_municipality),
                        value = detail.city ?: na,
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_barangay),
                        value = detail.barangay ?: na,
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_postal),
                        value = detail.postal ?: na,
                    )
                }

                // Right card: Key Dates & Status.
                DetailCard(modifier = Modifier.weight(1f)) {
                    DetailSectionHeader(
                        icon = AppIcons.CalendarToday,
                        title = stringResource(Res.string.secretary_detail_key_dates),
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_registered_on),
                        value = detail.registeredOn?.let(::formatDate) ?: na,
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_last_updated),
                        value = detail.lastUpdated?.let(::formatDate) ?: na,
                    )
                }
            }

            if (detail.linkedAccounts.isNotEmpty()) {
                Spacer(Modifier.height(Dimensions.viewPadding16))

                // Linked Accounts — full-width card.
                DetailCard(modifier = Modifier.fillMaxWidth()) {
                    DetailSectionHeader(
                        icon = AppIcons.Wifi,
                        title = stringResource(Res.string.secretary_detail_linked_accounts),
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding12))

                    detail.linkedAccounts.forEach { link ->
                        LinkedAccountRow(link, callback, na)
                        Spacer(Modifier.height(Dimensions.viewPadding8))
                    }
                }
            }
        }
    }
}

/**
 * A single linked-account row inside the store detail dialog. Tapping it opens
 * the account detail dialog — the store and the account are two views of the
 * same circuit, and the operator moves between them freely.
 */
@Composable
private fun LinkedAccountRow(
    link: AccountLink,
    callback: SecretaryCallback,
    na: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { callback.onAccountClick(link.accountId) },
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.viewPadding12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = link.accountNumber,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.secretary_detail_circuit_id) +
                        ": " + (link.circuitId ?: na),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AccountStatusChip(link.status)
        }
    }
}

// ─── Shared detail card composables ──────────────────────────────────────────

/** A cream/tinted surface used as a section card in detail dialogs. */
@Composable
internal fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = Dimensions.viewElevation0,
    ) {
        Column(modifier = Modifier.padding(Dimensions.viewPadding16)) {
            content()
        }
    }
}

/** Icon + ALL CAPS section label for detail cards. */
@Composable
internal fun DetailSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding6),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize16),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing * 1.2f,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A stacked label (small, muted) above a value (bold, dark). */
@Composable
internal fun LabelAboveValue(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A horizontal key-value row: label left, value right. */
@Composable
internal fun KeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.viewPadding2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
