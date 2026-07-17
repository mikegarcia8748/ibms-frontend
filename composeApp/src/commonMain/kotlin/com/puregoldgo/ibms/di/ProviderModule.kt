package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.data.api.createHttpClient
import com.puregoldgo.ibms.data.repository.KtorProviderRepository
import com.puregoldgo.ibms.presentation.provider.ProviderFormViewModel
import com.puregoldgo.ibms.presentation.provider.ProviderListViewModel
import com.puregoldgo.ibms.shared.domain.ProviderRepository
import com.puregoldgo.ibms.shared.domain.usecase.CreateProviderUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetProvidersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ProviderUseCases
import com.puregoldgo.ibms.shared.domain.usecase.UpdateProviderUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the Provider feature.
 */
val providerModule = module {

    // region Infrastructure
    single { createHttpClient() }
    // endregion

    // region Repository
    factory<ProviderRepository> { KtorProviderRepository(get()) }
    // endregion

    // region UseCases
    factoryOf(::GetProvidersUseCase)
    factoryOf(::CreateProviderUseCase)
    factoryOf(::UpdateProviderUseCase)
    factory {
        ProviderUseCases(
            getProviders = get(),
            createProvider = get(),
            updateProvider = get(),
        )
    }
    // endregion

    // region ViewModels
    factoryOf(::ProviderListViewModel)
    factoryOf(::ProviderFormViewModel)
    // endregion
}
