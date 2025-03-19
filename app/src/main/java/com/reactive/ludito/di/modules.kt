package com.reactive.ludito.di

import androidx.room.Room
import com.reactive.ludito.data.db.AppDatabase
import com.reactive.ludito.data.repository.LocationsRepository
import com.reactive.ludito.data.repository.LocationsRepositoryImpl
import com.reactive.ludito.ui.screens.tabs.map.MapViewModel
import com.reactive.ludito.ui.screens.tabs.map.details.DetailsViewModel
import com.reactive.ludito.ui.screens.tabs.map.search.SearchViewModel
import com.reactive.ludito.ui.screens.tabs.saved.SavedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "ludito_database"
        ).build()
    }

    single { get<AppDatabase>().locationDao() }
}

val repositoryModule = module {
    single<LocationsRepository> { LocationsRepositoryImpl(get()) }
}

val viewModelModule = module {
    viewModel { MapViewModel() }
    viewModel { SearchViewModel() }
    viewModel { DetailsViewModel(get()) }
    viewModel { SavedViewModel(get()) }
}

val modulesList = listOf(viewModelModule, databaseModule, repositoryModule)
