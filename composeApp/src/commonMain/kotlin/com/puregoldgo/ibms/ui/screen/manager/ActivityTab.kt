package com.puregoldgo.ibms.ui.screen.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.puregoldgo.ibms.ui.component.AppIcons
import com.puregoldgo.ibms.ui.component.ChipTone
import com.puregoldgo.ibms.ui.component.SearchField
import com.puregoldgo.ibms.ui.component.SectionCard
import com.puregoldgo.ibms.ui.component.SectionEmptyState
import com.puregoldgo.ibms.ui.component.SectionErrorState
import com.puregoldgo.ibms.ui.component.SectionLoadingState
import com.puregoldgo.ibms.ui.component.StatusChip
import com.puregoldgo.ibms.ui.theme.Dimensions
import ibmsispbillingmanagementsystem.composeapp.generated.resources.Res
import ibmsispbillingmanagementsystem.composeapp.generated.resources.console_retry
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_activity_empty
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_activity_search_hint
import ibmsispbillingmanagementsystem.composeapp.generated.resources.manager_activity_title
import org.jetbrains.compose.resources.stringResource

/**
 * The audit trail — who did what, most recent first.
 *
 * Ordered as the server sent it rather than re-sorted here: `GET /activities` is
 * the record, and a client that reorders it is a second opinion on an audit log.
 */
@Composable
internal fun ActivityTab(
    uiState: ManagerUIState,
    callback: ManagerCallback,
    isCompact: Boolean,
) {
    val search = @Composable { modifier: Modifier ->
        SearchField(
            value = uiState.activityQuery,
            onValueChange = callback.onActivityQueryChange,
            placeholder = stringResource(Res.string.manager_activity_search_hint),
            modifier = modifier,
        )
    }

    SectionCard(
        title = stringResource(Res.string.manager_activity_title),
        icon = AppIcons.Group,
        trailing = if (isCompact) null else ({ search(Modifier.width(Dimensions.viewWidth280)) }),
    ) {
        if (isCompact) {
            search(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Dimensions.viewPadding16))
        }

        when {
            uiState.isLoading -> SectionLoadingState()

            uiState.loadError != null -> SectionErrorState(
                message = uiState.loadError,
                onRetry = callback.onRetryLoad,
                retryLabel = stringResource(Res.string.console_retry),
            )

            uiState.visibleActivities.isEmpty() ->
                SectionEmptyState(stringResource(Res.string.manager_activity_empty))

            else -> LazyColumn(
                modifier = Modifier.heightIn(max = Dimensions.viewHeight480),
                verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
            ) {
                items(uiState.visibleActivities, key = { it.id }) { activity ->
                    ActivityCard(activity, isCompact)
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: ActivityRow, isCompact: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.viewRadius12))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(Dimensions.viewPadding16),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
        ) {
            Text(
                text = activity.actorName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            StatusChip(label = activity.actorRole.uppercase(), tone = ChipTone.Neutral)

            if (!isCompact) {
                Text(
                    text = activity.occurredAt,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }

        Spacer(Modifier.height(Dimensions.viewPadding8))

        // The server's own phrasing, shown verbatim — restating it here would
        // mean two descriptions of the same event that can drift apart.
        Text(
            text = activity.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isCompact) {
            Spacer(Modifier.height(Dimensions.viewPadding4))
            Text(
                text = activity.occurredAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
