package com.example.tourismapp.domain.use_cases.use_case_local

import com.example.tourismapp.domain.models.local.PlacesForSearchModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryPlacesLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class UseCasePlacesLocal @Inject constructor(
    private val repositoryPlacesLocal: RepositoryPlacesLocal
) {
    fun getPlacesKindsFromDb(): Flow<List<PlacesForSearchModel>> {
        return repositoryPlacesLocal.getPlacesKindsFromDb()
    }

    suspend fun insertPlacesKindsToDb(placesForSearchModel: PlacesForSearchModel) {
        repositoryPlacesLocal.insertPlacesKindsToDb(placesForSearchModel)
    }

    suspend fun deletePlaceKindFromDb(placeKind: String) {
        repositoryPlacesLocal.deletePlaceKindFromDb(placeKind)
    }

    suspend fun deleteAllPlacesKinds(){
        repositoryPlacesLocal.deleteAllPlacesKinds()
    }
}