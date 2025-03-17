package com.reactive.premier.utils.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

fun rateApp(context: Context) =
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=PackageName")))
