package com.reactive.premier.utils.extensions

import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.smoothSnapToPosition(
    position: Int,
    snapMode: Int = LinearSmoothScroller.SNAP_TO_START
) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
    }
    if (position > -1) {
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }
}

fun RecyclerView.executeSafely(func: () -> Unit) {
    if (scrollState != RecyclerView.SCROLL_STATE_IDLE) {
        val animator = itemAnimator
        itemAnimator = null
        func()
        itemAnimator = animator
    } else {
        func()
    }
}

fun RecyclerView.Adapter<*>.notifyItemRemovedAndInserted(
    count: Int,
    positionStart: Int = 0
) {
    this.notifyItemRangeRemoved(positionStart, count)
    this.notifyItemRangeInserted(positionStart, count)
}