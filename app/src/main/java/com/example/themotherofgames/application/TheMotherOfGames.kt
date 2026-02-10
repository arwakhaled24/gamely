package com.example.themotherofgames.application

import android.app.Application
import com.example.themotherofgames.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class TheMotherOfGames: Application() {
    override fun onCreate() {
        super.onCreate()
        GlobalContext.startKoin {
            androidContext(this@TheMotherOfGames)
            modules(appModule)
        }
    }
}