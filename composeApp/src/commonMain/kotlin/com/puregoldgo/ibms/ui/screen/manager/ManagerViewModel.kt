package com.puregoldgo.ibms.ui.screen.manager

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
 * MVI ViewModel for the oversight console.
 *
 * **Renders sample data.** The screen is a UI pass: no repositories, no HTTP.
 * The period roll-up lives in [ManagerUIState] rather than in the composables,
 * so the arithmetic is testable now — it is the one thing on this screen that
 * can be wrong without looking wrong.
 *
 * TODO: the wiring pass replaces [loadPanel] with concurrent calls to
 *  `GET /topsheets` and `GET /activities`, awaited together the way
 *  `SysadminViewModel.loadPanel` does. Both are readable by any role, which is
 *  the whole of this console's access — there is nothing here to write.
 */
class ManagerViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        ManagerUIState(
            userName = CurrentUserStore.name.orEmpty(),
            userRole = CurrentUserStore.role.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<ManagerUiEvent>()
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
                topSheets = ManagerSampleData.topSheets,
                activities = ManagerSampleData.activities,
            )
        }
    }

    fun onTabSelect(tab: ManagerTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onTopSheetQueryChange(query: String) {
        _uiState.update { it.copy(topSheetQuery = query) }
    }

    fun onActivityQueryChange(query: String) {
        _uiState.update { it.copy(activityQuery = query) }
    }

    // endregion

    /** Drops the stored session. There is no logout endpoint to call. */
    fun onLogout() {
        authRepository.signOut()
        viewModelScope.launch {
            _uiEvent.emit(ManagerUiEvent.NavigateToLogin)
        }
    }
}
