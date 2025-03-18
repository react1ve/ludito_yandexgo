package com.reactive.ludito.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.databinding.ItemAddressesSavedBinding
import com.reactive.premier.base.BasePremierAdapter

class SavedLocationsAdapter(private val onDelete: (data: LocationInfo) -> Unit) :
    BasePremierAdapter<LocationInfo, ItemAddressesSavedBinding>() {

    override fun getBinding(
        inflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ) = ItemAddressesSavedBinding.inflate(inflater, parent, false)

    override fun bindViewHolder(holder: ViewBindingHolder, data: LocationInfo) {
        holder.binding {
            data.let {
                name.text = data.name
                address.text = data.address
            }
            delete.setOnClickListener { onDelete(data) }
        }
    }
}
