package com.reactive.ludito.ui.screens.tabs.saved

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.R
import com.reactive.ludito.databinding.FragmentTabSavedBinding
import com.reactive.ludito.ui.adapters.SavedLocationsAdapter
import com.reactive.premier.base.BasePremierFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedScreen :
    BasePremierFragment<FragmentTabSavedBinding, SavedViewModel>(SavedViewModel::class) {

    override fun getBinding(inflater: LayoutInflater) = FragmentTabSavedBinding.inflate(inflater)

    private val adapter by lazy { SavedLocationsAdapter { viewModel.deleteLocation(it) } }

    override fun initialize() {
        with(binding) {
            toolbar.toolbarText.text = getString(R.string.my_addresses)

            recycler.adapter = adapter

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.savedLocations.collectLatest { locations ->
                    adapter.setData(locations)
                }
            }

        }
    }
}