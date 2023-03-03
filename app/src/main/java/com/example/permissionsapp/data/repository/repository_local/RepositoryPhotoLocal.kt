package com.example.permissionsapp.data.repository.repository_local

import com.example.permissionsapp.data.local.dao.PhotoDao
import com.example.permissionsapp.data.local.entities.PhotoData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryPhotoLocal @Inject constructor(
    private val photoDao: PhotoDao,
) {
    suspend fun addPhotoDescription(descriptionText: String?, uri:String){
        photoDao.insertPhotoDescription(descriptionText =descriptionText , uri = uri)
    }

   fun getPhotosByRouteName(routeName: String): Flow<List<PhotoData>> {
        return photoDao.getPhotosByRouteName(routeName)
    }

    fun getAllRoutesPhotosFromDb(): Flow<List<PhotoData>> {
        return photoDao.getAllRoutes()
    }

    suspend fun insertPhotosToDb(photoData: PhotoData) {
        photoDao.insertPhotos(photoData)
    }

    suspend fun deletePhotoFromDb(uri: String) {
        photoDao.deletePhoto(uri)
    }

    suspend fun deletePhotosByRouteName(routeName: String){
        photoDao.deletePhotosByRouteName(routeName)
    }
}
