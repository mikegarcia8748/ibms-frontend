package com.puregoldgo.ibms.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized dimensional constants for the presentation layer.
 *
 * Naming convention:
 * - Padding / spacing  → viewPadding<size>
 * - Text size          → textSize<size>
 * - Component height   → viewHeight<size>
 * - Component width    → viewWidth<size>
 * - Component size     → viewSize<size>  (both width & height)
 * - Elevation          → viewElevation<size>
 * - Corner radius      → viewRadius<size>
 * - Stroke width       → viewStroke<size>
 *
 * Prefer these over hardcoded `.dp` / `.sp` literals inside screen composables
 * so that sizing stays consistent and adjustable from a single location.
 */
object Dimensions {
    // Padding / spacing
    val viewPadding4 = 4.dp
    val viewPadding8 = 8.dp
    val viewPadding12 = 12.dp
    val viewPadding16 = 16.dp
    val viewPadding24 = 24.dp
    val viewPadding32 = 32.dp
    val viewPadding48 = 48.dp

    // Component heights
    val viewHeight2 = 2.dp
    val viewHeight4 = 4.dp
    val viewHeight48 = 48.dp
    val viewHeight64 = 64.dp
    val viewHeight80 = 80.dp

    // Component widths
    val viewWidth24 = 24.dp
    val viewWidth420 = 420.dp
    val viewWidth600 = 600.dp

    // Component sizes (both width & height)
    val viewSize20 = 20.dp
    val viewSize24 = 24.dp
    val viewSize80 = 80.dp

    // Elevation
    val viewElevation2 = 2.dp

    // Corner radius
    val viewRadius24 = 24.dp

    // Stroke width
    val viewStroke2 = 2.dp
}
