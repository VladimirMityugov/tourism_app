package com.example.permissionsapp.data.repository


import com.example.permissionsapp.data.local.dao.ObjectDao
import com.example.permissionsapp.data.local.dao.PhotoDao
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryLocal @Inject constructor(
    private val dao: PhotoDao,
    private val objectDao: ObjectDao
) {

    fun getPhotosFromDb(): Flow<List<PhotoData>> {
        return dao.getAllPhoto()
    }

    suspend fun insertPhotosToDb(photoData: PhotoData) {
        dao.insertPhotos(photoData)
    }

    suspend fun deleteFromDb(uri: String) {
        dao.deletePhoto(uri)
    }

    fun getObjectInfoFromDb(): Flow<List<ObjectInfo>> {
        return objectDao.getAllObjects()
    }

    suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfo {
        return objectDao.getObjectById(xid)
    }

    suspend fun insertObjectInfoToDb(objectInfo: ObjectInfo) {
        return objectDao.insertObjectInfo(objectInfo)
    }
}
