package com.reactive.ludito.ui.screens.tabs.map.search

import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.SearchView
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.R
import com.reactive.ludito.databinding.BottomSheetSearchBinding
import com.reactive.ludito.ui.adapters.SearchAddressAdapter
import com.reactive.premier.utils.bottomsheet.BottomSheetRoundedFragment
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

internal class SearchBottomSheet(
    private val visibleRegion: VisibleRegion,
    private val onSearchResult: (data: List<SearchResponseItem>, itemsBoundingBox: BoundingBox) -> Unit
) : BottomSheetRoundedFragment<BottomSheetSearchBinding>() {

    override fun getBinding(inflater: LayoutInflater) = BottomSheetSearchBinding.inflate(inflater)

    private val viewModel by inject<SearchViewModel>()
    private val searchAdapter by lazy {
        SearchAddressAdapter().apply {
            listener = {
                viewModel.onSuggestionClick(it)
            }
        }
    }

    override fun initialize() {
        setMaxHeight(0.9)
        setupSearch()
        setupRecycler()
        listenUiChanges()
    }

    private fun setupSearch() = binding.searchContainer.search.apply {
        requestFocus()
        isIconifiedByDefault = false
        queryHint = getString(R.string.search_hint)

        setOnSearchClickListener {
            viewModel.startSearch()
        }

        setOnCloseListener {
            viewModel.reset()
            true
        }

        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { performSearch(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if ((newText?.length ?: 0) >= 3) {
                    performSearch(newText!!)
                }
                return true
            }
        })

        viewModel.setVisibleRegion(visibleRegion)

    }

    private fun performSearch(query: String) {
        if (query == viewModel.uiState.value.query) return
        viewModel.setQueryText(query)
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

                searchAdapter.setData(
                    (it.suggestState as? SuggestState.Success)?.items ?: emptyList()
                )

                if (successSearchState?.zoomToItems == true) {
                    onSearchResult(
                        searchItems,
                        successSearchState.itemsBoundingBox
                    )
                    dismiss()
                }

            }
            .launchIn(lifecycleScope)

        viewModel.subscribeForSuggest().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
        viewModel.subscribeForSearch().flowWithLifecycle(lifecycle).launchIn(lifecycleScope)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.reset()
    }
}