package com.puregoldgo.ibms.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.puregoldgo.ibms.ui.theme.Dimensions

/**
 * The three opinionated width buckets from the Material 3 window size classes:
 * <https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes>.
 *
 * [Compact] is a phone in portrait, [Medium] a large phone landscape or a small
 * tablet, [Expanded] a tablet or a desktop/web window.
 */
enum class WindowWidthSizeClass { Compact, Medium, Expanded }

/** The three height buckets, same source as [WindowWidthSizeClass]. */
enum class WindowHeightSizeClass { Compact, Medium, Expanded }

/**
 * Which size bucket the current window falls into, on each axis.
 *
 * This is a hand-rolled port of the Material 3 `WindowSizeClass`: the official
 * `calculateWindowSizeClass()` is not available in `commonMain` under Compose
 * Multiplatform (it is platform-only), and every one of our targets already has
 * a measured size to hand via [BoxWithConstraints]. So rather than pull in the
 * dependency per platform, we classify a measured [DpSize] here, against the same
 * breakpoints the guide publishes, kept in [Dimensions].
 *
 * Read it with [currentWindowSizeClass] anywhere under [ProvideWindowSizeClass].
 */
@Immutable
data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass,
) {
    companion object {
        /** The single source of breakpoint truth; everything else routes here. */
        fun calculateFromSize(size: DpSize): WindowSizeClass = WindowSizeClass(
            widthSizeClass = when {
                size.width < Dimensions.viewWidth600 -> WindowWidthSizeClass.Compact
                size.width < Dimensions.viewWidth840 -> WindowWidthSizeClass.Medium
                else -> WindowWidthSizeClass.Expanded
            },
            heightSizeClass = when {
                size.height < Dimensions.viewHeight480 -> WindowHeightSizeClass.Compact
                size.height < Dimensions.viewHeight900 -> WindowHeightSizeClass.Medium
                else -> WindowHeightSizeClass.Expanded
            },
        )
    }
}

/**
 * The window size class in scope, supplied by [ProvideWindowSizeClass].
 *
 * The default is the smallest bucket rather than an error: a subtree drawn
 * without the provider (an isolated `@Preview`, say) then degrades to the compact
 * layout instead of crashing. Real app content always sits under the provider.
 */
val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass.calculateFromSize(DpSize(0.dp, 0.dp))
}

/**
 * Measures the space it is given and publishes the matching [WindowSizeClass] to
 * its subtree. Wrap the app once near the root; screens then read the class with
 * [currentWindowSizeClass] instead of each measuring width for themselves.
 */
@Composable
fun ProvideWindowSizeClass(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
        CompositionLocalProvider(LocalWindowSizeClass provides sizeClass) {
            content()
        }
    }
}

/** The [WindowSizeClass] in scope. Shorthand for [LocalWindowSizeClass]. */
val currentWindowSizeClass: WindowSizeClass
    @Composable
    @ReadOnlyComposable
    get() = LocalWindowSizeClass.current
