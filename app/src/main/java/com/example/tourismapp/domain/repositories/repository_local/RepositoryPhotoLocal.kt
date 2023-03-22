package com.example.tourismapp.domain.repositories.repository_local

import com.example.tourismapp.domain.models.local.PhotoDataModel
import kotlinx.coroutines.flow.Flow


interface RepositoryPhotoLocal {

    suspend fun addPhotoDescription(descriptionText: String?, uri: String)

    fun getPhotosByRouteName(routeName: String): Flow<List<PhotoDataModel>>

    fun getAllRoutesPhotosFromDb(): Flow<List<PhotoDataModel>>

    suspend fun insertPhotosToDb(photoDataModel: PhotoDataModel)

    suspend fun deletePhotoFromDb(uri: String)

    suspend fun deletePhotosByRouteName(routeName: String)

}
