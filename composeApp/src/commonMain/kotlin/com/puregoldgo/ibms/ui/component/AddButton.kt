package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_add
import org.jetbrains.compose.resources.stringResource

/**
 * The `+ ADD` affordance that appears on list cards.
 *
 * A [TextButton] keeps it visually lighter than the primary actions beside it,
 * and the leading plus icon matches the user's wording for creating a record.
 */
@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = stringResource(Res.string.console_add),
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = AppIcons.Add,
            contentDescription = null,
            modifier = Modifier.size(Dimensions.viewSize18),
        )
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(label)
    }
}
