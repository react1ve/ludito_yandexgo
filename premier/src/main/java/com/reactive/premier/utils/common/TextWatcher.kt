package com.reactive.premier.utils.common

import android.text.Editable
import android.text.TextWatcher

abstract class TextWatcher(private val enable: Boolean = false) : TextWatcher {

    private var lock = false
    override fun afterTextChanged(s: Editable?) {
        if (enable) {
            lock = !lock
            if (lock) textChanged(s.toString())
        } else textChanged(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    abstract fun textChanged(s: String)
}


interface TextWatcherInterface : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
        textChanged(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    fun textChanged(s: String)
}
