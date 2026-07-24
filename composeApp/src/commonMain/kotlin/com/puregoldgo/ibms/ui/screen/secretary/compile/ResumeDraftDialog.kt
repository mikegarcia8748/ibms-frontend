package com.puregoldgo.ibms.ui.screen.secretary.compile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.puregoldgo.ibms.ui.component.AppDialog
import com.puregoldgo.ibms.ui.component.AppDialogFooter
import com.puregoldgo.ibms.ui.component.AppDialogHeader
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.screen.secretary.TopSheetStatusChip
import com.puregoldgo.ibms.ui.screen.secretary.buildTopSheetRows
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_generated
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_period
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_total
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_total_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_resume_close
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_resume_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_resume_subtitle
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_resume_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_retry
import org.jetbrains.compose.resources.stringResource

/**
 * Lists a secretary's DRAFT topsheets so an unfinished compilation can be picked
 * back up. Selecting a draft hands it to [CompileViewModel.onResumeDraft], which
 * loads its lines and drops the secretary straight into the RFP-entry phase — the
 * same circuit a freshly-created draft enters, so every edit and the confirm stay
 * in one place.
 *
 * `internal` so the compile tab can draw it; free of any ViewModel dependency.
 */
@Composable
internal fun ResumeDraftDialog(
    state: CompileUIState,
    callback: CompileCallback,
) {
    AppDialog(onDismissRequest = callback.onResumeDraftDismiss) {
        AppDialogHeader(
            title = stringResource(Res.string.secretary_compile_resume_title),
            icon = AppIcons.Description,
            onClose = callback.onResumeDraftDismiss,
            closeDescription = stringResource(Res.string.secretary_compile_resume_close),
            subtitle = stringResource(Res.string.secretary_compile_resume_subtitle),
        )

        Column(modifier = Modifier.padding(Dimensions.viewPadding24)) {
            // The drafts are mapped through the shared topsheet mapper so the
            // batch number, peso total and date read exactly as they do in
            // Billing History — one shape, everywhere a topsheet is listed.
            val rows = buildTopSheetRows(state.drafts)

            when {
                state.isLoadingDrafts -> SectionLoadingState()

                state.draftsError != null -> SectionErrorState(
                    message = state.draftsError,
                    onRetry = callback.onResumeDraftClick,
                    retryLabel = stringResource(Res.string.secretary_retry),
                )

                rows.isEmpty() ->
                    SectionEmptyState(stringResource(Res.string.secretary_compile_resume_empty))

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = Dimensions.viewHeight480),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    items(rows, key = { it.id }) { row ->
                        DraftCard(row) { callback.onResumeDraft(row.id) }
                    }
                }
            }
        }

        AppDialogFooter {
            TextButton(onClick = callback.onResumeDraftDismiss) {
                Text(stringResource(Res.string.secretary_compile_resume_close))
            }
        }
    }
}

@Composable
private fun DraftCard(
    row: com.puregoldgo.ibms.ui.screen.secretary.TopSheetRow,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(Dimensions.viewPadding16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Text(
                text = row.reference,
                // Monospaced: the batch number is read aloud and compared
                // character by character, same as an invoice number.
                style = MaterialTheme.typography.bodyLarge
                    .copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
            StatusChip(label = row.providerName.uppercase(), tone = ChipTone.Neutral)
            TopSheetStatusChip(row.status)
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                MetaText(stringResource(Res.string.secretary_billing_period, row.period))
                MetaText(stringResource(Res.string.secretary_billing_generated, row.generatedOn))
                MetaText(stringResource(Res.string.secretary_billing_accounts, row.accountCount))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.secretary_billing_total_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.secretary_billing_total, row.totalValidated),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
