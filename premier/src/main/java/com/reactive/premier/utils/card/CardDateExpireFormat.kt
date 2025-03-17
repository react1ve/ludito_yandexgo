package com.reactive.premier.utils.card

import android.text.Editable
import android.text.TextWatcher

class CardDateExpireFormat(private val listener: () -> Unit) : TextWatcher {

    private var lock = false

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.length > 4) {
            listener()
        }
        if (lock || s.length > 4) {
            return
        }
        lock = true
        var i = 2
        while (i < s.length) {
            if (s.toString()[i] != '/') {
                s.insert(i, "/")
            }
            i += 2
        }
        lock = false
    }
}