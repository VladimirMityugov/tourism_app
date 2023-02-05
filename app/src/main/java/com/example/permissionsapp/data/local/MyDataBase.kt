package com.example.permissionsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.permissionsapp.data.local.dao.ObjectDao
import com.example.permissionsapp.data.local.dao.PhotoDao
import com.example.permissionsapp.data.local.dao.PlacesKindsDao
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.local.entities.PlacesForSearch


@Database (entities = [PhotoData::class, ObjectInfo::class, PlacesForSearch::class], version = 1)
abstract class MyDataBase: RoomDatabase() {

    abstract val dao: PhotoDao

    abstract val objectDao: ObjectDao

    abstract val placesKindsDao : PlacesKindsDao
}