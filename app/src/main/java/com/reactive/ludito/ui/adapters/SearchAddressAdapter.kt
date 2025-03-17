package com.reactive.ludito.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.reactive.ludito.data.SearchAddress
import com.reactive.ludito.databinding.ItemAddressesSearchBinding
import com.reactive.premier.base.BasePremierAdapter

class SearchAddressAdapter : BasePremierAdapter<SearchAddress, ItemAddressesSearchBinding>() {

    override fun getBinding(
        inflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ) = ItemAddressesSearchBinding.inflate(inflater, parent, false)

    override fun bindViewHolder(holder: ViewBindingHolder, data: SearchAddress) {
        holder.binding {
            data.let {
                name.text = data.name
                address.text = data.address
                distance.text = data.distance
            }
        }
    }
}
