package com.puregoldgo.ibms.platform.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.aakira.napier.Napier

/**
 * Not implemented yet.
 *
 * The one screen that picks a file — the sysadmin control panel's master-list
 * import — is a browser tool today, so only the wasmJs actual is real. This
 * exists so `:composeApp` still builds for Android, and is where an
 * `ActivityResultContracts.GetContent` launcher goes when the panel ships on a
 * phone.
 */
@Composable
actual fun rememberFilePicker(
    accept: List<String>,
    onPicked: (PickedFile) -> Unit,
): FilePickerLauncher = remember {
    FilePickerLauncher {
        Napier.w(message = "File picking is not implemented on Android", tag = "FilePicker")
    }
}

/** Android has no external-app file drop; the drop zone is tap-only here. */
@Composable
actual fun Modifier.fileDropTarget(
    enabled: Boolean,
    onDragOver: (Boolean) -> Unit,
    onDropped: (PickedFile) -> Unit,
): Modifier = this
