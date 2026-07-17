package com.puregoldgo.ibms.ui.screen.provider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.theme.Dimensions
import com.puregoldgo.ibms.shared.model.Provider
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen composable — handles events and navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(
    onNavigateToForm: (providerId: String?) -> Unit,
    viewModel: ProviderListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProviderListUiEvent.NavigateToForm -> onNavigateToForm(event.providerId)
                is ProviderListUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Providers") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onAddClick() },
                icon = { Text("+") },
                text = { Text("Add Provider") },
            )
        },
    ) { padding ->
        ProviderListContent(
            uiState = uiState,
            callback = ProviderListCallback(
                onProviderClick = { viewModel.onEditClick(it.id) },
                onRetry = { viewModel.loadProviders() },
            ),
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Pure UI content — no ViewModel dependency.
 */
@Composable
private fun ProviderListContent(
    uiState: ProviderListUIState,
    callback: ProviderListCallback,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.isEmpty -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No providers yet", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    Text(
                        "Tap + to add your first provider",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            uiState.errorMessage != null && uiState.providers.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(Dimensions.viewPadding16),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Dimensions.viewPadding8))
                    Text("Tap to retry", modifier = Modifier.clickable { callback.onRetry() })
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = Dimensions.viewPadding16),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.viewPadding8),
                ) {
                    item { Spacer(Modifier.height(Dimensions.viewPadding8)) }
                    items(uiState.providers, key = { it.id }) { provider ->
                        ProviderCard(
                            provider = provider,
                            onClick = { callback.onProviderClick(provider) },
                        )
                    }
                    item { Spacer(Modifier.height(Dimensions.viewHeight80)) }
                }
            }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.viewElevation2),
    ) {
        Column(modifier = Modifier.padding(Dimensions.viewPadding16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = provider.code,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (provider.contactEmail != null) {
                Spacer(Modifier.height(Dimensions.viewPadding4))
                Text(
                    text = provider.contactEmail!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (provider.contactPhone != null) {
                Text(
                    text = provider.contactPhone!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(Dimensions.viewPadding4))
            Text(
                text = if (provider.isActive) "Active" else "Inactive",
                style = MaterialTheme.typography.labelSmall,
                color = if (provider.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}
