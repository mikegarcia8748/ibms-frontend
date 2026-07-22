package com.puregoldgo.ibms.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The icons this app draws, defined here rather than depended on.
 *
 * JetBrains stopped publishing `material-icons-core` and `-extended` after
 * Compose Multiplatform 1.7.3; nothing exists for the 1.11.x this project is on,
 * and pinning the 1.7.3 klib would mix compose-ui ABIs across every non-JVM
 * target. These are the standard Material 24dp paths, so they look exactly like
 * the icon library would have — there is simply no artifact left to take them
 * from.
 *
 * Fill is opaque black on purpose: `Icon` tints whatever it is given with the
 * current content colour, so a flat fill is what lets these follow the theme.
 *
 * Add to this file only what a screen actually uses. It is a working set, not a
 * library.
 */
object AppIcons {

    val Search: ImageVector by lazy {
        materialIcon(
            name = "Search",
            pathData = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 " +
                "9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 " +
                "4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 " +
                "9.5 11.99 14 9.5 14z",
        )
    }

    val Close: ImageVector by lazy {
        materialIcon(
            name = "Close",
            pathData = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 " +
                "13.41 17.59 19 19 17.59 13.41 12z",
        )
    }

    /** Sign out. */
    val Logout: ImageVector by lazy {
        materialIcon(
            name = "Logout",
            pathData = "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c" +
                "-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z",
        )
    }

    val CloudUpload: ImageVector by lazy {
        materialIcon(
            name = "CloudUpload",
            pathData = "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 " +
                "8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78" +
                "-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z",
        )
    }

    /** A credential — the temporary password being issued or re-issued. */
    val Key: ImageVector by lazy {
        materialIcon(
            name = "Key",
            pathData = "M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 " +
                "4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 " +
                "2 2-.9 2-2 2z",
        )
    }

    /** The staff directory. */
    val Group: ImageVector by lazy {
        materialIcon(
            name = "Group",
            pathData = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 " +
                "3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 " +
                "3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0" +
                "-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7" +
                "-3.5z",
        )
    }

    /** Configuration — the ISP providers panel. */
    val Tune: ImageVector by lazy {
        materialIcon(
            name = "Tune",
            pathData = "M3 17v2h6v-2H3zM3 5v2h10V5H3zm10 16v-2h8v-2h-8v-2h-2v6h2zM7 9v2H3v2h4v" +
                "2h2V9H7zm14 4v-2H11v2h10zm-6-4h2V7h4V5h-4V3h-2v6z",
        )
    }

    /** Provision an account. */
    val PersonAdd: ImageVector by lazy {
        materialIcon(
            name = "PersonAdd",
            pathData = "M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h" +
                "3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z",
        )
    }

    /** A row's own menu — reset password, deactivate. */
    val MoreVert: ImageVector by lazy {
        materialIcon(
            name = "MoreVert",
            pathData = "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 " +
                "2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z",
        )
    }

    /** Take the temporary password to the clipboard. */
    val ContentCopy: ImageVector by lazy {
        materialIcon(
            name = "ContentCopy",
            pathData = "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 " +
                "2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z",
        )
    }

    /** A store or branch. */
    val Domain: ImageVector by lazy {
        materialIcon(
            name = "Domain",
            pathData = "M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5" +
                "h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2" +
                "v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z",
        )
    }

    /** A spreadsheet or document — the master list being imported. */
    val Description: ImageVector by lazy {
        materialIcon(
            name = "Description",
            pathData = "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l" +
                "-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z",
        )
    }

    /** Read this before you upload. */
    val Warning: ImageVector by lazy {
        materialIcon(
            name = "Warning",
            pathData = "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z",
        )
    }

    /** Confirms a destructive-ish action — the import commit. */
    val CheckCircle: ImageVector by lazy {
        materialIcon(
            name = "CheckCircle",
            pathData = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 1" +
                "5l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z",
        )
    }

    val Delete: ImageVector by lazy {
        materialIcon(
            name = "Delete",
            pathData = "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2" +
                "h14V4z",
        )
    }

    /** A connection — an ISP account or the line a branch runs on. */
    val Wifi: ImageVector by lazy {
        materialIcon(
            name = "Wifi",
            pathData = "M1 9l2 2c4.97-4.97 13.03-4.97 18 0l2-2C16.93 2.93 7.08 2.93 1 9zm8 8l3 3 3" +
                "-3c-1.65-1.66-4.34-1.66-6 0zm-4-4l2 2c2.76-2.76 7.24-2.76 10 0l2-2C15.14 9.14 " +
                "8.87 9.14 5 13z",
        )
    }

    /** Take the current list away as a file. */
    val Download: ImageVector by lazy {
        materialIcon(
            name = "Download",
            pathData = "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z",
        )
    }

    /** Create a record — the `+ ADD` affordance on a list. */
    val Add: ImageVector by lazy {
        materialIcon(
            name = "Add",
            pathData = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
        )
    }

    /** Step to the previous month in the compile period picker. */
    val ChevronLeft: ImageVector by lazy {
        materialIcon(
            name = "ChevronLeft",
            pathData = "M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z",
        )
    }

    /** Step to the next month in the compile period picker. */
    val ChevronRight: ImageVector by lazy {
        materialIcon(
            name = "ChevronRight",
            pathData = "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z",
        )
    }

    /** Return from the RFP-entry step to the compilation review. */
    val ArrowBack: ImageVector by lazy {
        materialIcon(
            name = "ArrowBack",
            pathData = "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
        )
    }
}

/** Builds a standard 24dp Material icon from its path data. */
private fun materialIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = ICON_SIZE.dp,
        defaultHeight = ICON_SIZE.dp,
        viewportWidth = ICON_SIZE,
        viewportHeight = ICON_SIZE,
    ).addPath(
        pathData = addPathNodes(pathData),
        fill = SolidColor(Color.Black),
    ).build()

private const val ICON_SIZE = 24f
