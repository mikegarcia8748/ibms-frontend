package com.puregoldgo.ibms.ui.screen.sysadmin

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
 * The console shell: who is signed in, which panel is open, and the way out.
 *
 * It holds no data and calls no repository but [AuthRepository]. Each panel has
 * a ViewModel of its own — `DirectoryViewModel`, `RegistryViewModel` — so the
 * one that fetches users can be tested without standing up the one that fetches
 * accounts, which is what this split was for.
 */
class SysadminViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // region State

    private val _uiState = MutableStateFlow(
        SysadminUIState(
            userName = CurrentUserStore.name.orEmpty(),
            userRole = CurrentUserStore.role.orEmpty(),
        ),
    )
    val uiState = _uiState.asStateFlow()

    // endregion

    // region Events

    private val _uiEvent = MutableSharedFlow<SysadminUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // endregion

    fun onTabSelect(tab: SysadminTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Drops the stored session.
     *
     * A temporary password may be on screen in the directory panel, and it must
     * not outlive the admin who was shown it. This ViewModel can no longer reach
     * into that state to clear it — the navigation this event triggers does, by
     * tearing the whole screen down; see `DirectoryViewModel.onCleared`. There
     * is no logout endpoint to call.
     */
    fun onLogout() {
        authRepository.signOut()
        viewModelScope.launch {
            _uiEvent.emit(SysadminUiEvent.NavigateToLogin)
        }
    }
}
