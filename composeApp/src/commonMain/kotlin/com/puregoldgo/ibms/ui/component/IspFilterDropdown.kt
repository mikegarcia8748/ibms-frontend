package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.dashboard_all_isps
import org.jetbrains.compose.resources.stringResource

/** One selectable entry: an id to filter by, and the label to show for it. */
data class FilterOption(val id: String, val label: String)

/**
 * Filters a list down to one ISP, or to none at all.
 *
 * "All ISPs" is modelled as a null [selectedId] rather than a sentinel option,
 * so the absence of a filter and a filter that happens to match everything stay
 * distinguishable to the caller.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IspFilterDropdown(
    options: List<FilterOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val allLabel = stringResource(Res.string.dashboard_all_isps)
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label ?: allLabel

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .width(Dimensions.viewWidth180),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.viewRadius8),
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * The same control with its wording and its width left to the caller.
 *
 * [IspFilterDropdown] above hardcodes "All ISPs" and a 180dp field, which is
 * exactly right for the two places it is used and wrong everywhere else — a
 * status filter says "All Status", and a dialog field fills its row and must
 * *require* an answer. Rather than grow that one a pile of parameters, this
 * states the two things that actually vary:
 *
 * - [clearLabel] non-null adds a leading entry that selects nothing, for filters.
 *   Null omits it, making the choice required.
 * - [placeholder] is shown while nothing is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledDropdown(
    options: List<FilterOption>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    clearLabel: String? = null,
    label: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label
        ?: clearLabel
        ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = { Text(placeholder) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.viewRadius8),
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            if (clearLabel != null) {
                DropdownMenuItem(
                    text = { Text(clearLabel) },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    },
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
