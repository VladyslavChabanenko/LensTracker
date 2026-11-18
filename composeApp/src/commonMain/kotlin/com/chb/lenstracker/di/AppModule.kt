package com.chb.lenstracker.di

import com.chb.lenstracker.data.db.LensCycleDao
import com.chb.lenstracker.data.db.LensTrackerDatabase
import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.data.repository.LensCycleRepositoryImpl
import com.chb.lenstracker.ui.screen.HomeViewModel
import com.chb.lenstracker.ui.screen.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule: Module = module {
    single<LensCycleDao> { get<LensTrackerDatabase>().lensCycleDao() }
    single<LensCycleRepository> { LensCycleRepositoryImpl(get(), get()) }
    viewModelOf(::HomeViewModel)
    // Explicit rather than viewModelOf: the trailing message resolver has a production default.
    viewModel { SettingsViewModel(get(), get()) }
}
