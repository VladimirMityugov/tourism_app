package com.example.permissionsapp.data.local.dao

import androidx.room.*
import com.example.permissionsapp.data.local.entities.RouteData
import kotlinx.coroutines.flow.Flow


@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(vararg routeData: RouteData)

    @Query("SELECT * FROM route_data")
    suspend fun getAllRoutes(): List<RouteData>

    @Query("SELECT * FROM route_data WHERE route_name = :routeName")
   fun getRouteByName(routeName: String): Flow<List<RouteData>>

    @Query("DELETE FROM route_data WHERE route_name = :routeName")
    suspend fun deleteRouteByName(routeName: String)

    @Query("UPDATE route_data SET route_description = :routeDescription WHERE route_name = :routeName")
    suspend fun addRouteDescription(routeDescription: String, routeName: String)

    @Query("UPDATE route_data SET end_date = :endDate WHERE route_name = :routeName")
    suspend fun updateRouteEndDate(endDate: String, routeName: String)
}