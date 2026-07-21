package com.puregoldgo.ibms.ui.screen.secretary

import androidx.compose.runtime.Composable
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.secretary_compile_title
import org.jetbrains.compose.resources.stringResource

/**
 * Placeholder.
 *
 * Compilation is the secretary's most consequential action — it writes a
 * month's billing for an entire ISP — and it has no design yet. Drawn as the
 * empty shell rather than guessed at: an invented picker wired to nothing would
 * be mistaken for a feature, and the wrong period compiled is not a UI bug.
 *
 * TODO: `POST /topsheets/preview` lists the accounts and totals for an ISP and
 *  period without persisting; `POST /topsheets/compile` commits it, and takes an
 *  `Idempotency-Key` header because a double-submit would bill twice.
 */
@Composable
internal fun CompileTopSheetTab() {
    SectionCard(
        title = stringResource(Res.string.secretary_compile_title),
        icon = AppIcons.Description,
    ) {
        SectionEmptyState(stringResource(Res.string.secretary_compile_empty))
    }
}
