package com.reactive.premier.di

import com.reactive.premier.base.BasePremierViewModel
import com.reactive.premier.utils.preferences.PremierSharedManager
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val premierViewModelModule = module {
    viewModelOf(::BasePremierViewModel)
}

val premierSharedPrefModule = module {
    singleOf(::PremierSharedManager)
}


val premierModulesList =
    listOf(premierSharedPrefModule)


