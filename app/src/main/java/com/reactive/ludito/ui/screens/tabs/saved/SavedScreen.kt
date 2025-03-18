package com.reactive.ludito.ui.screens.tabs.saved

import android.view.LayoutInflater
import com.reactive.ludito.R
import com.reactive.ludito.databinding.FragmentTabSavedBinding
import com.reactive.premier.base.BasePremierFragment
import com.reactive.premier.base.BasePremierViewModel

class SavedScreen :
    BasePremierFragment<FragmentTabSavedBinding, BasePremierViewModel>(BasePremierViewModel::class) {

    override fun getBinding(inflater: LayoutInflater) = FragmentTabSavedBinding.inflate(inflater)

    override fun initialize() = with(binding) {
        toolbar.toolbarText.text = getString(R.string.my_addresses)
    }
}