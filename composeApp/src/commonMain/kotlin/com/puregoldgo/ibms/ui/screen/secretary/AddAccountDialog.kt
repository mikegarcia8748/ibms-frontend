package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.platform.file.fileDropTarget
import com.puregoldgo.ibms.platform.file.rememberFilePicker
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_billing_period
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_billing_period_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_branch
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_branch_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_circuit_id
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_circuit_id_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_installation_date
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_installation_date_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_number
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_number_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_plan
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_plan_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_proof_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_proof_remove
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_proof_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_proof_upload
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_provider
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_provider_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_rate
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_rate_placeholder
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_section_billing
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_section_core
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_submit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add_account_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_dialog_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_select_placeholder
import org.jetbrains.compose.resources.stringResource

private const val MAX_PROOF_ATTACHMENTS = 5

/**
 * Creates an ISP account against a branch.
 *
 * The branch and the ISP are dropdowns, not free text: an account typed against
 * a store that does not exist is exactly how an account ends up floating, and
 * this dialog is upstream of the panel that has to find them again. Proofs are
 * uploaded as soon as they are picked; only the attachment ids travel with the
 * final `POST /accounts` request.
 */
@Composable
internal fun AddAccountDialog(
    form: NewAccountForm,
    canSubmit: Boolean,
    branches: List<SecretaryBranchRow>,
    providers: List<SecretaryProviderRow>,
    isCompact: Boolean,
    callback: SecretaryCallback,
) {
    AppDialog(
        onDismissRequest = callback.onAddAccountDismiss,
        dismissOnClickOutside = !form.isSubmitting,
        dismissOnBackPress = !form.isSubmitting,
    ) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_add_account_title),
            subtitle = stringResource(Res.string.secretary_add_account_subtitle),
            icon = AppIcons.Wifi,
            onClose = callback.onAddAccountDismiss,
            closeDescription = stringResource(Res.string.secretary_add_account_close),
            closeEnabled = !form.isSubmitting,
        )

        val fieldSpacing = Dimensions.viewPadding16

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            if (isCompact) {
                CoreFields(
                    form = form,
                    branches = branches,
                    providers = providers,
                    callback = callback,
                    verticalSpacing = fieldSpacing,
                )
                Spacer(Modifier.height(fieldSpacing))
                BillingFields(
                    form = form,
                    callback = callback,
                    verticalSpacing = fieldSpacing,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding24),
                ) {
                    CoreFields(
                        form = form,
                        branches = branches,
                        providers = providers,
                        callback = callback,
                        verticalSpacing = fieldSpacing,
                        modifier = Modifier.weight(1f),
                    )
                    BillingFields(
                        form = form,
                        callback = callback,
                        verticalSpacing = fieldSpacing,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        AppDialogFooter {
            TextButton(
                onClick = callback.onAddAccountDismiss,
                enabled = !form.isSubmitting,
            ) {
                Text(stringResource(Res.string.secretary_dialog_cancel))
            }
            Button(
                onClick = callback.onAddAccountSubmit,
                enabled = canSubmit,
            ) {
                if (form.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimensions.viewStroke2,
                    )
                } else {
                    Text(stringResource(Res.string.secretary_add_account_submit))
                }
            }
        }
    }
}

