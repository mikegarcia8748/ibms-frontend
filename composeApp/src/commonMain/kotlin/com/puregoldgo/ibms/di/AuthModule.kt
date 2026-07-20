package com.puregoldgo.ibms.di

import com.puregoldgo.core.network.createAuthHttpClient
import com.puregoldgo.ibms.data.auth.ChallengeHolder
import com.puregoldgo.ibms.data.repository.KtorAuthRepository
import com.puregoldgo.ibms.shared.domain.AuthRepository
import com.puregoldgo.ibms.shared.domain.usecase.CompletePasswordChangeUseCase
import com.puregoldgo.ibms.shared.domain.usecase.LoginUseCase
import com.puregoldgo.ibms.shared.domain.usecase.RefreshSessionUseCase
import com.puregoldgo.ibms.ui.screen.access.NoAccessViewModel
import com.puregoldgo.ibms.ui.screen.auth.LoginViewModel
import com.puregoldgo.ibms.ui.screen.auth.SetPasswordViewModel
import com.puregoldgo.ibms.ui.screen.splash.SplashViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifier for the client the auth endpoints use — no session bearer attached. */
val AuthHttpClient = named("authHttpClient")

/**
 * Koin DI module for the Authentication feature.
 */
val authModule = module {

    // region Infrastructure
    // A client of its own, deliberately without the Auth plugin: the password
    // change carries a challenge token in its own header, which the session
    // provider must neither replace nor try to refresh.
    single(AuthHttpClient) { createAuthHttpClient() }

    // Single, not factory: the challenge outlives the sign-in ViewModel that
    // received it and must be readable by the set-password screen that redeems it.
    single { ChallengeHolder() }
    // endregion

    // region Repository
    factory<AuthRepository> { KtorAuthRepository(get(AuthHttpClient)) }
    // endregion

    // region UseCases
    factoryOf(::LoginUseCase)
    factoryOf(::CompletePasswordChangeUseCase)
    factoryOf(::RefreshSessionUseCase)
    // endregion

    // region ViewModels
    factoryOf(::LoginViewModel)
    factoryOf(::SetPasswordViewModel)
    factoryOf(::SplashViewModel)
    factoryOf(::NoAccessViewModel)
    // endregion
}
