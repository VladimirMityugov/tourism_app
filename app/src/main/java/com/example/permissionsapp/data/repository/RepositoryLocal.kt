package com.example.permissionsapp.data.repository


import com.example.permissionsapp.data.local.dao.ObjectDao
import com.example.permissionsapp.data.local.dao.PhotoDao
import com.example.permissionsapp.data.local.dao.PlacesKindsDao
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.local.entities.PlacesForSearch
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryLocal @Inject constructor(
    private val dao: PhotoDao,
    private val objectDao: ObjectDao,
    private val placesKindsDao: PlacesKindsDao
) {

    suspend fun addPhotoDescription(descriptionText: String?, uri:String){
        dao.insertPhotoDescription(descriptionText =descriptionText , uri = uri)
    }

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

    suspend fun getPhotosByRouteName(routeName: String): List<PhotoData> {
        return dao.getPhotosByRouteName(routeName)
    }

    fun getRoutesFromDb(): Flow<List<PhotoData>> {
        return dao.getAllRoutes()
    }

    suspend fun insertPhotosToDb(photoData: PhotoData) {
        dao.insertPhotos(photoData)
    }

    suspend fun deleteFromDb(uri: String) {
        dao.deletePhoto(uri)
    }

    fun getAllObjectsFromDb(): Flow<List<ObjectInfo>> {
        return objectDao.getAllObjects()
    }

    suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfo {
        return objectDao.getObjectById(xid)
    }

    suspend fun insertObjectInfoToDb(objectInfo: ObjectInfo) {
        return objectDao.insertObjectInfo(objectInfo)
    }
}
