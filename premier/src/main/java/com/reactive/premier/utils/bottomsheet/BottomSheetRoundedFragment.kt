package com.reactive.premier.utils.bottomsheet

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reactive.premier.R

abstract class BottomSheetRoundedFragment<V : ViewBinding> :
    BottomSheetDialogFragment() {

    protected lateinit var binding: V
    abstract fun getBinding(inflater: LayoutInflater): V

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBinding(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initialize()
    }

    abstract fun initialize()

    fun setMaxHeight(height: Double) {
        val dm = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        val currentHeight = (dm.heightPixels * height).toInt()
        dialog?.setOnShowListener {
            val bottomSheetDialog = dialog as? BottomSheetDialog
            val bottomSheetInternal: View? =
                bottomSheetDialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetInternal?.apply {
                val behavior = BottomSheetBehavior.from(this)
                if (currentHeight != 0) {
                    behavior.peekHeight = currentHeight
                    this.layoutParams.height = currentHeight
                }
                behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                behavior.skipCollapsed = false
            }
        }
    }

    fun closeSheet() = this.dismiss()

    operator fun V.invoke(init: V.() -> Unit): V = this.apply(init)
}