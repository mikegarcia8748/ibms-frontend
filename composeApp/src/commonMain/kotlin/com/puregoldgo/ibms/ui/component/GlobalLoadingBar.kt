package com.puregoldgo.ibms.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.core.network.NetworkActivity
import kotlinx.coroutines.delay

/** How long a request must stay in flight before the bar appears. */
private const val SHOW_DELAY_MS = 150L

/**
 * A thin, indeterminate progress bar that shows whenever any HTTP request is in
 * flight, drawn across the top of the app.
 *
 * Ambient rather than blocking — it complements the per-section spinners without
 * intercepting input. The [SHOW_DELAY_MS] gate keeps it from flashing for calls
 * that come straight back: a bar that appears and vanishes inside a frame reads
 * as a glitch, not as progress.
 */
@Composable
fun GlobalLoadingBar(modifier: Modifier = Modifier) {
    val inFlight by NetworkActivity.inFlight.collectAsStateWithLifecycle()
    val busy = inFlight > 0

    // Delay only the *appearance*; the hide is immediate (the effect is cancelled
    // the moment work stops) so the bar never lingers past what it reports on.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(busy) {
        if (busy) {
            delay(SHOW_DELAY_MS)
            visible = true
        } else {
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}
