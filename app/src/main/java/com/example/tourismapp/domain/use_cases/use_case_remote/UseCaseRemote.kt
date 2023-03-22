package com.example.tourismapp.domain.use_cases.use_case_remote


import com.example.tourismapp.domain.models.remote.places_info_model.PlaceInfoModel
import com.example.tourismapp.domain.models.remote.places_model.PlacesModel
import com.example.tourismapp.domain.repositories.repository_remote.RepositoryRemote
import javax.inject.Inject

class UseCaseRemote @Inject constructor(private val repositoryRemote: RepositoryRemote) {

    suspend fun getPlacesAround(
        language: String,
        radius: Int,
        longitude: Double,
        latitude: Double,
        kinds: List<String>?,
        placeName: String?
    ): PlacesModel {
        return repositoryRemote.getPlacesAround(
            language = language,
            radius = radius,
            longitude = longitude,
            latitude = latitude,
            kinds = kinds,
            placeName = placeName
        )
    }

    suspend fun getPlaceInfo(language: String, xid: String): PlaceInfoModel {
        return repositoryRemote.getPlaceInfo(language, xid)
    }

}