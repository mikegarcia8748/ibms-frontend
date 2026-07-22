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
    val viewPadding2 = 2.dp
    val viewPadding4 = 4.dp
    val viewPadding6 = 6.dp
    val viewPadding8 = 8.dp
    val viewPadding10 = 10.dp
    val viewPadding12 = 12.dp
    val viewPadding16 = 16.dp
    val viewPadding20 = 20.dp
    val viewPadding24 = 24.dp
    val viewPadding32 = 32.dp
    val viewPadding48 = 48.dp

    // Component heights
    val viewHeight1 = 1.dp
    val viewHeight2 = 2.dp
    val viewHeight4 = 4.dp
    val viewHeight6 = 6.dp
    val viewHeight24 = 24.dp
    val viewHeight40 = 40.dp
    val viewHeight48 = 48.dp
    val viewHeight56 = 56.dp
    val viewHeight64 = 64.dp
    val viewHeight80 = 80.dp
    val viewHeight200 = 200.dp
    val viewHeight360 = 360.dp
    val viewHeight480 = 480.dp

    // Window size class breakpoint (height)
    val viewHeight900 = 900.dp

    // Component widths
    val viewWidth24 = 24.dp
    val viewWidth56 = 56.dp
    val viewWidth96 = 96.dp
    val viewWidth160 = 160.dp
    val viewWidth180 = 180.dp
    val viewWidth280 = 280.dp
    val viewWidth420 = 420.dp
    val viewWidth560 = 560.dp
    val viewWidth600 = 600.dp
    val viewWidth840 = 840.dp
    val viewWidth1200 = 1200.dp

    // Component sizes (both width & height)
    val viewSize16 = 16.dp
    val viewSize18 = 18.dp
    val viewSize20 = 20.dp
    val viewSize24 = 24.dp
    val viewSize32 = 32.dp
    val viewSize36 = 36.dp
    val viewSize40 = 40.dp
    val viewSize56 = 56.dp
    val viewSize80 = 80.dp

    // Elevation
    val viewElevation0 = 0.dp
    val viewElevation1 = 1.dp
    val viewElevation2 = 2.dp

    // Corner radius
    val viewRadius6 = 6.dp
    val viewRadius8 = 8.dp
    val viewRadius12 = 12.dp
    val viewRadius16 = 16.dp
    val viewRadius24 = 24.dp

    // Stroke width
    val viewStroke1 = 1.dp
    val viewStroke2 = 2.dp

    // Dash pattern for the dashed "add" affordances
    val viewDashOn = 6.dp
    val viewDashOff = 4.dp
}
