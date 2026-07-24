package com.chb.lenstracker.di

import com.chb.lenstracker.data.db.LensTrackerDatabase
import com.chb.lenstracker.data.db.createDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val iosModule: Module = module {
    single<LensTrackerDatabase> { createDatabase() }
}

fun initKoin() {
    startKoin {
        modules(appModule, iosModule)
    }
}
