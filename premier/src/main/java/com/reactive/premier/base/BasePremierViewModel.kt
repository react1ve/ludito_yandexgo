package com.reactive.premier.base

import androidx.lifecycle.ViewModel
import com.reactive.premier.utils.preferences.PremierSharedManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class BasePremierViewModel : ViewModel(), KoinComponent {

    val premierSharedManager: PremierSharedManager by inject()

}