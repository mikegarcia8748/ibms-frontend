package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.puregoldgo.ibms.ui.theme.Dimensions
import com.puregoldgo.ibms.shared.validation.PasswordPolicy
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_requirements_title
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_digit
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_lowercase
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_max_length
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_met_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_min_length
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_no_spaces
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_not_username
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_unmet_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_rule_uppercase
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_strength_fair
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_strength_good
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_strength_label
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_strength_strong
import ibmsispbillingmanagementsystem.composeapp.generated.resources.password_strength_weak
import org.jetbrains.compose.resources.stringResource

/**
 * The policy, shown in full and ticking off as the user types.
 *
 * Every rule is on screen from the moment the field appears rather than being
 * revealed one rejection at a time — the user can satisfy all of them in a
 * single pass instead of playing twenty questions with the server.
 *
 * The markers are drawn rather than set in a font: a check glyph would depend on
 * whatever typeface the target happens to ship, and Compose on the web canvas
 * has no fallback to fall back to.
 */
@Composable
fun PasswordRequirements(
    rules: List<PasswordPolicy.Rule>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.password_requirements_title),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Dimensions.viewPadding8))
        rules.forEach { rule ->
            RequirementRow(rule)
        }
    }
}

@Composable
private fun RequirementRow(rule: PasswordPolicy.Rule) {
    val label = rule.id.label()
    val stateDescription = if (rule.satisfied) {
        stringResource(Res.string.password_rule_met_content_description)
    } else {
        stringResource(Res.string.password_rule_unmet_content_description)
    }
    val tint = if (rule.satisfied) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = "$label. $stateDescription" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RuleMarker(satisfied = rule.satisfied, tint = tint)
        Spacer(Modifier.width(Dimensions.viewPadding8))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = tint,
        )
    }
}

/** A drawn check (met) or hollow ring (not yet) — see the note above on fonts. */
@Composable
private fun RuleMarker(satisfied: Boolean, tint: Color) {
    Canvas(modifier = Modifier.size(Dimensions.viewSize16)) {
        val stroke = size.minDimension * 0.12f
        if (satisfied) {
            val start = Offset(size.width * 0.20f, size.height * 0.55f)
            val middle = Offset(size.width * 0.42f, size.height * 0.76f)
            val end = Offset(size.width * 0.80f, size.height * 0.28f)
            drawLine(tint, start, middle, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(tint, middle, end, strokeWidth = stroke, cap = StrokeCap.Round)
        } else {
            drawCircle(
                color = tint,
                radius = size.minDimension / 2f - stroke,
                style = Stroke(width = stroke),
            )
        }
    }
}

/**
 * Advisory strength meter. It never gates the submit button — the policy does
 * that — it only tells the user whether they cleared the bar by a hair or by a
 * mile.
 */
@Composable
fun PasswordStrengthBar(
    strength: PasswordPolicy.Strength,
    modifier: Modifier = Modifier,
) {
    if (strength == PasswordPolicy.Strength.None) return

    val filled = when (strength) {
        PasswordPolicy.Strength.None -> 0
        PasswordPolicy.Strength.Weak -> 1
        PasswordPolicy.Strength.Fair -> 2
        PasswordPolicy.Strength.Good -> 3
        PasswordPolicy.Strength.Strong -> 4
    }
    val color = when (strength) {
        PasswordPolicy.Strength.Strong, PasswordPolicy.Strength.Good -> MaterialTheme.colorScheme.primary
        PasswordPolicy.Strength.Fair -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    val track = MaterialTheme.colorScheme.surfaceVariant
    val label = strength.label()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = label
            },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding4),
        ) {
            repeat(4) { index ->
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.viewHeight6)
                        .clip(RoundedCornerShape(Dimensions.viewHeight6))
                        .background(if (index < filled) color else track),
                )
            }
        }
        Spacer(Modifier.height(Dimensions.viewPadding4))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PasswordPolicy.Strength.label(): String = when (this) {
    PasswordPolicy.Strength.None -> ""
    PasswordPolicy.Strength.Weak -> strengthLabel(Res.string.password_strength_weak)
    PasswordPolicy.Strength.Fair -> strengthLabel(Res.string.password_strength_fair)
    PasswordPolicy.Strength.Good -> strengthLabel(Res.string.password_strength_good)
    PasswordPolicy.Strength.Strong -> strengthLabel(Res.string.password_strength_strong)
}

@Composable
private fun strengthLabel(resource: org.jetbrains.compose.resources.StringResource): String =
    stringResource(Res.string.password_strength_label) + ": " + stringResource(resource)

@Composable
private fun PasswordPolicy.RuleId.label(): String = when (this) {
    PasswordPolicy.RuleId.MinLength -> stringResource(Res.string.password_rule_min_length)
    PasswordPolicy.RuleId.MaxLength -> stringResource(Res.string.password_rule_max_length)
    PasswordPolicy.RuleId.Uppercase -> stringResource(Res.string.password_rule_uppercase)
    PasswordPolicy.RuleId.Lowercase -> stringResource(Res.string.password_rule_lowercase)
    PasswordPolicy.RuleId.Digit -> stringResource(Res.string.password_rule_digit)
    PasswordPolicy.RuleId.NoSpaces -> stringResource(Res.string.password_rule_no_spaces)
    PasswordPolicy.RuleId.NotUsername -> stringResource(Res.string.password_rule_not_username)
}
