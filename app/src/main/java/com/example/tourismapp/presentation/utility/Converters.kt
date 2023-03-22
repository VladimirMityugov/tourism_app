package com.example.tourismapp.presentation.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.room.TypeConverter
import com.example.tourismapp.presentation.services.Polyline
import com.example.tourismapp.presentation.services.Polylines
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayOutputStream

private const val TAG = "CONVERTERS"

class Converters {

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap?): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toPolylines(value: String): Polylines {
        val result = mutableListOf<Polyline>()
        if (value.isNotBlank()) {
            val polyline = value.split("||")
            val intermediateResult = mutableListOf<LatLng>()
            polyline.dropLast(1)
            polyline.forEach { polyline ->
                val coordinates = polyline.split("|")
                Log.d(TAG, "CONVERTED COORDINATES: $coordinates")
                if(coordinates.isNotEmpty()){
                    for (i in coordinates.indices step 2) {
                        Log.d(TAG, "INDEX: $i")
                        val lat = coordinates[i].toDouble()
                        val lng = coordinates[i + 1].toDouble()
                        intermediateResult.add(LatLng(lat, lng))
                        Log.d(TAG, "INTERMEDIATE RESULT: $intermediateResult")
                    }
                }
                result.add(intermediateResult)
            }
            Log.d(TAG, "INTERMEDIATE RESULT: $intermediateResult")
        }
        Log.d(TAG, "RESULT is : $result")
        return result
    }

    @TypeConverter
    fun fromPolylines(polylines: Polylines?): String {
        val result = StringBuilder()
        if (polylines != null) {
            for (polyline in polylines) {
                val intermediateResult = StringBuilder()
                polyline.forEachIndexed { index, latLng ->
                    val lastElement = polyline.last()
                    val lastIndex = polyline.indexOf(lastElement)
                    if (index != lastIndex) {
                        intermediateResult.append(latLng.latitude).append("|")
                            .append(latLng.longitude).append("|")
                    } else {
                        intermediateResult.append(latLng.latitude).append("|")
                            .append(latLng.longitude)
                    }
                }
                result.append(intermediateResult).append("||")
            }
        }
        return result.toString()
    }

}