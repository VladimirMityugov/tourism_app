package com.example.tourismapp.data.repositories.repository_remote

import com.example.tourismapp.data.remote.PlacesApi
import com.example.tourismapp.data.remote.mappers_remote.PlacesInfoMapper
import com.example.tourismapp.data.remote.mappers_remote.PlacesMapper
import com.example.tourismapp.domain.models.remote.places_info_model.PlaceInfoModel
import com.example.tourismapp.domain.models.remote.places_model.PlacesModel
import com.example.tourismapp.domain.repositories.repository_remote.RepositoryRemote
import javax.inject.Inject

private const val TAG = "REPO"

class RepositoryRemoteImpl @Inject constructor(
    private val placesApi: PlacesApi
) : RepositoryRemote {

    override suspend fun getPlacesAround(
        language: String,
        radius: Int,
        longitude: Double,
        latitude: Double,
        kinds: List<String>?,
        placeName: String?
    ): PlacesModel {
        val mapper = PlacesMapper()
        val result = placesApi.getPlacesAround(
            language = language,
            radius = radius,
            longitude = longitude,
            latitude = latitude,
            kinds = kinds,
            placeName = placeName
        )
        return mapper.toPlacesModel(result)

    }

    override suspend fun getPlaceInfo(language: String, xid: String): PlaceInfoModel {
        val mapper = PlacesInfoMapper()
        val result = placesApi.getPlaceInfo(
            language = language,
            xid = xid
        )
        return mapper.toPlaceInfoModel(result)
    }
}