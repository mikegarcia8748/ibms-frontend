package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.puregoldgo.ibms.ui.theme.Dimensions

/**
 * The rounded panel every section of the control panel sits in: an optional
 * icon badge, a title, optional trailing controls, then the caller's content.
 *
 * Pulled out because the dashboard repeats this shell six times — a card that
 * looked slightly different in each one would read as six unrelated widgets
 * rather than one surface.
 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    headerBusy: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius16),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimensions.viewElevation1,
    ) {
        Column(modifier = Modifier.padding(Dimensions.viewPadding20)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
            ) {
                if (icon != null) {
                    Surface(
                        shape = RoundedCornerShape(Dimensions.viewRadius8),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(Dimensions.viewPadding8)
                                .size(Dimensions.viewSize20),
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // A quiet spinner beside the title while the section silently
                // re-fetches — the rows below stay put, so this is the only sign
                // the list is being brought up to date.
                if (headerBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.viewSize16),
                        strokeWidth = Dimensions.viewStroke2,
                    )
                }

                if (trailing != null) {
                    // Pushes the controls to the far edge whatever the title's width.
                    Box(modifier = Modifier.weight(1f))
                    trailing()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.viewPadding16),
                content = content,
            )
        }
    }
}

/**
 * The centred, muted line a section shows when it has nothing to list. Stated
 * plainly rather than left blank — an empty panel reads as a failed load.
 */
@Composable
fun SectionEmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding32),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * The spinner a section shows while its rows are on their way.
 *
 * Occupies the same block as [SectionEmptyState] deliberately: without it, a
 * list that has not arrived yet is indistinguishable from one that came back
 * empty, and "no branches" is a much more alarming thing to read than a spinner.
 */
@Composable
fun SectionLoadingState(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.viewPadding32),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(Dimensions.viewSize24))
        }
    }
}

/**
 * A failed load, with the server's own wording and a way to try again.
 *
 * [message] is shown as received rather than replaced with generic copy — the
 * backend says things like "invalid cursor" that name the actual problem.
 */
@Composable
fun SectionErrorState(
    message: String,
    onRetry: () -> Unit,
    retryLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius12),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.viewPadding24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text(retryLabel)
            }
        }
    }
}
