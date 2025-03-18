package com.reactive.ludito.ui.screens.tabs.map

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.R
import com.reactive.ludito.data.LocationInfo
import com.reactive.ludito.data.SearchAddress
import com.reactive.ludito.databinding.FragmentTabMapBinding
import com.reactive.ludito.ui.screens.tabs.map.details.DetailsBottomSheet
import com.reactive.ludito.ui.screens.tabs.map.search.SearchBottomSheet
import com.reactive.premier.base.BasePremierFragment
import com.reactive.premier.utils.extensions.toast
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.reactive.premier.R as R2

class MapScreen : BasePremierFragment<FragmentTabMapBinding, MapViewModel>(MapViewModel::class) {

    override fun getBinding(inflater: LayoutInflater): FragmentTabMapBinding =
        FragmentTabMapBinding.inflate(inflater)

    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var map: Map
    private var isMoving = false
    private val cameraDebounceHandler = Handler(Looper.getMainLooper())
    private var cameraDebounceRunnable: Runnable? = null
    private val detailsBottomSheet by lazy {
        DetailsBottomSheet {
            binding.searchView.setText("")
            focusOnUserLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun initialize() {
        setupMap()
        setupSearch()
    }

    private fun setupSearch() = with(binding.searchView) {
        disable()

        val bottomsSheet = SearchBottomSheet(
            visibleRegion = map.visibleRegion,
            userLocation = userLocationLayer.cameraPosition()?.target
        ) { address ->
            updateSearchResponsePlacemarks(address)
            focusCamera(address.point)
        }

        setOnClickListener {
            bottomsSheet.show(childFragmentManager, "")
        }
    }

    private fun updateSearchResponsePlacemarks(address: SearchAddress) {
        map.mapObjects.clear()

        val imageProvider = ImageProvider.fromResource(requireContext(), R2.drawable.search_result)

        map.mapObjects.addPlacemark().apply {
            geometry = address.point
            setIcon(imageProvider, IconStyle().apply { scale = 0.5f })
            userData = address.geoObject
        }
    }

    private fun focusCamera(point: Point) {
        getAddressAsync(point.latitude.toString(), point.longitude.toString())

        val position = map.cameraPosition.run {
            CameraPosition(point, zoom, azimuth, tilt)
        }

        map.move(
            position,
            com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.5f),
            null
        )
    }

    override fun initClicks() {
        with(binding) {
            myLocation.setOnClickListener { focusOnUserLocation() }
            zoomIn.setOnClickListener { adjustZoom(1) }
            zoomOut.setOnClickListener { adjustZoom(-1) }
        }
    }

    private fun setupMap() = with(binding) {
        map = mapview.mapWindow.map.apply {
            isRotateGesturesEnabled = false
        }

        setupLocationLayer()
        moveToInitialLocation()
        setupCameraListener()
    }

    private fun setupLocationLayer() {
        MapKitFactory.getInstance().also { mapKit ->
            userLocationLayer =
                mapKit.createUserLocationLayer(binding.mapview.mapWindow).apply {
                    isVisible = true
                    isHeadingEnabled = true
                }
        }
    }

    private fun moveToInitialLocation() {
        val location = viewModel.premierSharedManager.let {
            val lat = it.latitude
            val lng = it.longitude
            if (lat != 0.0 && lng != 0.0) Point(lat, lng) else Point(41.311081, 69.240562)
        }
        moveCamera(location, 15f)
    }

    private fun setupCameraListener() {
        map.addCameraListener { _, cameraPosition, _, finishedMoving ->
            handleCameraMovement(finishedMoving, cameraPosition)
        }
    }

    private fun handleCameraMovement(finishedMoving: Boolean, cameraPosition: CameraPosition) {
        if (!isMoving && !finishedMoving) {
            onCameraMovementStarted()
        }

        if (finishedMoving) {
            onCameraMovementFinished(cameraPosition)
        }
    }

    private fun onCameraMovementStarted() {
        isMoving = true
        startPinLiftAnimation()
    }

    private fun onCameraMovementFinished(cameraPosition: CameraPosition) {
        isMoving = false
        cancelPendingAddressLookup()
        schedulePinDropAndAddressLookup(cameraPosition)
    }

    private fun cancelPendingAddressLookup() {
        cameraDebounceRunnable?.let { cameraDebounceHandler.removeCallbacks(it) }
    }

    private fun schedulePinDropAndAddressLookup(cameraPosition: CameraPosition) {
        cameraDebounceRunnable = Runnable {
            startPinDropAnimation {
                val location = cameraPosition.target
//                getAddressAsync(location.latitude.toString(), location.longitude.toString())
            }
        }
        cameraDebounceHandler.postDelayed(cameraDebounceRunnable!!, 150)
    }

    private fun startPinLiftAnimation() {
        val pinLiftHeight = resources.displayMetrics.heightPixels / 20f
        animatePinElevation(true, pinLiftHeight)
    }

    private fun startPinDropAnimation(onAnimationComplete: () -> Unit) {
        val pinLiftHeight = resources.displayMetrics.heightPixels / 20f
        animatePinElevation(false, pinLiftHeight, onAnimationComplete)
    }

    private fun animatePinElevation(
        moveUp: Boolean, distance: Float, onComplete: (() -> Unit)? = null
    ) {
        val startValue = if (moveUp) 0f else -distance
        val endValue = if (moveUp) -distance else 0f

        val animator = ValueAnimator.ofFloat(startValue, endValue).apply {
            duration = if (moveUp) 300 else 400
            interpolator = if (moveUp) {
                DecelerateInterpolator(1.5f)
            } else {
                BounceInterpolator()
            }

            addUpdateListener { animation ->
                binding.imgLocationPinUp.translationY = animation.animatedValue as Float
            }

            if (!moveUp) {
                doOnEnd {
                    onComplete?.invoke()
                }
            }
        }

        animator.start()

        animatePinShadow(moveUp)
    }

    private fun animatePinShadow(moveUp: Boolean) {
        binding.imgLocationPinShadow.animate().scaleX(if (moveUp) 0.7f else 1.0f)
            .scaleY(if (moveUp) 0.7f else 1.0f).alpha(if (moveUp) 0.6f else 1.0f)
            .setDuration(if (moveUp) 300 else 400).setInterpolator(
                if (moveUp) DecelerateInterpolator(1.5f)
                else AccelerateDecelerateInterpolator()
            ).start()
    }

    private fun adjustZoom(value: Int) {
        map.move(
            CameraPosition(
                map.cameraPosition.target, map.cameraPosition.zoom + value, 0.0f, 0.0f
            ), com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 0.3f), null
        )
    }

