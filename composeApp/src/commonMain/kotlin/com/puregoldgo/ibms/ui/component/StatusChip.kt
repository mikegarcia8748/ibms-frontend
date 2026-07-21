package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.puregoldgo.ibms.ui.theme.Dimensions

/**
 * The small uppercase pill used for a record's standing — `ACTIVE`, `CLOSED`,
 * a role, an employee number.
 *
 * Colour is chosen by [tone] rather than passed in, so the same status never
 * renders two ways on two screens.
 */
enum class ChipTone { Neutral, Positive, Negative, Accent }

@Composable
fun StatusChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Neutral,
) {
    val container: Color = when (tone) {
        ChipTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHighest
        ChipTone.Positive -> MaterialTheme.colorScheme.secondaryContainer
        ChipTone.Negative -> MaterialTheme.colorScheme.errorContainer
        ChipTone.Accent -> MaterialTheme.colorScheme.primaryContainer
    }
    val content: Color = when (tone) {
        ChipTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        ChipTone.Positive -> MaterialTheme.colorScheme.onSecondaryContainer
        ChipTone.Negative -> MaterialTheme.colorScheme.onErrorContainer
        ChipTone.Accent -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.viewRadius6),
        color = container,
        contentColor = content,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = Dimensions.viewPadding8,
                vertical = Dimensions.viewPadding4,
            ),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}
