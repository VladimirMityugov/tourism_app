package com.example.permissionsapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.permissionsapp.data.local.entities.PlacesForSearch
import kotlinx.coroutines.flow.Flow


@Dao
interface PlacesKindsDao {

 @Insert (onConflict = OnConflictStrategy.REPLACE)
 suspend fun insertPlaces(vararg placesForSearch: PlacesForSearch)

 @Query("SELECT * FROM placesForSearch")
 fun getAllPlaces():Flow<List<PlacesForSearch>>

 @Query("DELETE FROM placesForSearch WHERE kind = :placeKind")
 suspend fun deletePlace(placeKind: String)

 @Query("DELETE FROM placesForSearch")
 suspend fun deleteAllPlacesKinds()
}