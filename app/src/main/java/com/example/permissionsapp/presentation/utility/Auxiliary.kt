package com.example.permissionsapp.presentation.utility

import android.location.Location
import android.util.Log
import com.example.permissionsapp.presentation.services.Polyline
import com.example.permissionsapp.presentation.services.Polylines
import kotlin.math.round


private const val TAG = "AUX"

object Auxiliary {

    private fun calculatePolylineLength(polyline: Polyline): Float {
        var distance = 0F
        for (i in 0..polyline.size - 2) {
            val position1 = polyline[i]
            val position2 = polyline[i + 1]

            val results = FloatArray(1)
            Location.distanceBetween(
                position1.latitude,
                position1.longitude,
                position2.latitude,
                position2.longitude,
                results
            )
            distance += results[0]
        }
        return distance
    }

    fun calculateRouteDistance(routePath: Polylines): Float {
        var totalDistance = 0F
        for (polyline in routePath) {
            val polylineLength = calculatePolylineLength(polyline)
            totalDistance += polylineLength
        }
        return totalDistance
    }

    fun calculateAverageSpeed(time: Long, distance: Float): Float {
        val result = round((distance.toInt() / 1000F) / (time / 1000F / 60 / 60) * 10) / 10F
        Log.d(TAG, "RESULT IS : $result")
        return result
    }

}