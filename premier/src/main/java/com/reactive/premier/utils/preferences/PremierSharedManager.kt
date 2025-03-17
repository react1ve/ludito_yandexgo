package com.reactive.premier.utils.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

open class PremierSharedManager(context: Context) {

    private fun providePreferences(context: Context, name: String = context.packageName) =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    val preferences: SharedPreferences = providePreferences(context)

    var token by preferences.string()
    var latitude by preferences.double()
    var longitude by preferences.double()
    var push by preferences.string()
    var theme by preferences.int(defaultValue = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun deleteAll() {
        preferences.edit { clear() }
    }
}
