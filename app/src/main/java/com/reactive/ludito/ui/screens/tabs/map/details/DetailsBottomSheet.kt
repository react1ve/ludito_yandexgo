package com.reactive.ludito.ui.screens.tabs.map.details

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.databinding.BottomSheetDetailsBinding
import com.reactive.premier.R
import com.reactive.premier.utils.bottomsheet.BottomSheetRoundedFragment
import org.koin.android.ext.android.inject

internal class DetailsBottomSheet(private val onClose: () -> Unit) :
    BottomSheetRoundedFragment<BottomSheetDetailsBinding>() {

    override fun getBinding(inflater: LayoutInflater) = BottomSheetDetailsBinding.inflate(inflater)


    private val viewModel by inject<DetailsViewModel>()
    private var locationInfo: LocationInfo? = null
    private val animFadeOut by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
    }
    private val animFadeIn by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
    }

    fun setLocationInfo(locationInfo: LocationInfo) {
        this.locationInfo = locationInfo
    }

    override fun initialize() {

        with(binding) {
            name.setOnClickListener { dismiss() }
            addToFavorites.setOnClickListener { viewModel.addToFavorite(locationInfo) }

            locationInfo?.let {
                name.text = it.name
                address.text = it.address
                ratingContainer.setRating(it.rating, it.feedbackCount)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClose.invoke()
    }
}