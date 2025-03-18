package com.reactive.ludito.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import com.reactive.ludito.R
import com.reactive.ludito.databinding.ActivityHomeBinding
import com.reactive.ludito.ui.screens.BottomNavScreen
import com.reactive.premier.base.BasePremierActivity
import com.reactive.premier.base.initialFragment
import com.reactive.premier.utils.extensions.checkPermissions
import com.reactive.premier.R as R2

class HomeActivity : BasePremierActivity<ActivityHomeBinding>() {

    override fun getViewBinding() =
        ActivityHomeBinding.inflate(LayoutInflater.from(applicationContext))

    override fun getActivityContainerId(): Int {
        return R.id.fragmentContainer
    }

    override fun getNavContainerId(): Int? {
        return R2.id.navContainer
    }

    override fun getProgressBar(): View = binding.progressBar

    override fun onActivityCreated() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        permissions.forEach {
            if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED)
                checkPermissions(it)
        }

        initialFragment(BottomNavScreen())
    }
}
