package com.puregoldgo.ibms.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized dimensional constants for the presentation layer.
 *
 * Prefer these over hardcoded `.dp` literals inside screen composables so that
 * spacing/sizing stays consistent and adjustable from a single location.
 */
object Dimensions {
    // Spacing scale
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacingXxl = 32.dp

    // Component sizes
    val buttonHeight = 48.dp
    val progressIndicatorSmall = 20.dp
    val progressIndicatorMedium = 24.dp
    val progressStrokeWidth = 2.dp
    val listBottomSpacer = 80.dp
    val cardElevation = 2.dp
}
