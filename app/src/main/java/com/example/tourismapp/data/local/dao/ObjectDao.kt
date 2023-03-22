package com.example.tourismapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tourismapp.data.local.entities.ObjectInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ObjectDao {

    @Query("SELECT * FROM objectInfo")
    fun getAllObjects():Flow<List<ObjectInfo>>

    @Query("SELECT * FROM objectInfo WHERE xid =:xid")
    suspend fun getObjectById(xid:String): ObjectInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjectInfo (vararg objectInfo: ObjectInfo)

}