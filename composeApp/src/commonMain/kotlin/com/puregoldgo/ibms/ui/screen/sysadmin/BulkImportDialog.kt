package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_attention
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_attention_body
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_browse
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_cancel
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_change_file
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_drop_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_drop_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_size_bytes
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_size_kb
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_size_mb
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_start
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_import_uploading
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_dismiss
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_provider_line
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_provider_new
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_providers
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_rows
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_skipped
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_stores
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_bulk_upload_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** What the picker offers. The backend takes only XLSX and says so itself for the rest. */
private val ACCEPTED_TYPES = listOf(
    ".xlsx",
    ".csv",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "text/csv",
)

/**
 * The master-list importer.
 *
 * One dialog rather than two: choosing a file, watching it upload and reading
 * what it did are three views of the same task, and bouncing the user between
 * surfaces would lose the file name the summary is about.
 */
@Composable
internal fun BulkImportDialog(
    uiState: SysadminUIState,
    onFilePicked: (PickedFile) -> Unit,
    onStartImport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val summary = uiState.bulkImportSummary
    val isDone = summary != null

    AppDialog(onDismissRequest = onDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.sysadmin_bulk_import_title),
            icon = AppIcons.Description,
            onClose = onDismiss,
            closeDescription = stringResource(Res.string.sysadmin_bulk_import_close),
            // Closing mid-upload would orphan a request that is still going to
            // commit; the only honest option is to wait.
            closeEnabled = !uiState.isBulkImporting,
        )

        Column(
            modifier = Modifier
                // A long list of skipped rows must not push the footer off a
                // laptop screen.
                .heightIn(max = Dimensions.viewHeight480)
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.viewPadding24),
        ) {
            if (summary != null) {
                ImportSummaryBody(summary)
            } else {
                DropZone(
                    fileName = uiState.bulkImportFileName,
                    fileSize = uiState.bulkImportFileSize,
                    enabled = !uiState.isBulkImporting,
                    onFilePicked = onFilePicked,
                )

                Spacer(Modifier.height(Dimensions.viewPadding20))

                if (uiState.bulkImportError != null) {
                    ErrorBanner(uiState.bulkImportError)
                } else {
                    AttentionBanner()
                }
            }
        }

        DialogFooter(
            isDone = isDone,
            isImporting = uiState.isBulkImporting,
            canStart = uiState.canStartImport,
            onDismiss = onDismiss,
            onStartImport = onStartImport,
        )
    }
}

/**
 * The dashed target.
 *
 * Clickable everywhere and droppable where the platform allows it — see
 * `fileDropTarget`. Both routes end in the same callback, so a dropped file and
 * a browsed one are indistinguishable from here on.
 */
@Composable
private fun DropZone(
    fileName: String?,
    fileSize: Long,
    enabled: Boolean,
    onFilePicked: (PickedFile) -> Unit,
) {
    var isDragOver by remember { mutableStateOf(false) }

    val launcher = rememberFilePicker(accept = ACCEPTED_TYPES, onPicked = onFilePicked)

    val outline = if (isDragOver) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val fill = if (isDragOver) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val radius = Dimensions.viewRadius12

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
                onDragOver = { isDragOver = it },
                onDropped = onFilePicked,
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
                text = fileName ?: stringResource(Res.string.sysadmin_bulk_import_drop_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = if (fileName == null) {
                    stringResource(Res.string.sysadmin_bulk_import_drop_hint)
                } else {
                    fileSizeLabel(fileSize)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Dimensions.viewPadding4))

            if (fileName == null) {
                Button(
                    onClick = { launcher.launch() },
                    enabled = enabled,
                    shape = RoundedCornerShape(Dimensions.viewRadius8),
                ) {
                    Text(stringResource(Res.string.sysadmin_bulk_import_browse))
                }
            } else {
                TextButton(onClick = { launcher.launch() }, enabled = enabled) {
                    Text(stringResource(Res.string.sysadmin_bulk_import_change_file))
                }
            }
        }
    }
}

