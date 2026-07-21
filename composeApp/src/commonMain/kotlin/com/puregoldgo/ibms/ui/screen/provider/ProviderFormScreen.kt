package com.puregoldgo.ibms.ui.screen.provider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puregoldgo.ibms.ui.theme.Dimensions
import com.puregoldgo.ibms.shared.model.Provider
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen composable for creating/editing a Provider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderFormScreen(
    existingProvider: Provider? = null,
    onNavigateBack: () -> Unit,
    viewModel: ProviderFormViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(existingProvider) {
        if (existingProvider != null) {
            viewModel.initWithProvider(existingProvider)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ProviderFormUiEvent.SaveSuccess -> onNavigateBack()
                is ProviderFormUiEvent.ShowValidationError -> snackbarHostState.showSnackbar(event.message)
                is ProviderFormUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Provider" else "New Provider") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2190") // Left arrow unicode character
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ProviderFormContent(
            uiState = uiState,
            callback = ProviderFormCallback(
                onNameChange = viewModel::onNameChange,
                onPaymentScheduleDayChange = viewModel::onPaymentScheduleDayChange,
                onSave = viewModel::onSave,
            ),
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun ProviderFormContent(
    uiState: ProviderFormUIState,
    callback: ProviderFormCallback,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimensions.viewPadding16),
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = callback.onNameChange,
            label = { Text("Provider Name *") },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )

        Spacer(Modifier.height(Dimensions.viewPadding12))

        OutlinedTextField(
            value = uiState.paymentScheduleDay,
            onValueChange = callback.onPaymentScheduleDayChange,
            label = { Text("Payment Schedule Day *") },
            isError = uiState.paymentScheduleDayError != null,
            supportingText = {
                Text(uiState.paymentScheduleDayError ?: "Day of the month this ISP is paid on, 1–31")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Spacer(Modifier.height(Dimensions.viewPadding24))

        Button(
            onClick = callback.onSave,
            modifier = Modifier.fillMaxWidth().height(Dimensions.viewHeight48),
            enabled = !uiState.isSaving,
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(Dimensions.viewSize24),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(if (uiState.isEditMode) "Update Provider" else "Create Provider")
            }
        }
    }
}
