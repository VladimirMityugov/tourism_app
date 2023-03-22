package com.example.tourismapp.domain.repositories.repository_local


import com.example.tourismapp.domain.models.local.ObjectInfoModel
import kotlinx.coroutines.flow.Flow


interface RepositoryObjectLocal {

    fun getAllObjectsFromDb(): Flow<List<ObjectInfoModel>>

    suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfoModel

    suspend fun insertObjectInfoToDb(objectInfoModel: ObjectInfoModel)
}
