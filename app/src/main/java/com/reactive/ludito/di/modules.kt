package com.reactive.ludito.di

import com.reactive.ludito.ui.screens.main.MainViewModel
import com.reactive.ludito.ui.screens.tabs.map.MapViewModel
import com.reactive.ludito.ui.screens.tabs.map.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel() }
    viewModel { MapViewModel() }
    viewModel { SearchViewModel() }
}

val modulesList = listOf(viewModelModule)
