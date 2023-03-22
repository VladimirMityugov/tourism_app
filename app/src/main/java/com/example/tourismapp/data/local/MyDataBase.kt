package com.example.tourismapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tourismapp.data.local.dao.ObjectDao
import com.example.tourismapp.data.local.dao.PhotoDao
import com.example.tourismapp.data.local.dao.PlacesKindsDao
import com.example.tourismapp.data.local.dao.RouteDao
import com.example.tourismapp.data.local.entities.ObjectInfo
import com.example.tourismapp.data.local.entities.PhotoData
import com.example.tourismapp.data.local.entities.PlacesForSearch
import com.example.tourismapp.data.local.entities.RouteData
import com.example.tourismapp.presentation.utility.Converters


@Database(
    entities = [PhotoData::class, ObjectInfo::class, PlacesForSearch::class, RouteData::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MyDataBase : RoomDatabase() {

    abstract val photoDao: PhotoDao

    abstract val objectDao: ObjectDao

    abstract val placesKindsDao: PlacesKindsDao

    abstract val routeDao: RouteDao
}