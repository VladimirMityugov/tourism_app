package com.example.permissionsapp.data.repository

import android.util.Log
import com.example.permissionsapp.data.local.dao.ObjectDao
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.dao.PhotoDao
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.remote.places_info_dto.PlaceInfo
import com.example.permissionsapp.data.remote.places_dto.Places
import com.example.permissionsapp.data.remote.PlacesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val TAG = "REPO"

class Repository @Inject constructor(
    private val dao: PhotoDao,
    private val objectDao: ObjectDao,
    private val placesApi: PlacesApi
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

    suspend fun getObjectByIdInfoFromDb(xid:String): ObjectInfo {
        return objectDao.getObjectById(xid)
    }

    suspend fun insertObjectInfoToDb(objectInfo: ObjectInfo) {
        return objectDao.insertObjectInfo(objectInfo)
    }

    suspend fun getMuseumsAround(longitude: Double, latitude: Double): Places {
        return placesApi.getMuseumsAround(
            longitude = longitude,
            latitude = latitude
        )
    }

    suspend fun getMuseumsInfo(xid: String): PlaceInfo {
        Log.d(TAG, "xid = $xid")
        return placesApi.getMuseumsInfo(
            xid = xid
        )
    }
}