@Composable
private fun CoreFields(
    form: NewAccountForm,
    branches: List<SecretaryBranchRow>,
    providers: List<SecretaryProviderRow>,
    callback: SecretaryCallback,
    verticalSpacing: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.secretary_add_account_section_core))
        Spacer(Modifier.height(verticalSpacing))

        var branchQuery by remember { mutableStateOf("") }
        val branchOptions = remember(branchQuery, branches) {
            branches
                .filter {
                    branchQuery.isBlank() ||
                        it.displayName.contains(branchQuery, ignoreCase = true) ||
                        it.branchCode.contains(branchQuery, ignoreCase = true)
                }
                .sortedBy { it.displayName }
                .map { FilterOption(id = it.id, label = it.displayName) }
        }

        SearchField(
            value = branchQuery,
            onValueChange = { branchQuery = it },
            placeholder = stringResource(Res.string.secretary_add_account_branch_placeholder),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(Dimensions.viewPadding8))
        LabeledDropdown(
            options = branchOptions,
            selectedId = form.storeId,
            onSelect = callback.onNewAccountStoreChange,
            placeholder = stringResource(Res.string.secretary_select_placeholder),
            label = stringResource(Res.string.secretary_add_account_branch),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(verticalSpacing))

        LabeledDropdown(
            options = providers.providerFilterOptions(),
            selectedId = form.providerId,
            onSelect = callback.onNewAccountProviderChange,
            placeholder = stringResource(Res.string.secretary_select_placeholder),
            label = stringResource(Res.string.secretary_add_account_provider),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(verticalSpacing))

        OutlinedTextField(
            value = form.accountNumber,
            onValueChange = callback.onNewAccountNumberChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.secretary_add_account_number)) },
            placeholder = { Text(stringResource(Res.string.secretary_add_account_number_placeholder)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge
                .copy(fontFamily = FontFamily.Monospace),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(verticalSpacing))

        OutlinedTextField(
            value = form.circuitId,
            onValueChange = callback.onNewAccountCircuitIdChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.secretary_add_account_circuit_id)) },
            placeholder = { Text(stringResource(Res.string.secretary_add_account_circuit_id_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(verticalSpacing))

        OutlinedTextField(
            value = form.planName,
            onValueChange = callback.onNewAccountPlanChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.secretary_add_account_plan)) },
            placeholder = { Text(stringResource(Res.string.secretary_add_account_plan_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
    }
}

@Composable
private fun BillingFields(
    form: NewAccountForm,
    callback: SecretaryCallback,
    verticalSpacing: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(stringResource(Res.string.secretary_add_account_section_billing))
        Spacer(Modifier.height(verticalSpacing))

        OutlinedTextField(
            value = form.installationDate,
            onValueChange = callback.onNewAccountInstallationDateChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.secretary_add_account_installation_date)) },
            placeholder = { Text(stringResource(Res.string.secretary_add_account_installation_date_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(verticalSpacing))

        OutlinedTextField(
            value = form.billingPeriodLabel,
            onValueChange = callback.onNewAccountBillingPeriodChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.secretary_add_account_billing_period)) },
            placeholder = { Text(stringResource(Res.string.secretary_add_account_billing_period_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(verticalSpacing))

        OutlinedTextField(
            value = form.monthlyRate,
            onValueChange = callback.onNewAccountRateChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.secretary_add_account_rate)) },
            placeholder = { Text(stringResource(Res.string.secretary_add_account_rate_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
        )

        Spacer(Modifier.height(verticalSpacing))

        ProofUploadSection(
            form = form,
            onProofPicked = callback.onNewAccountProofPicked,
            onProofRemove = callback.onNewAccountProofRemove,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ProofUploadSection(
    form: NewAccountForm,
    onProofPicked: (PickedFile) -> Unit,
    onProofRemove: (String) -> Unit,
) {
    val picker = rememberFilePicker(accept = listOf("application/pdf")) { file ->
        onProofPicked(file)
    }
    var isDragging by remember { mutableStateOf(false) }
    val canAddMore = form.proofAttachmentIds.size < MAX_PROOF_ATTACHMENTS

    Text(
        text = stringResource(Res.string.secretary_add_account_proof_title),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(Modifier.height(Dimensions.viewPadding8))

    val dropModifier = Modifier.fileDropTarget(
        enabled = canAddMore && !form.isSubmitting,
        onDragOver = { isDragging = it },
        onDropped = { file -> onProofPicked(file) },
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .then(dropModifier)
            .clickable(
                enabled = canAddMore && !form.isSubmitting,
                onClick = { picker.launch() },
            ),
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = when {
            isDragging -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = BorderStroke(
            width = Dimensions.viewStroke1,
            color = when {
                isDragging -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Icon(
                imageVector = AppIcons.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.viewSize32),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.secretary_add_account_proof_upload),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.secretary_add_account_proof_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    form.proofUploadError?.let { error ->
        Spacer(Modifier.height(Dimensions.viewPadding8))
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    if (form.proofAttachmentIds.isNotEmpty()) {
        Spacer(Modifier.height(Dimensions.viewPadding12))
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8)) {
            form.proofAttachmentIds.forEachIndexed { index, attachmentId ->
                ProofAttachmentRow(
                    index = index,
                    attachmentId = attachmentId,
                    onRemove = { onProofRemove(attachmentId) },
                    enabled = !form.isSubmitting,
                )
            }
        }
    }
}

@Composable
private fun ProofAttachmentRow(
    index: Int,
    attachmentId: String,
    onRemove: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimensions.viewPadding12,
                    end = Dimensions.viewPadding4,
                    top = Dimensions.viewPadding4,
                    bottom = Dimensions.viewPadding4,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Icon(
                imageVector = AppIcons.Description,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.viewSize18),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "PDF ${index + 1}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
                onClick = onRemove,
                enabled = enabled,
            ) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = stringResource(Res.string.secretary_add_account_proof_remove),
                    modifier = Modifier.size(Dimensions.viewSize18),
                )
            }
        }
    }
}