    private fun focusOnUserLocation() {
        userLocationLayer.cameraPosition()?.let { position ->
            moveCamera(position.target)
            with(viewModel.premierSharedManager) {
                latitude = position.target.latitude
                longitude = position.target.longitude
            }
        }
    }

    private fun getAddressAsync(lat: String, lng: String) {
        lifecycleScope.launch {
            try {
                val locationInfo: LocationInfo = withContext(Dispatchers.IO) {
                    viewModel.getAddressFromCoordinates(lat, lng)
                }

                updateAddress(locationInfo)
            } catch (e: Exception) {
                toast(requireContext(), getString(R.string.address_not_found))
            }
        }
    }

    private fun moveCamera(point: Point, zoom: Float = 17.0f) {
        map.move(
            CameraPosition(point, zoom, 0.0f, 0.0f),
            com.yandex.mapkit.Animation(com.yandex.mapkit.Animation.Type.SMOOTH, 1f),
            null
        )
    }

    private fun updateAddress(locationInfo: LocationInfo) {
        binding.searchView.setText(locationInfo.name)

        if ((detailsBottomSheet.isAdded && detailsBottomSheet.isVisible).not()) {
            detailsBottomSheet.setLocationInfo(locationInfo)
            detailsBottomSheet.show(childFragmentManager, locationInfo.hashCode().toString())
        }
    }

    private fun ValueAnimator.doOnEnd(action: () -> Unit) {
        addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                action()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}