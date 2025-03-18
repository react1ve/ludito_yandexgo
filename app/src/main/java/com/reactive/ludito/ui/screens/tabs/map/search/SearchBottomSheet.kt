package com.reactive.ludito.ui.screens.tabs.map.search

import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.data.SearchAddress
import com.reactive.ludito.databinding.BottomSheetSearchBinding
import com.reactive.ludito.ui.adapters.SearchAddressAdapter
import com.reactive.premier.utils.bottomsheet.BottomSheetRoundedFragment
import com.reactive.premier.utils.extensions.showKeyboard
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

internal class SearchBottomSheet(
    private val visibleRegion: VisibleRegion,
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
        listenUiChanges()
    }

    private fun setupSearch() = with(binding.searchView) {
        showKeyboard()
        requestFocus()

        setQueryChangedListener { query ->
            if (query.length >= 3) {
                performSearch(query)
            }
        }

        setOnClearClickedListener {
            viewModel.reset()
        }

        viewModel.setVisibleRegion(visibleRegion)

    }

    private fun performSearch(query: String) {
        if (query == viewModel.uiState.value.query) return

        viewModel.setQueryText(query)
        viewModel.startSearch()
    }


    private fun setupRecycler() = with(binding.recycler) {
        adapter = searchAdapter
    }

    private fun listenUiChanges() {
        viewModel.uiState
            .flowWithLifecycle(lifecycle)
            .onEach {
                val successSearchState = it.searchState as? SearchState.Success
                val searchItems = successSearchState?.items ?: emptyList()

                searchAdapter.setData(searchItems)
            }
            .launchIn(lifecycleScope)

        viewModel.subscribeForSearch().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.reset()
    }
}