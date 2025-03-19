package com.reactive.premier.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.viewbinding.ViewBinding
import com.reactive.premier.R
import com.reactive.premier.utils.extensions.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModelForClass
import kotlin.reflect.KClass


abstract class BasePremierFragment<V : ViewBinding, VM : BasePremierViewModel>(clazz: KClass<VM>) :
    Fragment() {

    val viewModel: VM by viewModelForClass(clazz)
    lateinit var premierActivity: BasePremierActivity<*>

    protected var enableCustomBackPress = false
    protected lateinit var binding: V
    abstract fun getBinding(inflater: LayoutInflater): V

    override fun onAttach(context: Context) {
        super.onAttach(context)
        premierActivity = requireActivity() as BasePremierActivity<*>
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBinding(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()

        setFocus(view)

        initClicks()
    }

    override fun onResume() {
        super.onResume()
        observe()
    }

    abstract fun initialize()

    fun addFragment(
        fragment: Fragment,
        addBackStack: Boolean = true,
        @IdRes id: Int = parentLayoutId(),
        tag: String = fragment::class.java.simpleName
    ) {
        showProgress(false)
        hideKeyboard()
        activity?.supportFragmentManager?.commit(allowStateLoss = true) {
            if (addBackStack && !fragment.isAdded) addToBackStack(tag)
            setCustomAnimations(
                R.anim.enter_from_bottom,
                R.anim.exit_to_top,
                R.anim.enter_from_top,
                R.anim.exit_to_bottom
            )
            add(id, fragment)
        }
    }

    fun finishFragment() {
        hideKeyboard()
        showProgress(false)
        activity?.supportFragmentManager?.popBackStackImmediate()
    }

    fun popInclusive(name: String? = null, flags: Int = FragmentManager.POP_BACK_STACK_INCLUSIVE) {
        hideKeyboard()
        showProgress(false)
        activity?.supportFragmentManager?.popBackStackImmediate(name, flags)
    }

    protected open fun onFragmentBackButtonPressed() {
    }

    open fun openFromBackFragment() {
    }

    protected open fun observe() {
    }

    protected open fun initClicks() {
    }

    fun showProgress(show: Boolean) {
        (activity as? BasePremierActivity<*>)?.showProgress(show)
    }

    protected fun hideKeyboard() {
        view?.let {
            val imm =
                it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setFocus(view: View) {
        view.apply {
            isFocusableInTouchMode = true
            requestFocus()
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (enableCustomBackPress) onFragmentBackButtonPressed()
                    else activity?.onBackPressed()
                }
                enableCustomBackPress = false
                true
            }
        }
    }

    fun windowAdjustPan() =
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

    fun windowAdjustResize() =
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    override fun onDestroyView() {
        showProgress(false)
        super.onDestroyView()
    }

    operator fun V.invoke(init: V.() -> Unit): V = this.apply(init)

}

const val TARGET_FRAGMENT_REQUEST_CODE = 123
fun Fragment.setAsTargetFragment(
    owner: Fragment,
    requestCode: Int = TARGET_FRAGMENT_REQUEST_CODE
): Fragment {
    this.setTargetFragment(owner, requestCode)
    return this
}

fun Fragment.setResultOk(
    requestCode: Int = TARGET_FRAGMENT_REQUEST_CODE,
    data: Intent? = null
) {
    this.targetFragment?.onActivityResult(requestCode, Activity.RESULT_OK, data)
}

fun FragmentActivity.initialFragment(fragment: Fragment, showAnim: Boolean = false) {
    supportFragmentManager.commit(allowStateLoss = true) {
        if (showAnim)
            setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
        replace(parentLayoutId(), fragment)
    }
}

fun FragmentActivity.addFragment(
    fragment: Fragment,
    addBackStack: Boolean = true,
    tag: String = fragment.hashCode().toString()
) {
    hideKeyboard(fragment.view)
    supportFragmentManager.commit(allowStateLoss = true) {
        if (addBackStack && !fragment.isAdded) addToBackStack(tag)
        setCustomAnimations(
            R.anim.enter_from_bottom,
            R.anim.exit_to_top,
            R.anim.enter_from_top,
            R.anim.exit_to_bottom
        )
        add(parentLayoutId(), fragment)
    }
}

fun FragmentActivity.finishFragment() = supportFragmentManager.popBackStack()
fun parentLayoutId() = BasePremierActivity.parentLayoutId