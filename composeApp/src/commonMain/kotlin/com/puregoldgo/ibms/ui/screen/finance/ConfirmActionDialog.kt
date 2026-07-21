package com.puregoldgo.ibms.ui.screen.finance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogBanner
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_approve_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_approve_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_approve_submitting
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_approve_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_pay_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_pay_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_pay_submitting
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_confirm_pay_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_dialog_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_dialog_offline_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.finance_dialog_offline_title
import org.jetbrains.compose.resources.stringResource

/**
 * Confirms an approval or a payment.
 *
 * One dialog for both because the decision is the same shape — a named sheet, an
 * irreversible transition, one way forward and one way out — and two dialogs
 * would drift into wording each other's warnings differently.
 *
 * Neither dismisses on an outside click: this is the last gate in front of a
 * release of money, and a stray click beside it must not read as consent.
 *
 * The layout is finished; the confirm is not wired. The banner says so rather
 * than leaving the operator to discover that nothing happened.
 *
 * TODO: `POST /topsheets/{id}/approve` and `POST /topsheets/{id}/pay` (finance),
 *  then refresh both queues.
 */
@Composable
internal fun ConfirmActionDialog(
    action: TopSheetAction,
    canConfirm: Boolean,
    isSubmitting: Boolean,
    error: String?,
    callback: FinanceCallback,
) {
    val isApproval = action.kind == TopSheetAction.Kind.Approve

    AppDialog(
        onDismissRequest = callback.onActionDismiss,
        dismissOnClickOutside = false,
    ) {
        AppDialogHeader(
            title = stringResource(
                if (isApproval) {
                    Res.string.finance_confirm_approve_title
                } else {
                    Res.string.finance_confirm_pay_title
                },
            ),
            icon = if (isApproval) AppIcons.CheckCircle else AppIcons.Description,
            onClose = callback.onActionDismiss,
            closeDescription = stringResource(Res.string.finance_confirm_close),
            closeEnabled = !isSubmitting,
        )

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            Text(
                text = stringResource(
                    if (isApproval) {
                        Res.string.finance_confirm_approve_body
                    } else {
                        Res.string.finance_confirm_pay_body
                    },
                    action.sheet.invoiceNumber,
                    action.sheet.providerName,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            AppDialogBanner(
                container = MaterialTheme.colorScheme.surfaceContainerHighest,
                content = MaterialTheme.colorScheme.onSurface,
            ) {
                Text(
                    text = stringResource(Res.string.finance_dialog_offline_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(Res.string.finance_dialog_offline_body),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // The server's own wording, shown verbatim — it names what to fix.
            if (error != null) {
                Spacer(Modifier.height(Dimensions.viewPadding12))
                AppDialogBanner(
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer,
                ) {
                    Text(text = error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        AppDialogFooter {
            TextButton(onClick = callback.onActionDismiss, enabled = !isSubmitting) {
                Text(stringResource(Res.string.finance_dialog_cancel))
            }
            Button(onClick = callback.onActionConfirm, enabled = canConfirm) {
                Text(
                    stringResource(
                        when {
                            isSubmitting && isApproval ->
                                Res.string.finance_confirm_approve_submitting

                            isSubmitting -> Res.string.finance_confirm_pay_submitting
                            isApproval -> Res.string.finance_confirm_approve_submit
                            else -> Res.string.finance_confirm_pay_submit
                        },
                    ),
                )
            }
        }
    }
}
