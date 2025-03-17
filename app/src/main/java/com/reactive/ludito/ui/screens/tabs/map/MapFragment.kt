package com.reactive.ludito.ui.screens.tabs.map

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.lifecycleScope
import com.reactive.ludito.R
import com.reactive.ludito.databinding.TabMapBinding
import com.reactive.ludito.ui.screens.tabs.map.search.SearchBottomSheet
import com.reactive.ludito.ui.screens.tabs.map.search.SearchResponseItem
import com.reactive.premier.base.BasePremierFragment
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.reactive.premier.R as R2

class MapFragment : BasePremierFragment<TabMapBinding, MapViewModel>(MapViewModel::class) {

    override fun getBinding(inflater: LayoutInflater): TabMapBinding =
        TabMapBinding.inflate(inflater)

    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var map: Map
    private lateinit var animFadeIn: Animation
    private lateinit var animFadeOut: Animation
    private var isMoving = false
    private val cameraDebounceHandler = Handler(Looper.getMainLooper())
    private var cameraDebounceRunnable: Runnable? = null

    private val searchResultPlacemarkTapListener = MapObjectTapListener { mapObject, _ ->
        // Show details dialog on placemark tap.
        val selectedObject = (mapObject.userData as? GeoObject)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun initialize() {
        setupAnimations()
        setupMap()
        setupSearch()
    }

    private fun setupSearch() = with(binding.searchView.search) {
        isClickable = false

        val bottomsSheet = SearchBottomSheet(visibleRegion = map.visibleRegion) { list, itemBox ->
            updateSearchResponsePlacemarks(list)

            focusCamera(
                list.map { item -> item.point },
                itemBox,
            )

        }
        setOnClickListener {
            bottomsSheet.show(childFragmentManager, "")
        }
    }

    private fun updateSearchResponsePlacemarks(items: List<SearchResponseItem>) {
        map.mapObjects.clear()

        val imageProvider = ImageProvider.fromResource(requireContext(), R2.drawable.search_result)

        items.forEach {
            map.mapObjects.addPlacemark().apply {
                geometry = it.point
                setIcon(imageProvider, IconStyle().apply { scale = 0.5f })
                addTapListener(searchResultPlacemarkTapListener)
                userData = it.geoObject
            }
        }
    }

    private fun focusCamera(points: List<Point>, boundingBox: BoundingBox) {
        if (points.isEmpty()) return

        val position = if (points.size == 1) {
            getAddressAsync(points.first().latitude.toString(), points.first().longitude.toString())
            map.cameraPosition.run {
                CameraPosition(points.first(), zoom, azimuth, tilt)
            }
        } else {
            map.cameraPosition(Geometry.fromBoundingBox(boundingBox))
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

    private fun setupAnimations() {
        animFadeOut = AnimationUtils.loadAnimation(requireContext(), R2.anim.fade_out)
        animFadeIn = AnimationUtils.loadAnimation(requireContext(), R2.anim.fade_in)
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
            userLocationLayer = mapKit.createUserLocationLayer(binding.mapview.mapWindow).apply {
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
        hideAddressViews()
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
                fetchAddressForLocation(cameraPosition.target)
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

    private fun hideAddressViews() {
        with(binding.bottomSheet) {
            placeName.animateAddress(false)
            placeAddress.animateAddress(false)
        }
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
            fetchAddressForLocation(position.target)
        }
    }

    private fun fetchAddressForLocation(location: Point) {
        getAddressAsync(location.latitude.toString(), location.longitude.toString())
    }

    private fun getAddressAsync(lat: String, lng: String) {
        lifecycleScope.launch {
            try {
                updateAddress(getString(R.string.loading_address), "")

                val locationInfo = withContext(Dispatchers.IO) {
                    viewModel.getAddressFromCoordinates(lat, lng)
                }

                updateAddress(locationInfo.name, locationInfo.address)
            } catch (e: Exception) {
                updateAddress(getString(R.string.address_not_found), "")
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

    private fun updateAddress(name: String, address: String) {
        with(binding.bottomSheet) {
            placeName.text = name
            placeAddress.text = address
            placeName.animateAddress(true)
            placeAddress.animateAddress(true)
        }
    }

    private fun View.animateAddress(show: Boolean) {
        val translationY = if (show) 0f else 20f
        val alpha = if (show) 1f else 0f
        val animation = if (show) animFadeIn else animFadeOut

        animate().translationY(translationY).alpha(alpha).setInterpolator(DecelerateInterpolator())
            .setDuration(300).start()

        startAnimation(animation)
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