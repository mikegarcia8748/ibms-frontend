package com.puregoldgo.ibms.di

import com.puregoldgo.ibms.data.repository.KtorAccountRepository
import com.puregoldgo.ibms.data.repository.KtorTopSheetRepository
import com.puregoldgo.ibms.shared.domain.AccountRepository
import com.puregoldgo.ibms.shared.domain.TopSheetRepository
import com.puregoldgo.ibms.shared.domain.usecase.AssignRfpUseCase
import com.puregoldgo.ibms.shared.domain.usecase.ConfirmTopSheetUseCase
import com.puregoldgo.ibms.shared.domain.usecase.CreateAccountUseCase
import com.puregoldgo.ibms.shared.domain.usecase.CreateTopSheetDraftUseCase
import com.puregoldgo.ibms.shared.domain.usecase.DeleteTopSheetLineUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetLinesUseCase
import com.puregoldgo.ibms.shared.domain.usecase.GetTopSheetsUseCase
import com.puregoldgo.ibms.shared.domain.usecase.PreviewTopSheetUseCase
import com.puregoldgo.ibms.shared.domain.usecase.UpdateTopSheetLineUseCase
import com.puregoldgo.ibms.ui.screen.secretary.SecretaryViewModel
import com.puregoldgo.ibms.ui.screen.secretary.TopSheetDetailViewModel
import com.puregoldgo.ibms.ui.screen.secretary.compile.CompileViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin DI module for the secretary console.
 *
 * The console shell renders sample data this pass, but the Compile TopSheet
 * panel is fully wired: it has its own [CompileViewModel] and the topsheet
 * repository + use cases below. The panel's browse view reuses the account, store
 * and provider use cases already bound by [sysadminModule] and [providerModule]
 * — all land in the same container via [appModules], so [CompileViewModel]
 * resolves them without re-declaration. `AuthRepository` (sign-out) comes from
 * [authModule]; the shared `HttpClient` from [providerModule].
 */
val secretaryModule = module {

    // region Repository
    factory<AccountRepository> { KtorAccountRepository(get()) }
    factory<TopSheetRepository> { KtorTopSheetRepository(get()) }
    // endregion

    // region UseCases
    factoryOf(::CreateAccountUseCase)
    factoryOf(::PreviewTopSheetUseCase)
    factoryOf(::CreateTopSheetDraftUseCase)
    factoryOf(::GetTopSheetsUseCase)
    factoryOf(::GetTopSheetLinesUseCase)
    factoryOf(::UpdateTopSheetLineUseCase)
    factoryOf(::AssignRfpUseCase)
    factoryOf(::DeleteTopSheetLineUseCase)
    factoryOf(::ConfirmTopSheetUseCase)
    // endregion

    // region ViewModels
    factoryOf(::SecretaryViewModel)
    factoryOf(::CompileViewModel)
    factoryOf(::TopSheetDetailViewModel)
    // endregion
}
