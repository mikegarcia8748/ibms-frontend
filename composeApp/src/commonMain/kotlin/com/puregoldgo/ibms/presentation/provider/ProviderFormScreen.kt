package com.puregoldgo.ibms.presentation.provider

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            onNameChange = viewModel::onNameChange,
            onCodeChange = viewModel::onCodeChange,
            onEmailChange = viewModel::onEmailChange,
            onPhoneChange = viewModel::onPhoneChange,
            onSave = viewModel::onSave,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun ProviderFormContent(
    uiState: ProviderFormUIState,
    onNameChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Provider Name *") },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.code,
            onValueChange = onCodeChange,
            label = { Text("Provider Code *") },
            isError = uiState.codeError != null,
            supportingText = uiState.codeError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
            ),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.contactEmail,
            onValueChange = onEmailChange,
            label = { Text("Contact Email") },
            isError = uiState.emailError != null,
            supportingText = uiState.emailError?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.contactPhone,
            onValueChange = onPhoneChange,
            label = { Text("Contact Phone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = !uiState.isSaving,
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(if (uiState.isEditMode) "Update Provider" else "Create Provider")
            }
        }
    }
}
