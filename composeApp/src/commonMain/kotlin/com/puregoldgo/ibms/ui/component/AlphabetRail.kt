package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_letter_all
import org.jetbrains.compose.resources.stringResource

/** The `All` entry of the A–Z rail — no letter selected. */
const val LETTER_ALL: Char = ' '

/**
 * The A–Z index beside a long list. A column when there is width for it, a
 * horizontal strip above the list when there is not.
 *
 * [letters] is what the caller can actually file under, so the rail never
 * advertises a letter that resolves to an empty list.
 */
@Composable
fun AlphabetRail(
    letters: List<Char>,
    selected: Char,
    onSelect: (Char) -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
) {
    val allLabel = stringResource(Res.string.dashboard_letter_all)
    val entries = listOf(LETTER_ALL to allLabel) + letters.map { it to it.toString() }

    if (isCompact) {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            items(entries, key = { it.first }) { (letter, label) ->
                LetterButton(label, letter == selected) { onSelect(letter) }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.heightIn(max = Dimensions.viewHeight480),
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
