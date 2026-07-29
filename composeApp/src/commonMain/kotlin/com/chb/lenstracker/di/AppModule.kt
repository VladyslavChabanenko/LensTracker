package com.chb.lenstracker.di

import com.chb.lenstracker.data.db.LensCycleDao
import com.chb.lenstracker.data.db.LensTrackerDatabase
import com.chb.lenstracker.data.repository.LensCycleRepository
import com.chb.lenstracker.data.repository.LensCycleRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single<LensCycleDao> { get<LensTrackerDatabase>().lensCycleDao() }
    single<LensCycleRepository> { LensCycleRepositoryImpl(get()) }
}
