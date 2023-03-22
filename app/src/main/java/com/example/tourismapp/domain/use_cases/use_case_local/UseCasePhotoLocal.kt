package com.example.tourismapp.domain.use_cases.use_case_local

import com.example.tourismapp.domain.models.local.PhotoDataModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryPhotoLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class UseCasePhotoLocal @Inject constructor(
    private val repositoryPhotoLocal: RepositoryPhotoLocal
) {
    suspend fun addPhotoDescription(descriptionText: String?, uri:String){
        repositoryPhotoLocal.addPhotoDescription(descriptionText =descriptionText , uri = uri)
    }

 fun getPhotosByRouteName(routeName: String): Flow<List<PhotoDataModel>> {
        return repositoryPhotoLocal.getPhotosByRouteName(routeName)
    }

    fun getAllRoutesPhotosFromDb(): Flow<List<PhotoDataModel>> {
        return repositoryPhotoLocal.getAllRoutesPhotosFromDb()
    }

    suspend fun insertPhotosToDb(photoDataModel: PhotoDataModel) {
        repositoryPhotoLocal.insertPhotosToDb(photoDataModel)
    }

    suspend fun deletePhotoFromDb(uri: String) {
        repositoryPhotoLocal.deletePhotoFromDb(uri)
    }

    suspend fun deletePhotosByRouteName(routeName: String){
        repositoryPhotoLocal.deletePhotosByRouteName(routeName)
    }
}