package com.reactive.ludito.ui.custom_views

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.reactive.ludito.databinding.ViewSearchBinding

class CustomSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSearchBinding =
        ViewSearchBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        with(binding) {
            searchEditText.doAfterTextChanged {
                clearButton.visibility =
                    if (it?.isNotEmpty() == true) View.VISIBLE else View.GONE
            }

            clearButton.setOnClickListener {
                onClearListener?.invoke()
                searchEditText.text.clear()
            }
        }
    }

    fun disable() {
        binding.searchEditText.inputType = InputType.TYPE_NULL
    }

    private fun getText(): String = binding.searchEditText.text.toString()

    fun setText(text: CharSequence?) {
        binding.searchEditText.setText(text)
    }

    private var onClearListener: (() -> Unit)? = null

    fun setOnClearClickedListener(listener: () -> Unit) {
        this.onClearListener = listener
    }

    fun setQueryChangedListener(listener: (query: String) -> Unit) {
        binding.searchEditText.doAfterTextChanged {
            listener(getText())
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        super.setOnClickListener(listener)
        binding.searchEditText.setOnClickListener(listener)
        binding.root.setOnClickListener(listener)
    }
}