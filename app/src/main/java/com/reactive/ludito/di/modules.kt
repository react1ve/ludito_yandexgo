package com.reactive.ludito.di

import com.reactive.ludito.ui.screens.tabs.map.MapViewModel
import com.reactive.ludito.ui.screens.tabs.map.details.DetailsViewModel
import com.reactive.ludito.ui.screens.tabs.map.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MapViewModel() }
    viewModel { SearchViewModel() }
    viewModel { DetailsViewModel() }
}

val modulesList = listOf(viewModelModule)
