package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.puregoldgo.ibms.ui.theme.Dimensions

/**
 * The control panel's dialog shell: a capped, rounded surface with a tinted
 * header band, the caller's body, and the caller's footer.
 *
 * Built from a [Dialog] and plain surfaces instead of `AlertDialog`, which has
 * no room for a tinted header band, a drop zone or a split footer. The platform
 * default width is opted out of because these are wider than it allows and cap
 * themselves instead.
 *
 * [dismissOnClickOutside] and [dismissOnBackPress] default to the platform
 * behaviour and exist for the dialogs that must not lose what they are showing:
 * a stray click beside a temporary password would take the only readable copy of
 * it with them. Turning both off leaves the header's close button and the
 * footer's own action as the deliberate ways out.
 */
@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissOnClickOutside: Boolean = true,
    dismissOnBackPress: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = modifier
                .padding(Dimensions.viewPadding24)
                .widthIn(max = Dimensions.viewWidth560),
            shape = RoundedCornerShape(Dimensions.viewRadius16),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = Dimensions.viewElevation2,
        ) {
            Column(content = content)
        }
    }
}

/** Badge, title and the close affordance, on a tinted band. */
@Composable
fun AppDialogHeader(
    title: String,
    icon: ImageVector,
    onClose: () -> Unit,
    closeDescription: String,
    closeEnabled: Boolean = true,
    subtitle: String? = null,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimensions.viewPadding24,
                    end = Dimensions.viewPadding12,
                    top = Dimensions.viewPadding16,
                    bottom = Dimensions.viewPadding16,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(onClick = onClose, enabled = closeEnabled) {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = closeDescription,
                    modifier = Modifier.size(Dimensions.viewSize20),
                )
            }
        }
    }
}

/** The tray a dialog's actions sit in, right-aligned. */
@Composable
fun AppDialogFooter(content: @Composable RowScope.() -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.viewPadding16),
            horizontalArrangement = Arrangement.spacedBy(
                space = Dimensions.viewPadding12,
                alignment = Alignment.End,
            ),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * An inline notice inside a dialog body — an instruction, a warning, or the
 * backend's own rejection. Colour carries the meaning, so callers pass a
 * container/content pair from the scheme rather than picking one here.
 */
@Composable
fun AppDialogBanner(
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.Warning,
    body: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.viewPadding12),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimensions.viewSize18),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding2),
                content = body,
            )
        }
    }
}
