package com.example.permissionsapp.domain.use_case_local

import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.repositories.repository_local.RepositoryPhotoLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCasePhotoLocal @Inject constructor(
    private val repositoryPhotoLocal: RepositoryPhotoLocal
) {
    suspend fun addPhotoDescription(descriptionText: String?, uri:String){
        repositoryPhotoLocal.addPhotoDescription(descriptionText =descriptionText , uri = uri)
    }

 fun getPhotosByRouteName(routeName: String): Flow<List<PhotoData>> {
        return repositoryPhotoLocal.getPhotosByRouteName(routeName)
    }

    fun getAllRoutesPhotosFromDb(): Flow<List<PhotoData>> {
        return repositoryPhotoLocal.getAllRoutesPhotosFromDb()
    }

    suspend fun insertPhotosToDb(photoData: PhotoData) {
        repositoryPhotoLocal.insertPhotosToDb(photoData)
    }

    suspend fun deletePhotoFromDb(uri: String) {
        repositoryPhotoLocal.deletePhotoFromDb(uri)
    }

    suspend fun deletePhotosByRouteName(routeName: String){
        repositoryPhotoLocal.deletePhotosByRouteName(routeName)
    }
}