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
import com.puregoldgo.ibms.ui.component.AlphabetRail
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.LabeledDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_all_isps
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_branches_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_branches_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_branches_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_retry
import org.jetbrains.compose.resources.stringResource

/**
 * Every open branch, filtered three ways at once: by initial letter, by ISP, and
 * by free text. Closed ones live in the archive.
 */
@Composable
internal fun BranchLocationsTab(
    uiState: SecretaryUIState,
    callback: SecretaryCallback,
    isCompact: Boolean,
) {
    val allIspsLabel = stringResource(Res.string.console_all_isps)

    val ispFilter = @Composable { modifier: Modifier ->
        LabeledDropdown(
            options = uiState.activeProviders.providerFilterOptions(),
            selectedId = uiState.branchProviderId,
            onSelect = callback.onBranchProviderSelect,
            placeholder = allIspsLabel,
            clearLabel = allIspsLabel,
            modifier = modifier,
        )
    }
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.branchQuery,
            onValueChange = callback.onBranchQueryChange,
            placeholder = stringResource(Res.string.secretary_branches_search_hint),
            modifier = modifier,
        )
    }

    val card = @Composable { modifier: Modifier ->
        SectionCard(
            title = stringResource(Res.string.secretary_branches_title),
            icon = AppIcons.Domain,
            modifier = modifier,
            trailing = if (isCompact) {
                null
            } else {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
                    ) {
                        ispFilter(Modifier.width(Dimensions.viewWidth180))
                        search(Modifier.width(Dimensions.viewWidth280))
                        AddButton(callback.onAddBranchClick)
                    }
                }
            },
        ) {
            if (isCompact) {
                // No room for the controls beside the title — give them a row of
                // their own rather than shrinking them into unusability.
                ispFilter(Modifier.fillMaxWidth())
                Spacer(Modifier.height(Dimensions.viewPadding8))
                search(Modifier.fillMaxWidth())
                Spacer(Modifier.height(Dimensions.viewPadding8))
                AddButton(callback.onAddBranchClick, Modifier.fillMaxWidth())
                Spacer(Modifier.height(Dimensions.viewPadding16))
            }

            when {
                uiState.isLoading -> SectionLoadingState()

                uiState.loadError != null -> SectionErrorState(
                    message = uiState.loadError,
                    onRetry = callback.onRetryLoad,
                    retryLabel = stringResource(Res.string.secretary_retry),
                )

                uiState.visibleBranches.isEmpty() ->
                    SectionEmptyState(stringResource(Res.string.secretary_branches_empty))

                else -> LazyColumn(
                    // Bounded: this list sits inside a scrolling page, so it
                    // needs a height of its own to scroll within.
                    modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    items(uiState.visibleBranches, key = { it.id }) { branch ->
                        BranchCard(branch) { callback.onBranchClick(branch.id) }
                    }
                }
            }
        }
    }

    val rail = @Composable {
        AlphabetRail(
            letters = uiState.branchLetters,
            selected = uiState.branchLetter,
            onSelect = callback.onBranchLetterSelect,
            isCompact = isCompact,
        )
    }

    if (isCompact) {
        Column {
            rail()
            Spacer(Modifier.height(Dimensions.viewPadding12))
            card(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding16)) {
            rail()
            card(Modifier.weight(1f))
        }
    }
}

@Composable
private fun BranchCard(branch: SecretaryBranchRow, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Text(
                text = branch.branchCode,
                // Monospaced so codes of differing length still line up.
                style = MaterialTheme.typography.bodyMedium
                    .copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = branch.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        BranchStatusChip(branch.status)
    }
}
