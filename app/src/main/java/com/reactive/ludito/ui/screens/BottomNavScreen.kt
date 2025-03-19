package com.reactive.ludito.ui.screens

import android.view.LayoutInflater
import android.view.MenuItem
import com.reactive.ludito.ui.screens.tabs.map.MapScreen
import com.reactive.ludito.ui.screens.tabs.profile.ProfileScreen
import com.reactive.ludito.ui.screens.tabs.saved.SavedScreen
import com.reactive.premier.R
import com.reactive.premier.base.BasePremierFragment
import com.reactive.premier.base.BasePremierViewModel
import com.reactive.premier.databinding.ScreenBottomNavBinding

internal class BottomNavScreen :
    BasePremierFragment<ScreenBottomNavBinding, BasePremierViewModel>(BasePremierViewModel::class) {

    override fun getBinding(inflater: LayoutInflater) = ScreenBottomNavBinding.inflate(inflater)

    private var bottomFragments = arrayListOf(
        SavedScreen(),
        MapScreen(),
        ProfileScreen(),
    )

    override fun initialize() {
        binding.bottomNav.setOnNavigationItemSelectedListener { item: MenuItem ->
            return@setOnNavigationItemSelectedListener when (item.itemId) {
                R.id.nav_saved -> {
                    selectFragment(0)
                    true
                }

                R.id.nav_map -> {
                    selectFragment(1)
                    true
                }

                R.id.nav_profile -> {
                    selectFragment(2)
                    true
                }

                else -> false
            }
        }

        selectFragment(0)
    }

    private fun selectFragment(pos: Int) {
        replaceFragment(bottomFragments[pos])
    }
}
