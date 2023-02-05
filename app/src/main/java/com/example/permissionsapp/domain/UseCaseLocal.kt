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


    fun getPhotos(): Flow<List<PhotoData>> {
        return repositoryLocal.getPhotosFromDb()
    }

    suspend fun insertPhotos(photoData: PhotoData) {
        repositoryLocal.insertPhotosToDb(photoData)
    }

    suspend fun deletePhoto(uri: String) {
        repositoryLocal.deleteFromDb(uri)
    }

    fun getObjectInfo(): Flow<List<ObjectInfo>> {
        return repositoryLocal.getObjectInfoFromDb()
    }

    suspend fun getObjectByIdInfo(xid: String): ObjectInfo {
        return repositoryLocal.getObjectByIdInfoFromDb(xid)
    }

    suspend fun insertObjectInfo(objectInfo: ObjectInfo) {
        return repositoryLocal.insertObjectInfoToDb(objectInfo)
    }
}