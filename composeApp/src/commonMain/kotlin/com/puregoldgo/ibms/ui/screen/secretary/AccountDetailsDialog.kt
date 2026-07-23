package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.format.formatDate
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_account_detail_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_account_detail_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_name
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_account_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_bandwidth
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_billing_details
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_circuit_id
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_end_date
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_created_at
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_inst_date
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_linked_account_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_linked_store
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_monthly_rate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_not_available
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_start_date
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_technical_info
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_terms
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_detail_timeline
import org.jetbrains.compose.resources.stringResource

/**
 * A read-only detail dialog showing an account's full record.
 *
 * Reachable from any account card in the ISP accounts tab, and from the linked
 * rows inside [StoreDetailsDialog]. Billing history and documents are out of
 * scope here — they live on their own tabs.
 */
@Composable
internal fun AccountDetailsDialog(
    detail: AccountDetail,
    callback: SecretaryCallback,
) {
    val na = stringResource(Res.string.secretary_detail_not_available)

    // The subtitle line: provider · plan · CID.
    val providerLine = listOfNotNull(
        detail.providerName,
        detail.planName ?: "Unknown Plan",
        detail.circuitId?.let { "CID: $it" },
    ).joinToString(" \u2022 ")

    AppDialog(onDismissRequest = callback.onAccountDetailDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_account_detail_title),
            icon = AppIcons.Wifi,
            onClose = callback.onAccountDetailDismiss,
            closeDescription = stringResource(Res.string.secretary_detail_close),
            subtitle = stringResource(
                Res.string.secretary_account_detail_subtitle,
                detail.branchCode,
                detail.accountNumber,
            ),
        )

        Column(
            modifier = Modifier
                .padding(Dimensions.viewPadding24)
                .verticalScroll(rememberScrollState()),
        ) {
            // "LINKED ACCOUNT NUMBER" small colored label.
            Text(
                text = stringResource(Res.string.secretary_detail_linked_account_number),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(Modifier.height(Dimensions.viewPadding4))

            // Big account number + status chip.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = detail.accountNumber,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                AccountStatusChip(detail.status)
            }

            // Provider subtitle line.
            Text(
                text = providerLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimensions.viewPadding24))

            // Two cards side by side: Linked Store + Technical Info.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
            ) {
                // Left card: Linked Store.
                DetailCard(modifier = Modifier.weight(1f)) {
                    DetailSectionHeader(
                        icon = AppIcons.Domain,
                        title = stringResource(Res.string.secretary_detail_linked_store),
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                    ) {
                        // Branch code badge.
                        Surface(
                            shape = RoundedCornerShape(Dimensions.viewRadius6),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = detail.branchCode,
                                modifier = Modifier.padding(
                                    horizontal = Dimensions.viewPadding8,
                                    vertical = Dimensions.viewPadding4,
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                    Spacer(Modifier.height(Dimensions.viewPadding4))
                    Text(
                        text = detail.storeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Right card: Technical Info.
                DetailCard(modifier = Modifier.weight(1f)) {
                    DetailSectionHeader(
                        icon = AppIcons.Tag,
                        title = stringResource(Res.string.secretary_detail_technical_info),
                    )
                    Spacer(Modifier.height(Dimensions.viewPadding12))
                    KeyValueRow(
                        label = stringResource(Res.string.secretary_detail_account_number),
                        value = detail.accountNumber,
                    )
                    KeyValueRow(
                        label = stringResource(Res.string.secretary_detail_circuit_id),
                        value = detail.circuitId ?: na,
                    )
                    KeyValueRow(
                        label = stringResource(Res.string.secretary_detail_bandwidth),
                        value = detail.speed ?: na,
                    )
                    KeyValueRow(
                        label = stringResource(Res.string.secretary_detail_terms),
                        value = "${detail.contractDurationMonths ?: na} months",
                    )
                }
            }

            Spacer(Modifier.height(Dimensions.viewPadding16))

            // Billing Details card.
            DetailCard(modifier = Modifier.fillMaxWidth()) {
                DetailSectionHeader(
                    icon = AppIcons.Payments,
                    title = stringResource(Res.string.secretary_detail_billing_details),
                )
                Spacer(Modifier.height(Dimensions.viewPadding12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.secretary_detail_monthly_rate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "\u20B1${detail.monthlyRate}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(Dimensions.viewPadding16))

            // Timeline card — horizontal 4 columns.
            DetailCard(modifier = Modifier.fillMaxWidth()) {
                DetailSectionHeader(
                    icon = AppIcons.CalendarToday,
                    title = stringResource(Res.string.secretary_detail_timeline),
                )
                Spacer(Modifier.height(Dimensions.viewPadding12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_inst_date),
                        value = detail.installationDate?.let(::formatDate) ?: na,
                    )
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_created_at),
                        value = detail.createdAt?.let(::formatDate) ?: na,
                    )
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_start_date),
                        value = detail.contractStartDate?.let(::formatDate) ?: na,
                    )
                    LabelAboveValue(
                        label = stringResource(Res.string.secretary_detail_end_date),
                        value = detail.contractEndDate?.let(::formatDate) ?: na,
                    )
                }
            }
        }
    }
}
