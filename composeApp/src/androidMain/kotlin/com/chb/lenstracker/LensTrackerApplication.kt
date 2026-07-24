package com.chb.lenstracker

import android.app.Application
import com.chb.lenstracker.di.androidModule
import com.chb.lenstracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LensTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@LensTrackerApplication)
            modules(appModule, androidModule)
        }
    }
}
