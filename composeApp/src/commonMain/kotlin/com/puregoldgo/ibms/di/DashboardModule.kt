package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.data.repository.KtorAccountRepository
import com.puregoldgo.ibms.data.repository.KtorStoreRepository
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.domain.usecase.BulkImportAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.ui.screen.dashboard.DashboardViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the sysadmin control panel.
 *
 * `AuthRepository` (for logout) is declared by [authModule], and the shared
 * `HttpClient` and `GetProvidersUseCase` by [providerModule] — all three modules
 * land in the same container via [appModules].
 */
val dashboardModule = module {

    // region Repository
    factory<AccountRepository> { KtorAccountRepository(get()) }
    factory<StoreRepository> { KtorStoreRepository(get()) }
    // endregion

    // region UseCases
    factoryOf(::BulkImportAccountsUseCase)
    factoryOf(::GetStoresUseCase)
    factoryOf(::GetAccountsUseCase)
    // endregion

    // region ViewModels
    factoryOf(::DashboardViewModel)
    // endregion
}
