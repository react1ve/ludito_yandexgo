package com.reactive.premier.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BasePremierAdapter<T, V : ViewBinding> :
    RecyclerView.Adapter<BasePremierAdapter<T, V>.ViewBindingHolder>() {

    var listener: ((data: T) -> Unit)? = null
    var listenerPosition: ((position: Int) -> Unit)? = null
    protected var items = listOf<T>()
    open fun setData(data: List<T>) {
        items = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewBindingHolder {
        return ViewBindingHolder(getBinding(LayoutInflater.from(parent.context), parent, viewType))
    }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: ViewBindingHolder, position: Int) {
        holder.apply {
            bindViewHolder(this, items[holder.adapterPosition])
            holder.itemView.setOnClickListener {
                if (holder.adapterPosition != -1) {
                    listener?.invoke(items[holder.adapterPosition])
                    listenerPosition?.invoke(holder.adapterPosition)
                }
            }
        }
    }

    abstract fun getBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): V
    abstract fun bindViewHolder(holder: ViewBindingHolder, data: T)

    inner class ViewBindingHolder(val binding: V) : RecyclerView.ViewHolder(binding.root)

    operator fun V.invoke(init: V.() -> Unit): V = this.apply(init)
}