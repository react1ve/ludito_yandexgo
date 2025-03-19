package com.reactive.ludito.ui.screens.tabs.map.details

import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.R
import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.databinding.BottomSheetDetailsBinding
import com.reactive.premier.databinding.DialogAddToFavoritesBinding
import com.reactive.premier.utils.bottomsheet.BottomSheetRoundedFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.reactive.premier.R as R2

internal class DetailsBottomSheet(private val onClose: () -> Unit) :
    BottomSheetRoundedFragment<BottomSheetDetailsBinding>() {

    override fun getBinding(inflater: LayoutInflater) = BottomSheetDetailsBinding.inflate(inflater)

    private val viewModel by inject<DetailsViewModel>()
    private var locationInfo: LocationInfo? = null

    fun setLocationInfo(locationInfo: LocationInfo) {
        this.locationInfo = locationInfo
    }

    override fun initialize() {
        with(binding) {
            name.setOnClickListener { dismiss() }
            addToFavorites.setOnClickListener {
                viewModel.addToFavorite(locationInfo)
            }

            locationInfo?.let {
                name.text = it.name
                address.text = it.address
                ratingContainer.setRating(it.rating, it.feedbackCount)
                viewModel.checkIfSaved(it.id)
            }

            lifecycleScope.launch {
                viewModel.isSaved.collectLatest { isSaved ->
                    updateFavoriteButton(isSaved)
                }
            }
        }
    }

    private fun updateFavoriteButton(isSaved: Boolean) {
        with(binding) {
            if (isSaved) {
                addToFavorites.text = getString(R.string.remove_from_favorites)
                addToFavorites.setBackgroundResource(R2.drawable.rounded_button_red)
                addToFavorites.setOnClickListener {
                    viewModel.addToFavorite(locationInfo)
                }
            } else {
                addToFavorites.text = getString(R.string.add_to_favorites)
                addToFavorites.setBackgroundResource(R2.drawable.rounded_button_green)
                addToFavorites.setOnClickListener {
                    showAddToFavoritesDialog()
                }
            }
        }
    }

    private fun showAddToFavoritesDialog() {
        val dialogBinding = DialogAddToFavoritesBinding.inflate(layoutInflater)

        val dialog: AlertDialog =
            AlertDialog.Builder(requireContext(), R2.style.RoundedCornerDialog)
                .setView(dialogBinding.root)
                .setCancelable(true)
                .create()

        with(dialogBinding) {
            address.setText(locationInfo?.address)
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }
            confirmButton.setOnClickListener {
                viewModel.addToFavorite(locationInfo)
                dialog.dismiss()
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClose.invoke()
    }
}