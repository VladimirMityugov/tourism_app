package com.example.permissionsapp.presentation.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.permissionsapp.presentation.services.Polyline
import com.example.permissionsapp.presentation.services.Polylines
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
        val result = round((distance / 1000F) / (time / 1000F / 60 / 60) * 10) / 10F
        Log.d(TAG, "RESULT IS : $result")
        return result
    }

    fun bitmapDescriptorFromVector(
        context: Context,
        vectorResId: Int
    ): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun getMarkerBitmapFromView(view: View): Bitmap? {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val width = view.measuredWidth
        val height = view.measuredHeight
        if (width <= 0 || height <= 0) {
            return null
        }
        view.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }



}