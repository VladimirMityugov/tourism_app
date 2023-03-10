package com.example.permissionsapp.domain.use_case_local

import com.example.permissionsapp.data.local.entities.PlacesForSearch
import com.example.permissionsapp.data.repositories.repository_local.RepositoryPlacesLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCasePlacesLocal @Inject constructor(
    private val repositoryPlacesLocal: RepositoryPlacesLocal
) {
    fun getPlacesKindsFromDb(): Flow<List<PlacesForSearch>> {
        return repositoryPlacesLocal.getPlacesKindsFromDb()
    }

    suspend fun insertPlacesKindsToDb(placesForSearch: PlacesForSearch) {
        repositoryPlacesLocal.insertPlacesKindsToDb(placesForSearch)
    }

    suspend fun deletePlaceKindFromDb(placeKind: String) {
        repositoryPlacesLocal.deletePlaceKindFromDb(placeKind)
    }

    suspend fun deleteAllPlacesKinds(){
        repositoryPlacesLocal.deleteAllPlacesKinds()
    }
}