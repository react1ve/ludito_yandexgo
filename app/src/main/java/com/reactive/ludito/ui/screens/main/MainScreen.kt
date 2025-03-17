package com.reactive.ludito.ui.screens.main

import android.view.LayoutInflater
import com.reactive.ludito.databinding.ScreenMainBinding
import com.reactive.premier.base.BasePremierFragment

class MainScreen :
    BasePremierFragment<ScreenMainBinding, MainViewModel>(MainViewModel::class) {

    override fun getBinding(inflater: LayoutInflater) = ScreenMainBinding.inflate(inflater)

    override fun initialize() {
    }
}
