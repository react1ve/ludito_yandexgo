package com.reactive.premier.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.reactive.premier.utils.extensions.exitVariant
import com.reactive.premier.utils.extensions.showGone

abstract class BasePremierActivity<V : ViewBinding> : AppCompatActivity() {

    companion object {
        @IdRes
        var parentLayoutId: Int = 0

        @IdRes
        var navLayoutId: Int? = 0
    }

    protected lateinit var binding: V

    abstract fun getViewBinding(): V

    @IdRes
    abstract fun getActivityContainerId(): Int

    @IdRes
    abstract fun getNavContainerId(): Int?

    abstract fun getProgressBar(): View?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()

        parentLayoutId = getActivityContainerId()
        navLayoutId = getNavContainerId()

        setContentView(binding.root)

        onActivityCreated()
    }

    abstract fun onActivityCreated()

    override fun onBackPressed() {
        when {
            supportFragmentManager.backStackEntryCount > 0 -> finishFragment()
            supportFragmentManager.backStackEntryCount == 0 -> exitVariant()
            else -> super.onBackPressed()
        }
    }

    fun showProgress(show: Boolean) {
        getProgressBar()?.showGone(show)
    }

    override fun onResume() {
        super.onResume()
//        updateManager?.onResume()
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.fragments.size >= 1) {
                (supportFragmentManager.fragments.last() as? BasePremierFragment<*, *>)?.let {
                    it.openFromBackFragment()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragmentsActivityResults(requestCode, resultCode, data)
    }

    private fun fragmentsActivityResults(requestCode: Int, resultCode: Int, data: Intent?) {
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fragmentsPermissionResults(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun fragmentsPermissionResults(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        for (fragment in supportFragmentManager.fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    operator fun V.invoke(init: V.() -> Unit): V = this.apply(init)
}