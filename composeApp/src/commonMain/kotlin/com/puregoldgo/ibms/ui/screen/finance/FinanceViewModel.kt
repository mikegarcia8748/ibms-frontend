package com.puregoldgo.ibms.ui.screen.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puregoldgo.core.storage.CurrentUserStore
import com.puregoldgo.ibms.shared.domain.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the finance console.
 *
 * **Renders sample data.** The screen is a UI pass: no repositories, no HTTP.
 * Filtering already lives in [FinanceUIState] rather than in the composables, so
 * the rules are testable now and the previews can show a filtered queue without
 * running a ViewModel — and swapping [FinanceSampleData] for the API moves
 * nothing in the composables. The staging both other consoles went through.
 *
 * TODO: the wiring pass replaces [loadPanel] with concurrent calls to
 *  `GET /topsheets` and `GET /providers`, awaited together the way
 *  `SysadminViewModel.loadPanel` does — the payment schedule is a join of the
 *  two, so a panel with only one list in would draw rows that are quietly wrong.
 *  [onActionConfirm] then calls `POST /topsheets/{id}/approve` or
 *  `POST /topsheets/{id}/pay`, and [onExportClick] downloads
 *  `GET /exports/topsheet/{id}.xlsx`.
 */
class FinanceViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        FinanceUIState(
            userName = CurrentUserStore.name.orEmpty(),
            userRole = CurrentUserStore.role.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<FinanceUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    init {
        loadPanel()
    }

    // region Public API

    /** Seeds the console. Synchronous today; a network call once wired. */
    fun loadPanel() {
        _uiState.update {
            it.copy(
                isLoading = false,
                loadError = null,
                providers = FinanceSampleData.providers,
                topSheets = FinanceSampleData.topSheets,
            )
        }
    }

    fun onTabSelect(tab: FinanceTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // endregion

    // region Approvals

    fun onApprovalQueryChange(query: String) {
        _uiState.update { it.copy(approvalQuery = query) }
    }

    fun onApprovalProviderSelect(providerId: String?) {
        _uiState.update { it.copy(approvalProviderId = providerId) }
    }

    fun onApproveClick(sheet: FinanceTopSheetRow) = ask(sheet, TopSheetAction.Kind.Approve)

    // endregion

    // region Payments

    fun onPaymentQueryChange(query: String) {
        _uiState.update { it.copy(paymentQuery = query) }
    }

    fun onPaymentProviderSelect(providerId: String?) {
        _uiState.update { it.copy(paymentProviderId = providerId) }
    }

    fun onPayClick(sheet: FinanceTopSheetRow) = ask(sheet, TopSheetAction.Kind.Pay)

    /** TODO: `GET /exports/topsheet/{id}.xlsx`, then hand the bytes to the platform. */
    fun onExportClick(sheet: FinanceTopSheetRow) = Unit

    // endregion

    // region Confirmation

    /** TODO: `POST /topsheets/{id}/approve` or `/pay`. Inert until then — see the dialog's banner. */
    fun onActionConfirm() = Unit

    fun onActionDismiss() {
        _uiState.update { it.copy(pendingAction = null, actionError = null) }
    }

    // endregion

    /** Drops the stored session. There is no logout endpoint to call. */
    fun onLogout() {
        authRepository.signOut()
        viewModelScope.launch {
            _uiEvent.emit(FinanceUiEvent.NavigateToLogin)
        }
    }

    // region Internals

    /**
     * Opens the confirmation on a clean slate.
     *
     * Both transitions release money and neither has a way back, so they are
     * asked for rather than fired off the row. A stale rejection from a previous
     * attempt is cleared with it — it belongs to a sheet that is no longer the
     * one on screen.
     */
    private fun ask(sheet: FinanceTopSheetRow, kind: TopSheetAction.Kind) {
        _uiState.update {
            it.copy(
                pendingAction = TopSheetAction(sheet = sheet, kind = kind),
                actionError = null,
                isSubmitting = false,
            )
        }
    }

    // endregion
}
