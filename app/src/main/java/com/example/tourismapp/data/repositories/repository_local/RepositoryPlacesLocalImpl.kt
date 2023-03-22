package com.example.tourismapp.data.repositories.repository_local


import com.example.tourismapp.data.local.dao.PlacesKindsDao
import com.example.tourismapp.data.local.mappers_local.PlacesForSearchMapper
import com.example.tourismapp.domain.models.local.PlacesForSearchModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryPlacesLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepositoryPlacesLocalImpl @Inject constructor(
    private val placesKindsDao: PlacesKindsDao
) : RepositoryPlacesLocal {

    override fun getPlacesKindsFromDb(): Flow<List<PlacesForSearchModel>> {
        val mapper = PlacesForSearchMapper()
        return placesKindsDao.getAllPlaces()
            .map { places -> places.map { place -> mapper.toPlacesForSearchModel(place) } }
    }

    override suspend fun insertPlacesKindsToDb(placesForSearchModel: PlacesForSearchModel) {
        val mapper = PlacesForSearchMapper()
        placesKindsDao.insertPlaces(mapper.fromPlacesForSearchModel(placesForSearchModel))
    }

    override suspend fun deletePlaceKindFromDb(placeKind: String) {
        placesKindsDao.deletePlace(placeKind)
    }

    override suspend fun deleteAllPlacesKinds() {
        placesKindsDao.deleteAllPlacesKinds()
    }
}
