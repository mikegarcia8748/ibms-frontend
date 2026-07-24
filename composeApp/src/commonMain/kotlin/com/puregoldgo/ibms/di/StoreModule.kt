package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.data.repository.KtorStoreRepository
import com.puregoldgo.ibms.shared.domain.StoreRepository
import com.puregoldgo.ibms.shared.domain.usecase.CreateStoreUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for Store/Branch operations.
 *
 * Bound once here so every console that can create a store — secretary,
 * sysadmin and manager — resolves the same repository and use case.
 */
val storeModule = module {

    // region Repository
    factory<StoreRepository> { KtorStoreRepository(get()) }
    // endregion

    // region UseCases
    factoryOf(::CreateStoreUseCase)
    // endregion
}
