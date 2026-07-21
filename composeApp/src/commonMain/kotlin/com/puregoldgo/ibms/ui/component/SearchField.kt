package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_clear_search_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_search_content_description
import org.jetbrains.compose.resources.stringResource

/**
 * A single-line search box with a leading magnifier and a clear button that
 * appears only once there is something to clear.
 */
@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(text = placeholder, style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = stringResource(Res.string.dashboard_search_content_description),
                modifier = Modifier.size(Dimensions.viewSize18),
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = stringResource(
                            Res.string.dashboard_clear_search_content_description,
                        ),
                        modifier = Modifier.size(Dimensions.viewSize18),
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(Dimensions.viewRadius8),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}
