package com.reactive.ludito.ui.screens.tabs.profile

import android.view.LayoutInflater
import com.reactive.ludito.databinding.ScreenMainBinding
import com.reactive.premier.base.BasePremierFragment
import com.reactive.premier.base.BasePremierViewModel

class ProfileScreen :
    BasePremierFragment<ScreenMainBinding, BasePremierViewModel>(BasePremierViewModel::class) {

    override fun getBinding(inflater: LayoutInflater) = ScreenMainBinding.inflate(inflater)

    override fun initialize() {

    }
}