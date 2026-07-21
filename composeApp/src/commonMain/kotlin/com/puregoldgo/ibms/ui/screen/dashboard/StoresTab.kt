package com.puregoldgo.ibms.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
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
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_branch_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_branch_locations
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_branch_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_letter_all
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_status_active
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_status_inactive
import org.jetbrains.compose.resources.stringResource

/**
 * Every branch, filtered three ways at once: by initial letter, by ISP, and by
 * free text.
 */
@Composable
internal fun StoresTab(
    uiState: DashboardUIState,
    callback: DashboardCallback,
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
            title = stringResource(Res.string.dashboard_branch_locations),
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
                            placeholder = stringResource(Res.string.dashboard_branch_search_hint),
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
                    placeholder = stringResource(Res.string.dashboard_branch_search_hint),
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
                    retryLabel = stringResource(Res.string.dashboard_retry),
                )
            } else if (uiState.visibleBranches.isEmpty()) {
                SectionEmptyState(stringResource(Res.string.dashboard_branch_empty))
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

/**
 * The A–Z index. A column beside the list when there is width for it, a
 * horizontal strip above it when there is not.
 */
@Composable
private fun AlphabetRail(
    letters: List<Char>,
    selected: Char,
    onSelect: (Char) -> Unit,
    isCompact: Boolean,
) {
    val allLabel = stringResource(Res.string.dashboard_letter_all)
    val entries = listOf(LETTER_ALL to allLabel) + letters.map { it to it.toString() }

    if (isCompact) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            items(entries, key = { it.first }) { (letter, label) ->
                LetterButton(label, letter == selected) { onSelect(letter) }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            items(entries, key = { it.first }) { (letter, label) ->
                LetterButton(label, letter == selected) { onSelect(letter) }
            }
        }
    }
}

@Composable
private fun LetterButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(Dimensions.viewSize36)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
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
            if (isActive) Res.string.dashboard_status_active else Res.string.dashboard_status_inactive,
        ),
        tone = if (isActive) ChipTone.Positive else ChipTone.Negative,
    )
}

internal fun List<IspProviderRow>.toFilterOptions(): List<FilterOption> =
    map { FilterOption(id = it.id, label = it.name) }
