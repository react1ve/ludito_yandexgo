package com.reactive.premier.utils.preferences

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun SharedPreferences.stringNullable(
    defaultValue: String? = null,
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, String?> = object : ReadWriteProperty<Any, String?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): String? =
        getString(key(property), defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        edit().putString(key(property), value).apply()
    }
}

fun SharedPreferences.string(
    defaultValue: String = "",
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, String> =
    object : ReadWriteProperty<Any, String> {
        override fun getValue(
            thisRef: Any,
            property: KProperty<*>
        ) = getString(key(property), defaultValue) ?: ""

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: String
        ) = edit().putString(key(property), value).apply()
    }

fun SharedPreferences.int(
    defaultValue: Int = 0,
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, Int> =
    object : ReadWriteProperty<Any, Int> {
        override fun getValue(
            thisRef: Any,
            property: KProperty<*>
        ) = getInt(key(property), defaultValue)

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: Int
        ) = edit().putInt(key(property), value).apply()
    }

fun SharedPreferences.float(
    defaultValue: Float = 0.0f,
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, Float> =
    object : ReadWriteProperty<Any, Float> {
        override fun getValue(
            thisRef: Any,
            property: KProperty<*>
        ) = getFloat(key(property), defaultValue)

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: Float
        ) = edit().putFloat(key(property), value).apply()
    }

fun SharedPreferences.double(
    defaultValue: Double = 0.0,
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, Double> =
    object : ReadWriteProperty<Any, Double> {
        override fun getValue(
            thisRef: Any,
            property: KProperty<*>
        ) = getString(key(property), defaultValue.toString())!!.toFloat().toDouble()

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: Double
        ) = edit().putString(key(property), value.toString()).apply()
    }

fun SharedPreferences.long(
    defaultValue: Long = 0,
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, Long> =
    object : ReadWriteProperty<Any, Long> {
        override fun getValue(
            thisRef: Any,
            property: KProperty<*>
        ) = getLong(key(property), defaultValue)

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: Long
        ) = edit().putLong(key(property), value).apply()
    }

fun SharedPreferences.boolean(
    defaultValue: Boolean = false,
    key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, Boolean> =
    object : ReadWriteProperty<Any, Boolean> {
        override fun getValue(
            thisRef: Any,
            property: KProperty<*>
        ) = getBoolean(key(property), defaultValue)

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: Boolean
        ) = edit().putBoolean(key(property), value).apply()
    }

inline fun <reified T> SharedPreferences.custom(
    gson: Gson,
    crossinline key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, T?> =
    object : ReadWriteProperty<Any, T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T? {
            val json = getString(key(property), "")
            return if (json.isNullOrBlank()) null
            else gson.fromJson(json, T::class.java)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            if (value != null) edit().putString(key(property), gson.toJson(value)).apply()
            else edit().putString(key(property), "").apply()
        }
    }

inline fun <reified T> SharedPreferences.customList(
    gson: Gson,
    crossinline key: (KProperty<*>) -> String = KProperty<*>::name
): ReadWriteProperty<Any, List<T>> =
    object : ReadWriteProperty<Any, List<T>> {
        override fun getValue(thisRef: Any, property: KProperty<*>): List<T> {
            val json = getString(key(property), "")
            return if (json.isNullOrBlank()) emptyList()
            else gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: List<T>) {
            if (!value.isNullOrEmpty()) edit().putString(key(property), gson.toJson(value)).apply()
            else emptyList<T>()
        }
    }