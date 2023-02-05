package com.example.permissionsapp.presentation.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.example.permissionsapp.ui.main.maps.MapsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



class DefaultLocationClient (
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if (!context.hasLocationPermission()) {
                throw LocationClient.LocationException("Missing location permission")
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException(message = "GPS is disabled")
            }

            val request = LocationRequest.Builder(interval)
                .setIntervalMillis(interval)
                .build()

            val locationCallback = object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    result.lastLocation.let { location ->
                        launch {
                            if (location != null) {
                                send(location)
                            }
                        }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}