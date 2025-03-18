package com.reactive.premier.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

open class PremierSharedManager(context: Context) {

    private fun providePreferences(context: Context, name: String = context.packageName) =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    val preferences: SharedPreferences = providePreferences(context)

    var latitude by preferences.double()
    var longitude by preferences.double()

    fun deleteAll() {
        preferences.edit { clear() }
    }
}
