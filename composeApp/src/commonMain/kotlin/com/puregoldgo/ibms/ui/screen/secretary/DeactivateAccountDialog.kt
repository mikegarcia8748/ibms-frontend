package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.puregoldgo.ibms.platform.file.PickedFile
import com.puregoldgo.ibms.platform.file.fileDropTarget
import com.puregoldgo.ibms.platform.file.rememberFilePicker
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogBanner
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_account_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_branch_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_change_file
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_circuit_id_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_confirm
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_drop_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_drop_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_oversize
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_proof_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_warning
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_deactivate_wrong_type
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private val PDF_ACCEPTED_TYPES = listOf(
    ".pdf",
    "application/pdf",
)

private const val BYTES_PER_KB = 1024L
private const val BYTES_PER_MB = BYTES_PER_KB * BYTES_PER_KB
private const val MAX_FILE_BYTES = 5L * BYTES_PER_MB

/**
 * Confirmation dialog for requesting account deactivation.
 *
 * Shows the account being terminated, asks for a PDF proof, and confirms the
 * request. The actual `POST /accounts/{id}/deactivate` call is not wired yet;
 * a [NotConnectedBanner] makes that clear.
 */
@Composable
internal fun DeactivateAccountDialog(
    form: DeactivateAccountForm,
    callback: SecretaryCallback,
) {
    AppDialog(onDismissRequest = callback.onDeactivateAccountDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_deactivate_title),
            subtitle = stringResource(Res.string.secretary_deactivate_subtitle),
            icon = AppIcons.PowerSettingsNew,
            onClose = callback.onDeactivateAccountDismiss,
            closeDescription = stringResource(Res.string.secretary_deactivate_close),
            closeEnabled = !form.isSubmitting,
        )

        Column(
            modifier = Modifier
                .padding(Dimensions.viewPadding24)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16),
        ) {
            WarningBanner()
            AccountInfoCard(form)
            ProofDropZone(
                proofFile = form.proofFile,
                enabled = !form.isSubmitting,
                onFilePicked = callback.onDeactivateAccountFilePicked,
            )
            if (form.errorMessage != null) {
                ErrorBanner(form.errorMessage)
            }
            NotConnectedBanner()
        }

        AppDialogFooter {
            TextButton(
                onClick = callback.onDeactivateAccountDismiss,
                enabled = !form.isSubmitting,
            ) {
                Text(stringResource(Res.string.secretary_deactivate_cancel))
            }
            Button(
                onClick = callback.onDeactivateAccountConfirm,
                enabled = form.proofFile != null && !form.isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                if (form.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize18),
                        strokeWidth = Dimensions.viewStroke2,
                        color = MaterialTheme.colorScheme.onError,
                    )
                }
                Text(stringResource(Res.string.secretary_deactivate_confirm))
            }
        }
    }
}

@Composable
private fun WarningBanner() {
    AppDialogBanner(
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
        icon = AppIcons.Warning,
    ) {
        Text(
            text = stringResource(Res.string.secretary_deactivate_warning),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    AppDialogBanner(
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun AccountInfoCard(form: DeactivateAccountForm) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.viewPadding16),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            InfoRow(
                label = stringResource(Res.string.secretary_deactivate_account_label),
                value = form.accountNumber,
                isMonospace = true,
            )
            InfoRow(
                label = stringResource(Res.string.secretary_deactivate_circuit_id_label),
                value = form.circuitId ?: "—",
                isMonospace = true,
            )
            InfoRow(
                label = stringResource(Res.string.secretary_deactivate_branch_label),
                value = form.branchLabel,
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(Dimensions.viewPadding4))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = if (isMonospace) FontFamily.Monospace else null,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Clickable, droppable target for the deactivation proof PDF.
 *
 * Validates size (≤5MB) and type before forwarding the file to the ViewModel.
 * Drag-and-drop only works where [fileDropTarget] has a real implementation;
 * everywhere else it is tap-only.
 */
@Composable
private fun ProofDropZone(
    proofFile: PickedFile?,
    enabled: Boolean,
    onFilePicked: (PickedFile) -> Unit,
) {
    var dragOver by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val oversizeMessage = stringResource(Res.string.secretary_deactivate_oversize)
    val wrongTypeMessage = stringResource(Res.string.secretary_deactivate_wrong_type)

    val launcher = rememberFilePicker(
        accept = PDF_ACCEPTED_TYPES,
        onPicked = { file ->
            localError = null
            when {
                file.size > MAX_FILE_BYTES -> {
                    localError = oversizeMessage
                }

                file.mimeType != "application/pdf" &&
                    !file.name.endsWith(".pdf", ignoreCase = true) -> {
                    localError = wrongTypeMessage
                }

                else -> onFilePicked(file)
            }
        },
    )

    val outline = if (dragOver) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val fill = if (dragOver) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val radius = Dimensions.viewRadius12

    Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
    ) {
        Text(
            text = stringResource(Res.string.secretary_deactivate_proof_label),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.viewHeight200)
                .background(color = fill, shape = RoundedCornerShape(radius))
                .drawBehind {
                    drawRoundRect(
                        color = outline,
                        style = Stroke(
                            width = Dimensions.viewStroke2.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(
                                    Dimensions.viewDashOn.toPx(),
                                    Dimensions.viewDashOff.toPx(),
                                ),
                            ),
                        ),
                        cornerRadius = CornerRadius(radius.toPx()),
                    )
                }
                .fileDropTarget(
                    enabled = enabled,
                    onDragOver = { dragOver = it },
                    onDropped = { file ->
                        dragOver = false
                        when {
                            file.size > MAX_FILE_BYTES -> {
                                localError = oversizeMessage
                            }

                            file.mimeType != "application/pdf" &&
                                !file.name.endsWith(".pdf", ignoreCase = true) -> {
                                localError = wrongTypeMessage
                            }

                            else -> onFilePicked(file)
                        }
                    },
                )
                .clickable(enabled = enabled) { launcher.launch() }
                .padding(Dimensions.viewPadding16),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                Surface(
                    shape = RoundedCornerShape(Dimensions.viewRadius12),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Icon(
                        imageVector = AppIcons.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(Dimensions.viewPadding12)
                            .size(Dimensions.viewSize24),
                    )
                }

                Text(
                    text = proofFile?.name
                        ?: stringResource(Res.string.secretary_deactivate_drop_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = proofFile?.let { fileSizeLabel(it.size) }
                        ?: stringResource(Res.string.secretary_deactivate_drop_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                if (proofFile != null) {
                    TextButton(
                        onClick = { launcher.launch() },
                        enabled = enabled,
                    ) {
                        Text(stringResource(Res.string.secretary_deactivate_change_file))
                    }
                }
            }
        }

        if (localError != null) {
            ErrorBanner(localError!!)
        }
    }
}

@Composable
private fun fileSizeLabel(bytes: Long): String = when {
    bytes >= BYTES_PER_MB -> "${oneDecimal(bytes, BYTES_PER_MB)} MB"
    bytes >= BYTES_PER_KB -> "${oneDecimal(bytes, BYTES_PER_KB)} KB"
    else -> "$bytes B"
}

private fun oneDecimal(bytes: Long, unit: Long): String {
    val tenths = (bytes.toDouble() / unit * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
