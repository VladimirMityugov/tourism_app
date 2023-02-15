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

 @Query("SELECT * FROM photoData")
 fun getAllPhoto():Flow<List<PhotoData>>

 @Query ("UPDATE photoData SET description = :descriptionText WHERE pic_src =:uri ")
 suspend fun insertPhotoDescription(descriptionText: String?,uri:String)

 @Query("DELETE FROM photoData WHERE pic_src = :uri")
 suspend fun deletePhoto(uri: String)
}