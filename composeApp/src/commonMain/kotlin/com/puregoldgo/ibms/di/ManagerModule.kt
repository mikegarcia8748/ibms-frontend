package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.ui.screen.manager.ManagerViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the oversight console.
 *
 * Only the ViewModel: the console renders sample data this pass, and its one
 * real dependency — `AuthRepository`, for sign-out — is declared by [authModule].
 * The repositories arrive with the wiring pass.
 */
val managerModule = module {

    // region ViewModels
    factoryOf(::ManagerViewModel)
    // endregion
}
