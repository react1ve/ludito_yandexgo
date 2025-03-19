package com.reactive.premier.di

import com.reactive.premier.utils.preferences.PremierSharedManager
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val premierSharedPrefModule = module {
    singleOf(::PremierSharedManager)
}


val premierModulesList =
    listOf(premierSharedPrefModule)


