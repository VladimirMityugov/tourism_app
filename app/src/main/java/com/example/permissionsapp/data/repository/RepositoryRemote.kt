package com.example.permissionsapp.data.repository

import android.util.Log
import com.example.permissionsapp.data.remote.places_info_dto.PlaceInfo
import com.example.permissionsapp.data.remote.places_dto.Places
import com.example.permissionsapp.data.remote.PlacesApi
import javax.inject.Inject

private const val TAG = "REPO"

class RepositoryRemote @Inject constructor(
    private val placesApi: PlacesApi
) {

    suspend fun getPlacesAround(
        language: String,
        radius: Int,
        longitude: Double,
        latitude: Double,
        kinds: List<String>?,
        placeName: String?
    ): Places {
        return placesApi.getPlacesAround(
            language = language,
            radius = radius,
            longitude = longitude,
            latitude = latitude,
            kinds = kinds,
            placeName = placeName
        )
    }

    suspend fun getPlaceInfo(language: String, xid: String): PlaceInfo {
        Log.d(TAG, "xid = $xid")
        return placesApi.getPlaceInfo(
            language=language,
            xid = xid
        )
    }
}