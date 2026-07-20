package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_hide
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_hide_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_show
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_show_content_description
import org.jetbrains.compose.resources.stringResource

/**
 * A password input with a reveal toggle, used by both sign-in and set-password.
 *
 * Two decisions worth keeping:
 *
 *  - **Paste is not blocked.** Temporary passwords reach people through a chat
 *    message or a phone call, and generated ones are long — refusing paste would
 *    push users toward retyping them wrong, or toward choosing something they can
 *    remember instead.
 *  - **The reveal is a labelled control**, not a decorative glyph, so it reads
 *    correctly to a screen reader and needs no icon dependency.
 *
 * [imeAction] lets the last field in a form submit straight from the keyboard.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        isError = isError,
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction,
            autoCorrectEnabled = false,
            capitalization = KeyboardCapitalization.None,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() },
            onGo = { onImeAction() },
        ),
        supportingText = supportingText?.let {
            {
                Text(
                    text = it,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        trailingIcon = {
            val description = if (isVisible) {
                stringResource(Res.string.password_hide_content_description)
            } else {
                stringResource(Res.string.password_show_content_description)
            }
            TextButton(
                onClick = onVisibilityToggle,
                enabled = enabled,
                modifier = Modifier.clearAndSetSemantics { contentDescription = description },
            ) {
                Text(
                    text = if (isVisible) {
                        stringResource(Res.string.password_hide)
                    } else {
                        stringResource(Res.string.password_show)
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}
