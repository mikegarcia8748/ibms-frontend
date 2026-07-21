package com.puregoldgo.ibms.ui.screen.finance

/** One-shot navigation signals from the finance console. */
sealed class FinanceUiEvent {

    /** The session was dropped locally — the caller returns to sign-in. */
    data object NavigateToLogin : FinanceUiEvent()
}
