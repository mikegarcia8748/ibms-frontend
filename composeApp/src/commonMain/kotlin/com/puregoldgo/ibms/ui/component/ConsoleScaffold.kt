package com.puregoldgo.ibms.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import com.puregoldgo.ibms.ui.adaptive.WindowSizeClass
import com.puregoldgo.ibms.ui.adaptive.WindowWidthSizeClass
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_brand
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_brand_suffix
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_logout
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_logout_content_description
import ibmsispbillingmanagementsystem.composeapp.generated.resources.img_puregold_logo
import ibmsispbillingmanagementsystem.composeapp.generated.resources.login_logo_content_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The shell every role console is drawn in: the dark identity bar, and a
 * centred, capped, scrolling body underneath it.
 *
 * The sysadmin and secretary consoles each carried their own copy of this, which
 * was the right call at two — the comment on the second one said so. At four it
 * stops paying: the copies had already drifted apart in the width they measured
 * their tab row against, and a fifth console would inherit whichever copy it was
 * pasted from.
 *
 * Dialogs are *not* a slot here. [AppDialog] draws into a platform dialog
 * window, so where a console calls it in the tree has no bearing on where it
 * appears — it belongs inside [content], next to the state that opens it.
 */
@Composable
fun ConsoleScaffold(
    userName: String,
    userRole: String,
    onLogoutClick: () -> Unit,
    appBarActions: @Composable RowScope.() -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable ColumnScope.(ConsoleLayout) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(hostState = snackbarHostState)
            }
        },
        topBar = {
            ConsoleAppBar(
                userName = userName,
                userRole = userRole,
                onLogoutClick = onLogoutClick,
                actions = appBarActions,
            )
        },
        snackbarHost = { snackbarHostState?.let { SnackbarHost(it) } },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            val windowSizeClass = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
            val layout = ConsoleLayout(
                windowSizeClass = windowSizeClass,
                isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact,
                isTabRowCompact = maxWidth < Dimensions.viewWidth1200,
            )
            val contentPadding = if (layout.isCompact) {
                Dimensions.viewPadding16
            } else {
                Dimensions.viewPadding32
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    // A console stretched across an ultrawide browser is
                    // unreadable; cap it and centre the column instead.
                    modifier = Modifier.widthIn(max = Dimensions.viewWidth1200),
                ) {
                    Spacer(Modifier.height(Dimensions.viewPadding32))
                    content(layout)
                    Spacer(Modifier.height(Dimensions.viewPadding48))
                }
            }
        }
    }
}

/**
 * What a console body needs to reflow itself, measured once by [ConsoleScaffold].
 *
 * [windowSizeClass] is the full three-bucket classification, there for consoles
 * that want to tell Medium (tablet) apart from Expanded (desktop). The two
 * booleans are the questions the current consoles actually ask, kept because they
 * answer different things and short-circuit the common cases.
 * [isCompact] is "is this a phone" (the Compact width bucket) — it decides
 * stacking and padding. [isTabRowCompact] is "do the tab labels still fit", which
 * goes wrong well before that: six labels only fit at the full capped width, so a
 * tab row on a half-width desktop window has to scroll while the body around it is
 * still laid out wide — a question the size-class buckets can't express.
 */
@Immutable
data class ConsoleLayout(
    val windowSizeClass: WindowSizeClass,
    val isCompact: Boolean,
    val isTabRowCompact: Boolean,
)

/**
 * The dark bar across the top: brand, who is signed in, and the way out.
 *
 * `inverseSurface` rather than a hand-picked near-black: it is the scheme's own
 * answer to "a dark surface in a light app", so it stays coherent with the rest
 * of the theme and inverts correctly if a dark scheme is ever switched on.
 *
 * [actions] sits before the sign-out button, for the console-specific actions a
 * given role earns up here. Sign-out is not a slot — every console has it, in
 * the same place, and it is the last thing that should be paste-able wrongly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleAppBar(
    userName: String,
    userRole: String,
    onLogoutClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) = BoxWithConstraints {
    // The bar sits in the Scaffold's topBar, outside the body's own
    // measurement, so it has to ask about width itself. Measured out here
    // rather than inside a slot because both the title and the actions need
    // the answer. Classified through the same breakpoints as the body so the
    // two never disagree about what "compact" means.
    val isCompact = WindowSizeClass
        .calculateFromSize(DpSize(maxWidth, maxHeight))
        .widthSizeClass == WindowWidthSizeClass.Compact

    TopAppBar(
        // `TopAppBar` defaults to the surface colours; the inverse pair is what
        // makes this the dark bar the rest of the theme expects.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            actionIconContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
        actions = {
            actions()

            val logoutDescription =
                stringResource(Res.string.console_logout_content_description)

            if (isCompact) {
                // Icon only — the label does not fit, and the content
                // description keeps it named for a screen reader.
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = AppIcons.Logout,
                        contentDescription = logoutDescription,
                        modifier = Modifier.size(Dimensions.viewSize20),
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            } else {
                TextButton(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ),
                ) {
                    Icon(
                        imageVector = AppIcons.Logout,
                        contentDescription = logoutDescription,
                        modifier = Modifier.size(Dimensions.viewSize18),
                    )
                    Spacer(Modifier.width(Dimensions.viewPadding8))
                    Text(stringResource(Res.string.console_logout))
                }
            }
        },
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.viewHeight64),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding12),
            ) {
                Image(
                    painter = painterResource(Res.drawable.img_puregold_logo),
                    contentDescription = stringResource(Res.string.login_logo_content_description),
                    modifier = Modifier.size(Dimensions.viewSize32),
                )

                Text(
                    text = stringResource(Res.string.console_brand),
                    style = MaterialTheme.typography.titleMedium,
                )

                // First thing to go when the bar is tight: it is decoration, and
                // dropping it keeps the identity and the sign-out both readable.
                if (!isCompact) {
                    Text(
                        text = stringResource(Res.string.console_brand_suffix),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = ALPHA_MUTED),
                    )
                }

                if (userName.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .width(Dimensions.viewStroke1)
                            .height(Dimensions.viewHeight24)
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface
                                    .copy(alpha = ALPHA_DIVIDER),
                            ),
                    )

                    Column(
                        // Yields space to the sign-out button rather than
                        // pushing it off the edge; a long name ellipsizes.
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = userRole.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                                .copy(alpha = ALPHA_MUTED),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
    )
}

/**
 * A console's title, its supporting line, and at most one trailing action.
 *
 * [action] takes the [Modifier] it must apply rather than being a bare slot:
 * when the header stacks it has to fill the width, and a caller that forgot
 * would leave a button hugging the left edge of a phone screen.
 */
@Composable
fun ConsoleHeader(
    title: String,
    subtitle: String,
    isCompact: Boolean,
    action: (@Composable (Modifier) -> Unit)? = null,
) {
    @Composable
    fun titleBlock() {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(Dimensions.viewPadding4))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (action == null) {
        titleBlock()
        return
    }

    if (isCompact) {
        Column(modifier = Modifier.fillMaxWidth()) {
            titleBlock()
            Spacer(Modifier.height(Dimensions.viewPadding16))
            action(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            titleBlock()
            action(Modifier)
        }
    }
}

/** Opacity for de-emphasised text and hairlines on the dark bar. */
private const val ALPHA_MUTED = 0.7f
private const val ALPHA_DIVIDER = 0.3f
