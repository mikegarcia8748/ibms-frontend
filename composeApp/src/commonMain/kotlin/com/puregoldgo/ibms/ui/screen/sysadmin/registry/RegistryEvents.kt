package com.puregoldgo.ibms.ui.screen.sysadmin.registry

/** One-shot signals from the registry panels. */
sealed class RegistryUiEvent {

    /** A message to show briefly, usually a validation or server error. */
    data class ShowSnackbar(val message: String) : RegistryUiEvent()
}
