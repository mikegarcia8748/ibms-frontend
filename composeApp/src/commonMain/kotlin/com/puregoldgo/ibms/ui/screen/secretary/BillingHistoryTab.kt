package com.puregoldgo.ibms.ui.screen.secretary

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_accounts
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_generated
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_period
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_total
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_billing_total_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_retry
import org.jetbrains.compose.resources.stringResource

/** Every topsheet already compiled, searchable by the invoice number on it. */
@Composable
internal fun BillingHistoryTab(
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
    isCompact: Boolean,
) {
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.invoiceQuery,
            onValueChange = callback.onInvoiceQueryChange,
            placeholder = stringResource(Res.string.secretary_billing_search_hint),
            modifier = modifier,
        )
    }

    SectionCard(
        title = stringResource(Res.string.secretary_billing_title),
        icon = AppIcons.Description,
        trailing = if (isCompact) null else ({ search(Modifier.width(Dimensions.viewWidth280)) }),
    ) {
        if (isCompact) {
            search(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding16))
        }

        when {
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.secretary_retry),
            )

            uiState.visibleTopSheets.isEmpty() ->
                SectionEmptyState(stringResource(Res.string.secretary_billing_empty))

            else -> LazyColumn(
                // Bounded so this list scrolls within the page rather than
                // stretching it to the length of the whole billing history.
                modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                items(uiState.visibleTopSheets, key = { it.id }) { sheet ->
                    TopSheetCard(sheet, isCompact, onClick = { callback.onTopSheetClick(sheet.id) })
                }
            }
        }
    }
}

@Composable
private fun TopSheetCard(sheet: TopSheetRow, isCompact: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(Dimensions.viewPadding16),
    ) {
        @Composable
        fun identity() {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    Text(
                        text = sheet.reference,
                        // Monospaced: invoice numbers get read aloud and
                        // compared character by character against the vendor's.
                        style = MaterialTheme.typography.bodyLarge
                            .copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    StatusChip(
                        label = sheet.providerName.uppercase(),
                        tone = ChipTone.Neutral,
                    )
                    TopSheetStatusChip(sheet.status)
                }

                Spacer(Modifier.height(Dimensions.viewPadding8))

                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
                    MetaText(stringResource(Res.string.secretary_billing_period, sheet.period))
                    MetaText(
                        stringResource(Res.string.secretary_billing_generated, sheet.generatedOn),
                    )
                    MetaText(
                        stringResource(Res.string.secretary_billing_accounts, sheet.accountCount),
                    )
                }
            }
        }

        @Composable
        fun total(alignment: Alignment.Horizontal) {
            Column(horizontalAlignment = alignment) {
                Text(
                    text = stringResource(Res.string.secretary_billing_total_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.secretary_billing_total, sheet.totalValidated),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                )
            }
        }

        if (isCompact) {
            // The metadata line alone is wider than a phone; stacking keeps the
            // total readable instead of squeezing both into unreadability.
            identity()
            Spacer(Modifier.height(Dimensions.viewPadding12))
            total(Alignment.Start)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                identity()
                total(Alignment.End)
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
