package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.data.repository.KtorAccountRepository
import com.puregoldgo.ibms.data.repository.KtorStoreRepository
import com.puregoldgo.ibms.data.repository.KtorUserRepository
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.domain.UserRepository
import com.puregoldgo.ibms.shared.domain.usecase.BulkImportAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetAccountsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetStoresUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetUsersUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ProvisionUserUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ResetUserPasswordUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserRoleUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateUserStatusUseCase
import com.puregoldgo.ibms.ui.screen.sysadmin.SysadminViewModel
import com.puregoldgo.ibms.ui.screen.sysadmin.directory.DirectoryViewModel
import com.puregoldgo.ibms.ui.screen.sysadmin.registry.RegistryViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the sysadmin control panel.
 *
 * `AuthRepository` (for logout) is declared by [authModule], and the shared
 * `HttpClient` and `GetProvidersUseCase` by [providerModule] — all three modules
 * land in the same container via [appModules].
 */
val sysadminModule = module {

    // region Repository
    factory<AccountRepository> { KtorAccountRepository(get()) }
    factory<StoreRepository> { KtorStoreRepository(get()) }
    factory<UserRepository> { KtorUserRepository(get()) }
    // endregion

    // region UseCases
    factoryOf(::BulkImportAccountsUseCase)
    factoryOf(::GetStoresUseCase)
    factoryOf(::GetAccountsUseCase)
    factoryOf(::GetUsersUseCase)
    factoryOf(::ProvisionUserUseCase)
    factoryOf(::ResetUserPasswordUseCase)
    factoryOf(::UpdateUserRoleUseCase)
    factoryOf(::UpdateUserStatusUseCase)
    // endregion

    // region ViewModels
    // One per responsibility: the shell, the staff directory, and the two
    // registries that share a fetch.
    factoryOf(::SysadminViewModel)
    factoryOf(::DirectoryViewModel)
    factoryOf(::RegistryViewModel)
    // endregion
}
