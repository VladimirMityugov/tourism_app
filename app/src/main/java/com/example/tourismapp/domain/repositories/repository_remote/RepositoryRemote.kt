package com.example.tourismapp.domain.repositories.repository_remote

import com.example.tourismapp.domain.models.remote.places_info_model.PlaceInfoModel
import com.example.tourismapp.domain.models.remote.places_model.PlacesModel


interface RepositoryRemote {

    suspend fun getPlacesAround(
        language: String,
        radius: Int,
        longitude: Double,
        latitude: Double,
        kinds: List<String>?,
        placeName: String?
    ): PlacesModel

    suspend fun getPlaceInfo(language: String, xid: String): PlaceInfoModel


}