package com.example.permissionsapp.domain

import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.local.entities.PlacesForSearch
import com.example.permissionsapp.data.repository.RepositoryLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCaseLocal @Inject constructor(
    private val repositoryLocal: RepositoryLocal
) {

    fun getPlacesKindsFromDb(): Flow<List<PlacesForSearch>> {
        return repositoryLocal.getPlacesKindsFromDb()
    }

    suspend fun insertPlacesKindsToDb(placesForSearch: PlacesForSearch) {
        repositoryLocal.insertPlacesKindsToDb(placesForSearch)
    }

    suspend fun deletePlaceKindFromDb(placeKind: String) {
       repositoryLocal.deletePlaceKindFromDb(placeKind)
    }

    suspend fun deleteAllPlacesKinds(){
        repositoryLocal.deleteAllPlacesKinds()
    }


    suspend fun getPhotosByRouteName(routeName: String): List<PhotoData> {
        return repositoryLocal.getPhotosByRouteName(routeName)
    }

    fun getRoutesFromDb(): Flow<List<PhotoData>> {
        return repositoryLocal.getRoutesFromDb()
    }

    suspend fun insertPhotos(photoData: PhotoData) {
        repositoryLocal.insertPhotosToDb(photoData)
    }

    suspend fun addPhotoDescription(descriptionText: String?, uri:String){
        repositoryLocal.addPhotoDescription(descriptionText =descriptionText , uri = uri)
    }

    suspend fun deletePhoto(uri: String) {
        repositoryLocal.deleteFromDb(uri)
    }

    fun getAllObjectInfo(): Flow<List<ObjectInfo>> {
        return repositoryLocal.getAllObjectsFromDb()
    }

    suspend fun getObjectByIdInfo(xid: String): ObjectInfo {
        return repositoryLocal.getObjectByIdInfoFromDb(xid)
    }

    suspend fun insertObjectInfo(objectInfo: ObjectInfo) {
        return repositoryLocal.insertObjectInfoToDb(objectInfo)
    }
}