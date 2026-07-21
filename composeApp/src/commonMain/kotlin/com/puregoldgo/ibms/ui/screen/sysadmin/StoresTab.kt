package com.puregoldgo.ibms.ui.screen.sysadmin

import androidx.compose.foundation.background
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
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.FilterOption
import com.puregoldgo.ibms.ui.component.IspFilterDropdown
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_branch_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_branch_locations
import ibmsispbillingmanagementsystem.composeapp.generated.resources.sysadmin_branch_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_status_active
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_status_inactive
import org.jetbrains.compose.resources.stringResource

/**
 * Every branch, filtered three ways at once: by initial letter, by ISP, and by
 * free text.
 */
@Composable
internal fun StoresTab(
    uiState: SysadminUIState,
    callback: SysadminCallback,
    isCompact: Boolean,
) {
    val rail = @Composable {
        AlphabetRail(
            letters = uiState.branchLetters,
            selected = uiState.branchLetter,
            onSelect = callback.onBranchLetterSelect,
            isCompact = isCompact,
        )
    }

    val card = @Composable { modifier: Modifier ->
        SectionCard(
            title = stringResource(Res.string.sysadmin_branch_locations),
            icon = AppIcons.Domain,
            modifier = modifier,
            trailing = if (isCompact) {
                null
            } else {
                {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12)) {
                        IspFilterDropdown(
                            options = uiState.activeProviders.toFilterOptions(),
                            selectedId = uiState.branchProviderId,
                            onSelect = callback.onBranchProviderSelect,
                        )
                        SearchField(
                            value = uiState.branchQuery,
                            onValueChange = callback.onBranchQueryChange,
                            placeholder = stringResource(Res.string.sysadmin_branch_search_hint),
                            modifier = Modifier.width(Dimensions.viewWidth280),
                        )
                    }
                }
            },
        ) {
            if (isCompact) {
                // No room for the controls beside the title — give them a row
                // of their own rather than shrinking them into unusability.
                IspFilterDropdown(
                    options = uiState.activeProviders.toFilterOptions(),
                    selectedId = uiState.branchProviderId,
                    onSelect = callback.onBranchProviderSelect,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Dimensions.viewPadding8))
                SearchField(
                    value = uiState.branchQuery,
                    onValueChange = callback.onBranchQueryChange,
                    placeholder = stringResource(Res.string.sysadmin_branch_search_hint),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Dimensions.viewPadding16))
            }

            if (uiState.isLoading) {
                SectionLoadingState()
            } else if (uiState.loadError != null) {
                SectionErrorState(
                    message = uiState.loadError,
                    onRetry = callback.onRetryLoad,
                    retryLabel = stringResource(Res.string.console_retry),
                )
            } else if (uiState.visibleBranches.isEmpty()) {
                SectionEmptyState(stringResource(Res.string.sysadmin_branch_empty))
            } else {
                LazyColumn(
                    // Bounded: this list sits inside a scrolling page, so it
                    // needs a height of its own to scroll within.
                    modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    items(uiState.visibleBranches, key = { it.id }) { branch ->
                        BranchCard(branch)
                    }
                }
            }
        }
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
private fun BranchCard(branch: BranchRow) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
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
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = branch.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        RecordStatusChip(isActive = branch.isActive)
    }
}

/** `NAME (City)` when a city is known, plain name otherwise. */
private val BranchRow.displayName: String
    get() = if (city.isNullOrBlank()) name else "$name ($city)"

/** The ACTIVE / INACTIVE pill, so both list tabs word and colour it the same. */
@Composable
internal fun RecordStatusChip(isActive: Boolean) {
    StatusChip(
        label = stringResource(
            if (isActive) Res.string.console_status_active else Res.string.console_status_inactive,
        ),
        tone = if (isActive) ChipTone.Positive else ChipTone.Negative,
    )
}

internal fun List<IspProviderRow>.toFilterOptions(): List<FilterOption> =
    map { FilterOption(id = it.id, label = it.name) }
