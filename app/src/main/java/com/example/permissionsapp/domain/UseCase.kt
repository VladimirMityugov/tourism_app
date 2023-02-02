package com.example.permissionsapp.domain

import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.remote.places_info_dto.PlaceInfo
import com.example.permissionsapp.data.remote.places_dto.Places
import com.example.permissionsapp.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCase @Inject constructor(private val repository: Repository) {


    fun getPhotos(): Flow<List<PhotoData>> {
        return repository.getPhotosFromDb()
    }

    suspend fun insertPhotos(photoData: PhotoData) {
        repository.insertPhotosToDb(photoData)
    }

    suspend fun deletePhoto(uri: String) {
        repository.deleteFromDb(uri)
    }

    fun getObjectInfo():Flow<List<ObjectInfo>> {
        return repository.getObjectInfoFromDb()
    }

    suspend fun getObjectByIdInfo(xid:String): ObjectInfo {
        return repository.getObjectByIdInfoFromDb(xid)
    }

    suspend fun insertObjectInfo(objectInfo: ObjectInfo) {
        return repository.insertObjectInfoToDb(objectInfo)
    }

    suspend fun getMuseumsAroundUseCase(longitude: Double, latitude: Double): Places {
        return repository.getMuseumsAround(longitude, latitude)
    }

    suspend fun getMuseumsInfoUseCase(xid: String): PlaceInfo {
        return repository.getMuseumsInfo(xid)
    }

}