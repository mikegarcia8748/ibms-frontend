package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_add
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_export
import org.jetbrains.compose.resources.stringResource

/**
 * The two list actions, shared by the branch and account tabs so `+ ADD` never
 * sits a little differently on one of them.
 *
 * Tonal rather than filled: they sit in a card header beside a search field, and
 * a filled button there would out-shout the list it acts on.
 */

@Composable
internal fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
    ) {
        Icon(
            imageVector = AppIcons.Add,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize18),
        )
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(stringResource(Res.string.secretary_add))
    }
}

@Composable
internal fun ExportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            imageVector = AppIcons.Download,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize18),
        )
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(stringResource(Res.string.secretary_export))
    }
}
