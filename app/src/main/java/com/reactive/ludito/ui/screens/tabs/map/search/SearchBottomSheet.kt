package com.reactive.ludito.ui.screens.tabs.map.search

import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.data.SearchAddress
import com.reactive.ludito.databinding.BottomSheetSearchBinding
import com.reactive.ludito.ui.adapters.SearchAddressAdapter
import com.reactive.premier.utils.bottomsheet.BottomSheetRoundedFragment
import com.reactive.premier.utils.extensions.gone
import com.reactive.premier.utils.extensions.showGone
import com.reactive.premier.utils.extensions.showKeyboard
import com.reactive.premier.utils.extensions.visible
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

internal class SearchBottomSheet(
    private val visibleRegion: VisibleRegion,
    private val userLocation: Point? = null,
    private val onSearchResult: (data: SearchAddress) -> Unit
) : BottomSheetRoundedFragment<BottomSheetSearchBinding>() {

    override fun getBinding(inflater: LayoutInflater) = BottomSheetSearchBinding.inflate(inflater)

    private val viewModel by inject<SearchViewModel>()
    private val searchAdapter by lazy {
        SearchAddressAdapter().apply {
            listener = {
                onSearchResult(it)
                dismiss()
            }
        }
    }

    override fun initialize() {
        setMaxHeight(0.9)
        setupSearch()
        setupRecycler()
        setupLocation()
        listenUiChanges()
    }

    private fun setupSearch() = with(binding.searchView) {
        showKeyboard()
        requestFocus()

        setQueryChangedListener { query ->
            if (query.length >= 3) {
                performSearch(query)
            } else if (query.isEmpty()) {
                viewModel.reset()
                searchAdapter.setData(emptyList())
            }
        }

        setOnClearClickedListener {
            viewModel.reset()
            searchAdapter.setData(emptyList())
        }

        viewModel.setVisibleRegion(visibleRegion)
    }

    private fun setupLocation() {
        userLocation?.let { location ->
            viewModel.setUserLocation(location)
        }
    }

    private fun performSearch(query: String) {
        if (query == viewModel.uiState.value.query) return

        viewModel.setQueryText(query)
        viewModel.startSearch()
    }

    private fun setupRecycler() = with(binding.recycler) {
        adapter = searchAdapter
    }

    private fun listenUiChanges() = with(binding) {
        viewModel.uiState
            .flowWithLifecycle(lifecycle)
            .onEach {
                when (val searchState = it.searchState) {
                    is SearchState.Loading -> {
                        progressBar.visible()
                    }

                    is SearchState.Success -> {
                        progressBar.gone()
                        searchAdapter.setData(searchState.items)

                        emptyView.showGone(searchState.items.isEmpty())
                    }

                    is SearchState.Error -> {
                        progressBar.gone()
                        errorView.visible()
                    }

                    else -> {
                        progressBar.gone()
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.subscribeForSearch().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.reset()
    }
}