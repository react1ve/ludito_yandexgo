package com.reactive.ludito.ui.screens

import android.view.LayoutInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
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

    private val savedFragment by lazy { SavedScreen() }
    private val mapFragment by lazy { MapScreen() }
    private val profileFragment by lazy { ProfileScreen() }

    private var currentFragment: Fragment? = null

    override fun initialize() {
        binding.bottomNav.setOnNavigationItemSelectedListener { item: MenuItem ->
            return@setOnNavigationItemSelectedListener when (item.itemId) {
                R.id.nav_saved -> {
                    selectFragment(savedFragment)
                    true
                }

                R.id.nav_map -> {
                    selectFragment(mapFragment)
                    true
                }

                R.id.nav_profile -> {
                    selectFragment(profileFragment)
                    true
                }

                else -> false
            }
        }

        if (currentFragment == null) {
            selectFragment(savedFragment)
        }
    }

    private fun selectFragment(fragment: Fragment) {
        if (currentFragment !== fragment) {
            childFragmentManager.beginTransaction().apply {
                currentFragment?.let { hide(it) }

                if (fragment.isAdded) {
                    show(fragment)
                } else {
                    add(R.id.navContainer, fragment)
                }

                commit()
            }

            currentFragment = fragment
        }
    }
}