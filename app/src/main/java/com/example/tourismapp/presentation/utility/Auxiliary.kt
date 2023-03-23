package com.example.tourismapp.presentation.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.example.tourismapp.R
import com.example.tourismapp.presentation.services.Polyline
import com.example.tourismapp.presentation.services.Polylines
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
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

    fun addAllPolylines(routePath: Polylines, map: GoogleMap) {
        for (polyline in routePath) {
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .addAll(polyline)
            map.addPolyline(polylineOptions)
        }
    }

    fun setMapStyle(map: GoogleMap, context: Context) {
        try {
            val success: Boolean = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context, R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    @SuppressLint("MissingPermission")
   fun setMapSettings(map: GoogleMap) {
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        with(map.uiSettings) {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
            isCompassEnabled = true
            isZoomGesturesEnabled = true
            isRotateGesturesEnabled = true
        }
    }

    fun zoomToSeeWholeTrack(routePath: List<Polyline>, map: GoogleMap, mapView: FragmentContainerView) {
        val bounds = LatLngBounds.builder()
        for (polyline in routePath) {
            for (coordinates in polyline) {
                bounds.include(coordinates)
            }
        }
        map.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05F).toInt()
            )
        )
    }



}