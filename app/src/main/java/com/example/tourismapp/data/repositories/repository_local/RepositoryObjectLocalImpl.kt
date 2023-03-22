package com.example.tourismapp.data.repositories.repository_local


import com.example.tourismapp.data.local.dao.ObjectDao
import com.example.tourismapp.data.local.mappers_local.ObjectInfoMapper
import com.example.tourismapp.domain.models.local.ObjectInfoModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryObjectLocal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepositoryObjectLocalImpl @Inject constructor(
    private val objectDao: ObjectDao
) : RepositoryObjectLocal {
    override fun getAllObjectsFromDb(): Flow<List<ObjectInfoModel>> {
        val mapper = ObjectInfoMapper()
        val result = objectDao.getAllObjects().map { allObjects ->
            allObjects.map { singleObject ->
                mapper.toObjectInfoModel(singleObject)
            }
        }
        return result
    }

    override suspend fun getObjectByIdInfoFromDb(xid: String): ObjectInfoModel {
        val mapper = ObjectInfoMapper()
        return mapper.toObjectInfoModel(objectDao.getObjectById(xid))
    }

    override suspend fun insertObjectInfoToDb(objectInfoModel: ObjectInfoModel) {
        val mapper = ObjectInfoMapper()
        objectDao.insertObjectInfo(mapper.fromObjectInfoModel(objectInfoModel))
    }
}
