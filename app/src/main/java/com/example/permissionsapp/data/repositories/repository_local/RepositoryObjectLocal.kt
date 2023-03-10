package com.example.permissionsapp.data.repositories.repository_local


import com.example.permissionsapp.data.local.dao.ObjectDao
import com.example.permissionsapp.data.local.entities.ObjectInfo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryObjectLocal @Inject constructor(
    private val objectDao: ObjectDao
) {
    fun getAllObjectsFromDb(): Flow<List<ObjectInfo>> {
        return objectDao.getAllObjects()
    }

    suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfo {
        return objectDao.getObjectById(xid)
    }

    suspend fun insertObjectInfoToDb(objectInfo: ObjectInfo) {
        return objectDao.insertObjectInfo(objectInfo)
    }
}
