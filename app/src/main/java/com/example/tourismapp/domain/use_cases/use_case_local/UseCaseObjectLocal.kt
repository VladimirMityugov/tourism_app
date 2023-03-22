package com.example.tourismapp.domain.use_cases.use_case_local

import com.example.tourismapp.domain.models.local.ObjectInfoModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryObjectLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class UseCaseObjectLocal @Inject constructor(
    private val repositoryObjectLocal: RepositoryObjectLocal
) {
    fun getAllObjectsFromDb(): Flow<List<ObjectInfoModel>> {
        return repositoryObjectLocal.getAllObjectsFromDb()
    }

    suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfoModel {
        return repositoryObjectLocal.getObjectByIdInfoFromDb(xid)
    }

    suspend fun insertObjectInfoToDb(objectInfoModel: ObjectInfoModel) {
        return repositoryObjectLocal.insertObjectInfoToDb(objectInfoModel)
    }

}