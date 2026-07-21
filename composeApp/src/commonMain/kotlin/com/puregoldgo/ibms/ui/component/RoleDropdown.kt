package com.puregoldgo.ibms.ui.component

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.puregoldgo.ibms.shared.model.Role
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.role_finance
import ibmsispbillingmanagementsystem.composeapp.generated.resources.role_manager
import ibmsispbillingmanagementsystem.composeapp.generated.resources.role_payables
import ibmsispbillingmanagementsystem.composeapp.generated.resources.role_pending
import ibmsispbillingmanagementsystem.composeapp.generated.resources.role_secretary
import ibmsispbillingmanagementsystem.composeapp.generated.resources.role_sysadmin
import org.jetbrains.compose.resources.stringResource

/**
 * Picks one of the six roles.
 *
 * Deliberately not [IspFilterDropdown]: that one models "no selection" as a null
 * id, and a role is never absent — `PENDING` *is* the answer for an account
 * nobody has decided about yet. Bending the filter to serve both would make the
 * null case mean two different things.
 *
 * [label] goes to the screen reader rather than on screen: in a directory list
 * every row has one of these, and a visible "Role" caption on each would be
 * noise, but "Role for Angel V Rubio" is exactly what a screen reader needs to
 * tell them apart.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleDropdown(
    selected: Role,
    onSelect: (Role) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = roleLabel(selected),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .width(Dimensions.viewWidth160)
                .semantics { contentDescription = label },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true,
            shape = RoundedCornerShape(Dimensions.viewRadius8),
            textStyle = MaterialTheme.typography.bodyMedium,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Role.entries.forEach { role ->
                DropdownMenuItem(
                    text = { Text(roleLabel(role)) },
                    onClick = {
                        expanded = false
                        // A no-op PATCH would still revoke nothing and change
                        // nothing, but it would flicker the row into its busy
                        // state for no reason.
                        if (role != selected) onSelect(role)
                    },
                )
            }
        }
    }
}

/**
 * The human name for a role.
 *
 * `PENDING` reads as "No role yet" rather than "Pending": there is nothing
 * pending — no queue, no approval waiting to happen — it simply has no
 * permissions, and calling it pending is what made the old delegations panel
 * look like it was waiting for something.
 */
@Composable
fun roleLabel(role: Role): String = stringResource(
    when (role) {
        Role.SYSADMIN -> Res.string.role_sysadmin
        Role.SECRETARY -> Res.string.role_secretary
        Role.PAYABLES -> Res.string.role_payables
        Role.FINANCE -> Res.string.role_finance
        Role.MANAGER -> Res.string.role_manager
        Role.PENDING -> Res.string.role_pending
    },
)
