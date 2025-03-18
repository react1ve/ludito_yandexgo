package com.reactive.ludito.ui.screens.tabs.profile

import android.view.LayoutInflater
import com.reactive.ludito.R
import com.reactive.ludito.databinding.FragmentTabProfileBinding
import com.reactive.premier.base.BasePremierFragment
import com.reactive.premier.base.BasePremierViewModel

class ProfileScreen :
    BasePremierFragment<FragmentTabProfileBinding, BasePremierViewModel>(BasePremierViewModel::class) {

    override fun getBinding(inflater: LayoutInflater) = FragmentTabProfileBinding.inflate(inflater)

    override fun initialize() = with(binding) {
        toolbar.toolbarText.text = getString(R.string.my_profile)
    }
}