package com.reactive.ludito.di

import com.reactive.ludito.data.repository.LocationsRepository
import com.reactive.ludito.data.room.LocationsDatabase
import com.reactive.ludito.ui.screens.tabs.map.MapViewModel
import com.reactive.ludito.ui.screens.tabs.map.details.DetailsViewModel
import com.reactive.ludito.ui.screens.tabs.map.search.SearchViewModel
import com.reactive.ludito.ui.screens.tabs.saved.SavedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { LocationsDatabase.getDatabase(androidContext()) }
    single { LocationsRepository(get()) }
}

val viewModelModule = module {
    viewModel { MapViewModel() }
    viewModel { SearchViewModel() }
    viewModel { DetailsViewModel(get()) }
    viewModel { SavedViewModel(get()) }
}

val modulesList = listOf(databaseModule, viewModelModule)