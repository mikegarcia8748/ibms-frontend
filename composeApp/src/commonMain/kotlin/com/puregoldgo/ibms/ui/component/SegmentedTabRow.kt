package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.puregoldgo.ibms.ui.theme.Dimensions

/**
 * The pill switch a control panel picks its section with: free-width labels in a
 * tray, the selected one raised onto its own surface.
 *
 * Built from plain surfaces rather than `TabRow`: these are free-width pills
 * inside a tray, not M3 tabs with an indicator, and forcing the component to
 * look like this costs more than drawing it.
 *
 * Generic over the tab type so each screen keeps its own enum — the sysadmin
 * panel switches three ways, the secretary panel six.
 */
@Composable
fun <T> SegmentedTabRow(
    tabs: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    isCompact: Boolean,
) {
    val row = @Composable {
        Surface(
            shape = RoundedCornerShape(Dimensions.viewRadius12),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(Dimensions.viewPadding6),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding4),
            ) {
                tabs.forEach { (tab, label) ->
                    val isSelected = tab == selected
                    Surface(
                        modifier = Modifier.clickable { onSelect(tab) },
                        shape = RoundedCornerShape(Dimensions.viewRadius8),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        tonalElevation = if (isSelected) {
                            Dimensions.viewElevation2
                        } else {
                            Dimensions.viewElevation0
                        },
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(
                                horizontal = Dimensions.viewPadding16,
                                vertical = Dimensions.viewPadding10,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }

    if (isCompact) {
        // Labels this long do not fit a phone; let the tray scroll rather than
        // truncating whichever one is off-screen.
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) { row() }
    } else {
        Row { row() }
    }
}
