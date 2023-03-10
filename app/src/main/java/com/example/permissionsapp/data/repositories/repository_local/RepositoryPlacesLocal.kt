package com.example.permissionsapp.data.repositories.repository_local


import com.example.permissionsapp.data.local.dao.PlacesKindsDao
import com.example.permissionsapp.data.local.entities.PlacesForSearch
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryPlacesLocal @Inject constructor(
    private val placesKindsDao: PlacesKindsDao
) {

    fun getPlacesKindsFromDb(): Flow<List<PlacesForSearch>> {
        return placesKindsDao.getAllPlaces()
    }

    suspend fun insertPlacesKindsToDb(placesForSearch: PlacesForSearch) {
        placesKindsDao.insertPlaces(placesForSearch)
    }

    suspend fun deletePlaceKindFromDb(placeKind: String) {
        placesKindsDao.deletePlace(placeKind)
    }

    suspend fun deleteAllPlacesKinds(){
        placesKindsDao.deleteAllPlacesKinds()
    }
}