/** What the spreadsheet has to look like. Shown before the upload, not after it fails. */
@Composable
private fun AttentionBanner() {
    AppDialogBanner(
        container = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = stringResource(Res.string.sysadmin_bulk_import_attention),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(Res.string.sysadmin_bulk_import_attention_body),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

/**
 * The backend's own words.
 *
 * Its rejections name the offending column or row — "missing required column
 * 'Store Code' in header row" tells a sysadmin exactly what to fix — so the
 * message is shown verbatim rather than translated into house copy.
 */
@Composable
private fun ErrorBanner(message: String) {
    AppDialogBanner(
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * What the import did.
 *
 * Reports reused counts alongside created ones because the import is idempotent
 * — re-uploading the same spreadsheet is a legitimate thing to do, and "0
 * created, 170 reused" is a success, not a failure. Skipped rows are listed with
 * their reasons rather than summarised to a number: a skipped row is a row
 * someone has to go fix in the source file.
 */
@Composable
private fun ImportSummaryBody(summary: BulkImportSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding4)) {
        Text(
            text = stringResource(Res.string.sysadmin_bulk_upload_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(Dimensions.viewPadding4))

        Text(
            text = stringResource(
                Res.string.sysadmin_bulk_upload_stores,
                summary.storesCreated,
                summary.storesReused,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(
                Res.string.sysadmin_bulk_upload_accounts,
                summary.accountsCreated,
                summary.accountsReused,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(
                Res.string.sysadmin_bulk_upload_rows,
                summary.totalRows - summary.rowsSkipped,
                summary.totalRows,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Only worth a section of its own when the file actually spanned more
        // than one ISP; for the common single-provider import the account totals
        // above already say the same thing.
        if (summary.providers.size > 1) {
            Spacer(Modifier.height(Dimensions.viewPadding8))
            Text(
                text = stringResource(Res.string.sysadmin_bulk_upload_providers),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            summary.providers.forEach { provider ->
                Text(
                    text = stringResource(
                        if (provider.created) {
                            Res.string.sysadmin_bulk_upload_provider_new
                        } else {
                            Res.string.sysadmin_bulk_upload_provider_line
                        },
                        provider.name,
                        provider.accountsCreated,
                        provider.accountsReused,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (summary.skipReasons.isNotEmpty()) {
            Spacer(Modifier.height(Dimensions.viewPadding8))
            Text(
                text = stringResource(Res.string.sysadmin_bulk_upload_skipped),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
            )
            summary.skipReasons.forEach { reason ->
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Cancel and commit, or a single dismiss once the import has answered. */
@Composable
private fun DialogFooter(
    isDone: Boolean,
    isImporting: Boolean,
    canStart: Boolean,
    onDismiss: () -> Unit,
    onStartImport: () -> Unit,
) {
    AppDialogFooter {
        if (isDone) {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(Dimensions.viewRadius8),
            ) {
                Text(stringResource(Res.string.sysadmin_bulk_upload_dismiss))
            }
            return@AppDialogFooter
        }

        OutlinedButton(
            onClick = onDismiss,
            enabled = !isImporting,
            shape = RoundedCornerShape(Dimensions.viewRadius8),
        ) {
            Text(stringResource(Res.string.sysadmin_bulk_import_cancel))
        }

        Button(
            onClick = onStartImport,
            enabled = canStart,
            shape = RoundedCornerShape(Dimensions.viewRadius8),
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimensions.viewSize18),
                    strokeWidth = Dimensions.viewStroke2,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    imageVector = AppIcons.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.viewSize18),
                )
            }
            Spacer(Modifier.width(Dimensions.viewPadding8))
            Text(
                stringResource(
                    if (isImporting) {
                        Res.string.sysadmin_bulk_import_uploading
                    } else {
                        Res.string.sysadmin_bulk_import_start
                    },
                ),
            )
        }
    }
}

private const val BYTES_PER_KB = 1024L
private const val BYTES_PER_MB = BYTES_PER_KB * 1024L

/**
 * A human-sized file size.
 *
 * Hand-rolled because there is no multiplatform number formatter, and one
 * decimal place is all this needs — the reader is confirming they picked the
 * right spreadsheet, not auditing bytes.
 */
@Composable
private fun fileSizeLabel(bytes: Long): String = when {
    bytes >= BYTES_PER_MB ->
        stringResource(Res.string.sysadmin_bulk_import_size_mb, oneDecimal(bytes, BYTES_PER_MB))

    bytes >= BYTES_PER_KB ->
        stringResource(Res.string.sysadmin_bulk_import_size_kb, oneDecimal(bytes, BYTES_PER_KB))

    else -> stringResource(Res.string.sysadmin_bulk_import_size_bytes, bytes.toInt())
}

private fun oneDecimal(bytes: Long, unit: Long): String {
    val tenths = (bytes.toDouble() / unit * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
