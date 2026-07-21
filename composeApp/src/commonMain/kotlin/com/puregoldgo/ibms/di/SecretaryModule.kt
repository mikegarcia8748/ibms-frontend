package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.ui.screen.secretary.SecretaryViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the secretary console.
 *
 * Only the ViewModel: the console renders sample data this pass, and its one
 * real dependency — `AuthRepository`, for sign-out — is declared by [authModule].
 * The repositories and use cases arrive with the wiring pass.
 */
val secretaryModule = module {

    // region ViewModels
    factoryOf(::SecretaryViewModel)
    // endregion
}
