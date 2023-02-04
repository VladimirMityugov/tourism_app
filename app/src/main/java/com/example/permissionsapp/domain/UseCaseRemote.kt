package com.example.permissionsapp.domain


import com.example.permissionsapp.data.remote.places_info_dto.PlaceInfo
import com.example.permissionsapp.data.remote.places_dto.Places
import com.example.permissionsapp.data.repository.RepositoryRemote
import javax.inject.Inject

class UseCaseRemote @Inject constructor(private val repositoryRemote: RepositoryRemote) {

    suspend fun getPlacesAround(
        language: String,
        radius: Int,
        longitude: Double,
        latitude: Double,
        kinds: List<String>?,
        rating: String?,
        placeName: String?
    ): Places {
        return repositoryRemote.getPlacesAround(
            language = language,
            radius = radius,
            longitude = longitude,
            latitude = latitude,
            kinds = kinds,
            rating = rating,
            placeName = placeName
        )
    }

    suspend fun getPlaceInfo(xid: String): PlaceInfo {
        return repositoryRemote.getPlaceInfo(xid)
    }

}