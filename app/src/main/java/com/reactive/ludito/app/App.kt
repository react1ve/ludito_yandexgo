package com.reactive.ludito.app

import android.app.Application
import com.reactive.ludito.di.modulesList
import com.reactive.premier.utils.PremierConstants.Keys.mapsKey
import com.yandex.mapkit.MapKitFactory
import org.koin.core.context.loadKoinModules

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initYandex()
        initKoin()
    }

    private fun initKoin() {
        loadKoinModules(modulesList)
    }

    private fun initYandex() {
        MapKitFactory.setApiKey(mapsKey)
    }
}
