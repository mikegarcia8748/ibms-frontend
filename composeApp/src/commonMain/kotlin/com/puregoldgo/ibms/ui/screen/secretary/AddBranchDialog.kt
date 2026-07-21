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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogBanner
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_city
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_code
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_name
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_provider
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_branch_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_offline_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_offline_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_select_placeholder
import org.jetbrains.compose.resources.stringResource

/**
 * Creates a branch.
 *
 * The layout is finished; the submit is not wired. The banner says so rather
 * than leaving the operator to discover it — a form that silently discards what
 * was typed is worse than one that admits it cannot save yet.
 *
 * TODO: `POST /stores` (secretary), then refresh the branch list.
 */
@Composable
internal fun AddBranchDialog(
    form: NewBranchForm,
    canSubmit: Boolean,
    providers: List<SecretaryProviderRow>,
    callback: SecretaryCallback,
) {
    AppDialog(onDismissRequest = callback.onAddBranchDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_add_branch_title),
            icon = AppIcons.Domain,
            onClose = callback.onAddBranchDismiss,
            closeDescription = stringResource(Res.string.secretary_add_branch_close),
        )

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            OutlinedTextField(
                value = form.branchCode,
                onValueChange = callback.onNewBranchCodeChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.secretary_add_branch_code)) },
                singleLine = true,
                // Branch codes are numeric in every record the import has seen,
                // but they are identifiers, not quantities — so the keyboard is
                // a hint, not a restriction.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            OutlinedTextField(
                value = form.name,
                onValueChange = callback.onNewBranchNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.secretary_add_branch_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            OutlinedTextField(
                value = form.city,
                onValueChange = callback.onNewBranchCityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.secretary_add_branch_city)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            LabeledDropdown(
                options = providers.providerFilterOptions(),
                selectedId = form.providerId,
                onSelect = callback.onNewBranchProviderChange,
                placeholder = stringResource(Res.string.secretary_select_placeholder),
                label = stringResource(Res.string.secretary_add_branch_provider),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Dimensions.viewPadding16))

            NotConnectedBanner()
        }

        AppDialogFooter {
            TextButton(onClick = callback.onAddBranchDismiss) {
                Text(stringResource(Res.string.secretary_dialog_cancel))
            }
            Button(
                onClick = callback.onAddBranchSubmit,
                enabled = canSubmit,
            ) {
                Text(stringResource(Res.string.secretary_add_branch_submit))
            }
        }
    }
}

/** Says out loud that the submit goes nowhere yet. Shared by both add dialogs. */
@Composable
internal fun NotConnectedBanner() {
    AppDialogBanner(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = stringResource(Res.string.secretary_dialog_offline_title),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = stringResource(Res.string.secretary_dialog_offline_body),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
