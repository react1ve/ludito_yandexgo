package com.reactive.premier.utils.card

import android.text.Editable
import android.text.TextWatcher


class CardNumberFormat(private val action: () -> Unit) : TextWatcher {

    private var lock = false
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        if (s.length > 19) {
            s.delete(18, s.length - 1)
        }
        if (lock || s.length > 19) {
            return
        }
        lock = true
        var i = 4
        while (i < s.length) {
            if (s.toString()[i] != ' ') {
                s.insert(i, " ")
            }
            i += 5
        }
        lock = false

        if (s.length > 18) action()
    }
}

