package com.reactive.ludito.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedLocationEntity::class], version = 1, exportSchema = false)
abstract class LocationsDatabase : RoomDatabase() {

    abstract fun savedLocationsDao(): SavedLocationsDao

    companion object {
        fun getDatabase(context: Context): LocationsDatabase {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                LocationsDatabase::class.java,
                "locations_database"
            )
                .fallbackToDestructiveMigration()
                .build()
            return instance
        }
    }
}