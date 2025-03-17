package com.reactive.premier.utils.extensions

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.TimeZone

@SuppressLint("SimpleDateFormat")
fun utcToLocal(utcTime: String): Long {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    val value = formatter.parse(utcTime)!!
    return value.time
}