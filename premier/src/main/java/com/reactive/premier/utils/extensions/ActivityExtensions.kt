package com.reactive.premier.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.allGranted
import com.reactive.premier.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion

var exit = false
fun FragmentActivity.exitVariant() {
    if (exit) {
        finishAffinity()
    } else {
        Toast.makeText(this, this.getString(R.string.back_again), Toast.LENGTH_SHORT).show()
        exit = true
        Handler().postDelayed({ exit = false }, 2000)
    }
}

fun hideKeyboard(view: View?) {
    if (view != null) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

@SuppressLint("CheckResult")
fun AppCompatActivity.checkPermissions(vararg permissions: String, action: () -> Unit = {}) {
    checkAppPermissions(this, this, permissions, action)
}

private fun checkAppPermissions(
    lifecycleOwner: LifecycleOwner,
    context: Context,
    permissions: Array<out String>,
    action: () -> Unit
) {
    lifecycleOwner.lifecycleScope.launchWhenStarted {
        PermissionRequester.instance().request(*permissions)
            .onCompletion {
                val allGranted: Boolean =
                    PermissionRequester.instance().request(*permissions).allGranted()
                if (allGranted) {
                    action()
                } else {
                    toastLong(context, context.getString(R.string.givePermission))
                }
            }
            .distinctUntilChanged()
            .collect()

    }
}
