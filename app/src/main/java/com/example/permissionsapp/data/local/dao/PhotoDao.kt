package com.example.permissionsapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.permissionsapp.data.local.entities.PhotoData
import kotlinx.coroutines.flow.Flow


@Dao
interface PhotoDao {
 @Insert
 suspend fun insertPhotos(vararg photoData: PhotoData)

 @Query("SELECT * FROM photoData WHERE routeName =:routeName")
 fun getPhotosByRouteName(routeName: String): Flow<List<PhotoData>>

 @Query ("UPDATE photoData SET description = :descriptionText WHERE pic_src =:uri ")
 suspend fun insertPhotoDescription(descriptionText: String?,uri:String)

 @Query("DELETE FROM photoData WHERE pic_src = :uri")
 suspend fun deletePhoto(uri: String)

 @Query("DELETE FROM photoData WHERE routeName = :routeName")
 suspend fun deletePhotosByRouteName(routeName: String)

 @Query("SELECT * FROM photoData ORDER BY random()")
 fun getAllRoutes(): Flow<List<PhotoData>>

}