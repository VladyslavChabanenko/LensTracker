package com.chb.lenstracker.di

import android.content.Context
import com.chb.lenstracker.data.db.LensTrackerDatabase
import com.chb.lenstracker.data.db.createDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val androidModule: Module = module {
    single<LensTrackerDatabase> { createDatabase(get<Context>()) }
}
