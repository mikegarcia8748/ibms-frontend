package com.puregoldgo.ibms.platform.file

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.domDataTransferOrNull
import io.github.aakira.napier.Napier
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.w3c.dom.HTMLInputElement
import org.w3c.files.File
import org.w3c.files.FileReader

private const val TAG = "FilePicker"

/** Sent when the browser has no `type` for the file — the backend sniffs bytes anyway. */
private const val FALLBACK_MIME = "application/octet-stream"

/**
 * Drives a throwaway `<input type="file">`.
 *
 * The element is created per click and removed as soon as it has answered
 * rather than being kept around: a persistent hidden input holds a reference to
 * the last chosen file, and re-picking the *same* file then fires no `change`
 * event at all, which reads to the user as a dead button.
 */
@Composable
actual fun rememberFilePicker(
    accept: List<String>,
    onPicked: (PickedFile) -> Unit,
): FilePickerLauncher {
    val currentOnPicked by rememberUpdatedState(onPicked)
    val acceptAttribute = accept.joinToString(",")

    return remember(acceptAttribute) {
        FilePickerLauncher {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = acceptAttribute
            input.style.display = "none"

            input.onchange = {
                val file = input.files?.item(0)
                input.remove()
                if (file != null) {
                    file.readInto(currentOnPicked)
                }
            }

            // Cancelling never fires `change`, so the element would otherwise
            // linger in the DOM for the life of the page.
            input.oncancel = {
                input.remove()
            }

            document.body?.appendChild(input)
            input.click()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun Modifier.fileDropTarget(
    enabled: Boolean,
    onDragOver: (Boolean) -> Unit,
    onDropped: (PickedFile) -> Unit,
): Modifier {
    val currentOnDragOver by rememberUpdatedState(onDragOver)
    val currentOnDropped by rememberUpdatedState(onDropped)

    val target = remember {
        object : DragAndDropTarget {
            override fun onEntered(event: DragAndDropEvent) = currentOnDragOver(true)

            override fun onExited(event: DragAndDropEvent) = currentOnDragOver(false)

            override fun onEnded(event: DragAndDropEvent) = currentOnDragOver(false)

            override fun onDrop(event: DragAndDropEvent): Boolean {
                currentOnDragOver(false)
                // Only the first file: the import takes one spreadsheet, and
                // silently picking one of several would be worse than ignoring
                // the extras.
                val file = event.transferData?.domDataTransferOrNull?.files?.item(0)
                    ?: return false
                file.readInto(currentOnDropped)
                return true
            }
        }
    }

    return if (enabled) {
        dragAndDropTarget(shouldStartDragAndDrop = { true }, target = target)
    } else {
        this
    }
}

/** Reads the whole file and hands it over. Silently drops unreadable files. */
private fun File.readInto(onRead: (PickedFile) -> Unit) {
    val reader = FileReader()

    reader.onload = {
        val buffer = reader.result as? ArrayBuffer
        if (buffer == null) {
            Napier.e(message = "FileReader produced no ArrayBuffer for $name", tag = TAG)
        } else {
            onRead(
                PickedFile(
                    name = name,
                    mimeType = type.ifBlank { FALLBACK_MIME },
                    bytes = Int8Array(buffer).toByteArray(),
                ),
            )
        }
    }

    reader.onerror = {
        Napier.e(message = "Could not read $name", tag = TAG)
    }

    reader.readAsArrayBuffer(this)
}
