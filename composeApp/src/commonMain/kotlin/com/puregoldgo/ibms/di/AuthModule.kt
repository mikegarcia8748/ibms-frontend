package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.data.repository.KtorAuthRepository
import com.puregoldgo.ibms.ui.screen.auth.LoginViewModel
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.domain.usecase.LoginUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the Authentication feature.
 */
val authModule = module {

    // region Repository
    factory<AuthRepository> { KtorAuthRepository(get()) }
    // endregion

    // region UseCases
    factoryOf(::LoginUseCase)
    // endregion

    // region ViewModels
    factoryOf(::LoginViewModel)
    // endregion
}
