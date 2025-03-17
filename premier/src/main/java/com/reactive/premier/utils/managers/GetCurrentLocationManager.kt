package com.reactive.premier.utils.managers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import java.util.Locale


class GetCurrentLocationManager(
    private val context: FragmentActivity, private val listener: CurrentLocationDatasListener
) {

    /*
    * 1. Add location permissions (ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION) to Manifest
    * 2. Add deps to build.gradle.kts     implementation "com.google.android.gms:play-services-location:18.0.0"
    * 3. Check Runtime Permissions before initializing this method
    * */

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private var mGoogleApiClient: GoogleApiClient
    private var mLocationRequest: LocationRequest

    init {
        mGoogleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(p0: Bundle?) {
                    loge("connected")
                    getCurrentLoc()
                }

                override fun onConnectionSuspended(p0: Int) {
                    loge("suspended")
                }
            })
            .addOnConnectionFailedListener {
                listener.onFailed(it.errorMessage ?: it.errorCode.toString())
                try {
                    // Start an Activity that tries to resolve the error
                    it.startResolutionForResult(context, 90000)
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }
            .addApi(LocationServices.API)
            .build()

        mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (!mGoogleApiClient.isConnected) mGoogleApiClient.connect()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val result = LocationServices.SettingsApi.checkLocationSettings(
            mGoogleApiClient, builder.build()
        )

        result.setResultCallback {
            val status = it.status
            val state = it.locationSettingsStates
            when (it.status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    // All location settings are satisfied. The client can
                    // initialize location requests here.
                    getCurrentLoc()
                }

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(context, 1000)
                    } catch (e: SendIntentException) {
                        // Ignore the error.
                    }
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    // Location settings are not satisfied. However, we have no way
                    // to fix the settings so we won't show the dialog.
                    listener.onFailed("Location settings are not satisfied")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLoc() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        loge("get loc")
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                loge("last loc")
                listener.onGotLocation(it)
                getAddressByGeo(it.latitude, it.longitude)
            } ?: run {
                requestLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        loge("request loc")
        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                loge("Result " + locationResult.locations.size)
                try {
                    for (location in locationResult.locations) {
                        location?.let {
                            loge("new loc $location")
                            listener.onGotLocation(it)
                            getAddressByGeo(it.latitude, it.longitude)
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        Handler().post {
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1000 -> when (resultCode) {
                Activity.RESULT_OK -> // All required changes were successfully made
                    getLocation()

                Activity.RESULT_CANCELED -> // The user was asked to change settings, but chose not to
                    listener.onFailed("Location Service not Enabled")

                else -> {
                }
            }
        }
    }

    private fun getAddressByGeo(lat: Double, lng: Double) {
        loge("get address")
        try {
            geocoder.getFromLocation(lat, lng, 5)?.apply {
                context.runOnUiThread {
                    listener.onGotAddresses(this)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loge(msg: String) {
        Log.w("GetCurrentLocationManager", msg)
    }
}

interface CurrentLocationDatasListener {
    fun onGotLocation(location: Location)
    fun onGotAddresses(addresses: List<Address>)
    fun onFailed(message: String)
}
