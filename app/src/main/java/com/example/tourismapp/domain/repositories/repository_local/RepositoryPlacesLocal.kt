package com.example.tourismapp.domain.repositories.repository_local


import com.example.tourismapp.domain.models.local.PlacesForSearchModel
import kotlinx.coroutines.flow.Flow


interface RepositoryPlacesLocal {

    fun getPlacesKindsFromDb(): Flow<List<PlacesForSearchModel>>

    suspend fun insertPlacesKindsToDb(placesForSearchModel: PlacesForSearchModel)

    suspend fun deletePlaceKindFromDb(placeKind: String)

    suspend fun deleteAllPlacesKinds()
}
