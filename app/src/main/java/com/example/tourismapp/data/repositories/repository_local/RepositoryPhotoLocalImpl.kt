package com.example.tourismapp.data.repositories.repository_local

import com.example.tourismapp.data.local.dao.PhotoDao
import com.example.tourismapp.data.local.entities.PhotoData
import com.example.tourismapp.data.local.mappers_local.PhotoDataMapper
import com.example.tourismapp.data.local.mappers_local.RouteDataMapper
import com.example.tourismapp.domain.models.local.PhotoDataModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryPhotoLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepositoryPhotoLocalImpl @Inject constructor(
    private val photoDao: PhotoDao,
) : RepositoryPhotoLocal {
    override suspend fun addPhotoDescription(descriptionText: String?, uri: String) {
        photoDao.insertPhotoDescription(descriptionText = descriptionText, uri = uri)
    }

    override fun getPhotosByRouteName(routeName: String): Flow<List<PhotoDataModel>> {
        val mapper = PhotoDataMapper()
        return photoDao.getPhotosByRouteName(routeName)
            .map { photos -> photos.map { singlePhoto -> mapper.toPhotoDataModel(singlePhoto) } }
    }

    override fun getAllRoutesPhotosFromDb(): Flow<List<PhotoDataModel>> {
        val mapper = PhotoDataMapper()
        return photoDao.getAllRoutes()
            .map { photos -> photos.map { singlePhoto -> mapper.toPhotoDataModel(singlePhoto) } }
    }

    override suspend fun insertPhotosToDb(photoDataModel: PhotoDataModel) {
        val mapper = PhotoDataMapper()
        photoDao.insertPhotos(mapper.fromPhotoDataModel(photoDataModel))
    }

    override suspend fun deletePhotoFromDb(uri: String) {
        photoDao.deletePhoto(uri)
    }

    override suspend fun deletePhotosByRouteName(routeName: String) {
        photoDao.deletePhotosByRouteName(routeName)
    }
}
