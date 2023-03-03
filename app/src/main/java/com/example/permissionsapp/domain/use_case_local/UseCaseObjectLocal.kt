package com.example.permissionsapp.domain.use_case_local

import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.repository.repository_local.RepositoryObjectLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCaseObjectLocal @Inject constructor(
    private val repositoryObjectLocal: RepositoryObjectLocal
) {
    fun getAllObjectsFromDb(): Flow<List<ObjectInfo>> {
        return repositoryObjectLocal.getAllObjectsFromDb()
    }

    suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfo {
        return repositoryObjectLocal.getObjectByIdInfoFromDb(xid)
    }

    suspend fun insertObjectInfoToDb(objectInfo: ObjectInfo) {
        return repositoryObjectLocal.insertObjectInfoToDb(objectInfo)
    }

}