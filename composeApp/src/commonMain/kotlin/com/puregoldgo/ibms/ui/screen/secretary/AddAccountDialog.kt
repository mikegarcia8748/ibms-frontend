package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_branch
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_provider
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_rate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_select_placeholder
import org.jetbrains.compose.resources.stringResource

/**
 * Creates an ISP account against a branch.
 *
 * The branch and the ISP are dropdowns, not free text: an account typed against
 * a store that does not exist is exactly how an account ends up floating, and
 * this dialog is upstream of the panel that has to find them again.
 *
 * TODO: `POST /accounts` (secretary, payables), then refresh the account list.
 */
@Composable
internal fun AddAccountDialog(
    form: NewAccountForm,
    canSubmit: Boolean,
    branches: List<SecretaryBranchRow>,
    providers: List<SecretaryProviderRow>,
    callback: SecretaryCallback,
) {
    AppDialog(onDismissRequest = callback.onAddAccountDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_add_account_title),
            icon = AppIcons.Wifi,
            onClose = callback.onAddAccountDismiss,
            closeDescription = stringResource(Res.string.secretary_add_account_close),
        )

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            OutlinedTextField(
                value = form.accountNumber,
                onValueChange = callback.onNewAccountNumberChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.secretary_add_account_number)) },
                singleLine = true,
                // Monospaced here for the same reason the list shows it that
                // way: it is transcribed character by character off an invoice.
                textStyle = MaterialTheme.typography.bodyLarge
                    .copy(fontFamily = FontFamily.Monospace),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            LabeledDropdown(
                options = branches.branchFilterOptions(),
                selectedId = form.storeId,
                onSelect = callback.onNewAccountStoreChange,
                placeholder = stringResource(Res.string.secretary_select_placeholder),
                label = stringResource(Res.string.secretary_add_account_branch),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            LabeledDropdown(
                options = providers.providerFilterOptions(),
                selectedId = form.providerId,
                onSelect = callback.onNewAccountProviderChange,
                placeholder = stringResource(Res.string.secretary_select_placeholder),
                label = stringResource(Res.string.secretary_add_account_provider),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            OutlinedTextField(
                value = form.monthlyRate,
                onValueChange = callback.onNewAccountRateChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.secretary_add_account_rate)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            NotConnectedBanner()
        }

        AppDialogFooter {
            TextButton(onClick = callback.onAddAccountDismiss) {
                Text(stringResource(Res.string.secretary_dialog_cancel))
            }
            Button(
                onClick = callback.onAddAccountSubmit,
                enabled = canSubmit,
            ) {
                Text(stringResource(Res.string.secretary_add_account_submit))
            }
        }
    }
}
