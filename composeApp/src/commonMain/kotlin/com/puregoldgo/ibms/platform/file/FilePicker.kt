package com.puregoldgo.ibms.platform.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * A file the user chose, already read into memory.
 *
 * Bytes rather than a handle: there is no file type common to wasmJs, Android
 * and iOS, and the upload needs the whole thing anyway. Deliberately *not* a
 * `data class` and never stored in Compose state — a `ByteArray` compares by
 * identity, so a state holder carrying one would report a change on every
 * recomposition.
 */
@Stable
class PickedFile(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
) {
    val size: Long get() = bytes.size.toLong()
}

/** Opens the platform's file chooser. Held across recomposition. */
@Stable
fun interface FilePickerLauncher {
    fun launch()
}

/**
 * Remembers a launcher for the platform file chooser.
 *
 * [accept] filters what the chooser offers, in the form the web `accept`
 * attribute takes — extensions (`".xlsx"`) or MIME types. Platforms that only
 * understand one of the two translate as best they can; the filter is a
 * convenience, never a guarantee, so callers must still validate what comes
 * back.
 *
 * [onPicked] fires only on a real selection. Cancelling is silent — there is
 * nothing for the caller to undo.
 */
@Composable
expect fun rememberFilePicker(
    accept: List<String>,
    onPicked: (PickedFile) -> Unit,
): FilePickerLauncher

/**
 * Accepts files dragged onto this node from outside the app.
 *
 * Only the browser has such a thing, so this is a no-op everywhere else and any
 * drop zone using it must also be clickable. [onDragOver] drives the hover
 * affordance; it is never called on platforms that cannot drop.
 */
@Composable
expect fun Modifier.fileDropTarget(
    enabled: Boolean,
    onDragOver: (Boolean) -> Unit,
    onDropped: (PickedFile) -> Unit,
): Modifier
