package com.puregoldgo.ibms.ui.screen.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_barangay_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_branch_code_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_branch_name_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_city_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_location_section
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_postal_code_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_province_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_region_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_store_type_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_store_type_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.register_branch_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.store_type_puregold
import ibmsispbillingmanagementsystem.composeapp.generated.resources.store_type_puremart
import org.jetbrains.compose.resources.stringResource

/**
 * Registers a new branch / store.
 *
 * The dialog is role-agnostic: callers decide who sees it, but the form itself
 * only collects the data `POST /stores` accepts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterBranchDialog(
    form: RegisterBranchForm,
    callback: RegisterBranchCallback,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    AppDialog(
        onDismissRequest = callback.onDismiss,
        modifier = modifier,
    ) {
        AppDialogHeader(
            title = stringResource(Res.string.register_branch_title),
            subtitle = stringResource(Res.string.register_branch_subtitle),
            icon = AppIcons.Domain,
            onClose = callback.onDismiss,
            closeDescription = stringResource(Res.string.register_branch_close),
        )

        Column(
            modifier = Modifier
                .padding(Dimensions.viewPadding24)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
        ) {
            StoreTypeField(
                storeType = form.storeType,
                onStoreTypeChange = callback.onStoreTypeChange,
            )

            FormRow(isCompact = isCompact) {
                OutlinedTextField(
                    value = form.branchCode,
                    onValueChange = callback.onBranchCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.register_branch_branch_code_label)) },
                    isError = form.branchCodeError != null,
                    supportingText = form.branchCodeError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next,
                    ),
                )

                OutlinedTextField(
                    value = form.branchName,
                    onValueChange = callback.onBranchNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.register_branch_branch_name_label)) },
                    isError = form.branchNameError != null,
                    supportingText = form.branchNameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            LocationSection()

            FormRow(isCompact = isCompact) {
                OutlinedTextField(
                    value = form.region,
                    onValueChange = callback.onRegionChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.register_branch_region_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )

                OutlinedTextField(
                    value = form.province,
                    onValueChange = callback.onProvinceChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.register_branch_province_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            FormRow(isCompact = isCompact) {
                OutlinedTextField(
                    value = form.city,
                    onValueChange = callback.onCityChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.register_branch_city_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )

                OutlinedTextField(
                    value = form.postalCode,
                    onValueChange = callback.onPostalCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.register_branch_postal_code_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                )
            }

            OutlinedTextField(
                value = form.barangay,
                onValueChange = callback.onBarangayChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.register_branch_barangay_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            )
        }

        AppDialogFooter {
            TextButton(
                onClick = callback.onDismiss,
                enabled = !form.isSaving,
            ) {
                Text(stringResource(Res.string.register_branch_cancel))
            }
            Button(
                onClick = callback.onSubmit,
                enabled = form.canSubmit,
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(Res.string.register_branch_submit))
                }
            }
        }
    }
}

@Composable
private fun StoreTypeField(
    storeType: com.puregoldgo.ibms.shared.model.StoreType,
    onStoreTypeChange: (com.puregoldgo.ibms.shared.model.StoreType) -> Unit,
) {
    val options = listOf(
        FilterOption(
            id = com.puregoldgo.ibms.shared.model.StoreType.PUREGOLD.name.lowercase(),
            label = stringResource(Res.string.store_type_puregold),
        ),
        FilterOption(
            id = com.puregoldgo.ibms.shared.model.StoreType.PUREMART.name.lowercase(),
            label = stringResource(Res.string.store_type_puremart),
        ),
    )

    LabeledDropdown(
        options = options,
        selectedId = storeType.name.lowercase(),
        onSelect = { id ->
            val selected = com.puregoldgo.ibms.shared.model.StoreType.entries
                .firstOrNull { it.name.lowercase() == id }
            selected?.let(onStoreTypeChange)
        },
        placeholder = stringResource(Res.string.register_branch_store_type_placeholder),
        label = stringResource(Res.string.register_branch_store_type_label),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LocationSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
    ) {
        Icon(
            imageVector = AppIcons.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize20),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.register_branch_location_section),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FormRow(
    isCompact: Boolean,
    content: @Composable () -> Unit,
) {
    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            content()
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}
