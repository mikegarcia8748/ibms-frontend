package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.puregoldgo.ibms.ui.theme.Dimensions

/**
 * A compact month stepper over a `YYYY-MM` period, e.g. `‹ July 2026 ›`.
 *
 * There is no calendar here on purpose: compilation picks a whole billing month,
 * never a day, and the backend rejects a future period — so [canGoNext] is false
 * once [period] reaches the current month and the forward arrow greys out.
 * [period] must already be a valid `YYYY-MM`; a malformed value renders verbatim
 * rather than crashing.
 */
@Composable
fun MonthPicker(
    period: String,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = AppIcons.ChevronLeft,
                    contentDescription = "Previous month",
                    modifier = Modifier.widthIn(min = Dimensions.viewSize20),
                )
            }
            Text(
                text = period.toMonthLabel(),
                modifier = Modifier.widthIn(min = Dimensions.viewWidth96),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            IconButton(onClick = onNext, enabled = canGoNext) {
                Icon(
                    imageVector = AppIcons.ChevronRight,
                    contentDescription = "Next month",
                    modifier = Modifier.widthIn(min = Dimensions.viewSize20),
                )
            }
        }
    }
}

/** `"2026-07"` → `"July 2026"`; anything unparseable is returned unchanged. */
private fun String.toMonthLabel(): String {
    val parts = split("-")
    val month = parts.getOrNull(1)?.toIntOrNull()
    val year = parts.getOrNull(0)
    if (month == null || year == null || month !in 1..12) return this
    return "${MONTH_NAMES[month - 1]} $year"
}

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
