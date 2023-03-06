package com.example.permissionsapp.data.local.dao

import android.graphics.Bitmap
import androidx.room.*
import com.example.permissionsapp.data.local.entities.RouteData
import kotlinx.coroutines.flow.Flow


@Dao
interface RouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(vararg routeData: RouteData)

    @Query("SELECT * FROM route_data")
    fun getAllRoutes(): Flow<List<RouteData>>

    @Query("SELECT * FROM route_data WHERE route_name = :routeName")
    fun getRouteByName(routeName: String): Flow<RouteData>

    @Query("DELETE FROM route_data WHERE route_name = :routeName")
    suspend fun deleteRouteByName(routeName: String)

    @Query("UPDATE route_data SET route_distance = :routeDistance, route_average_speed = :routeAverageSpeed, route_time = :routeTime, end_date = :endDate WHERE route_name = :routeName")
    suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routeName: String
    )

    @Query("UPDATE route_data SET bmp = :routePicture WHERE route_name = :routeName")
    suspend fun addRoutePicture(
        routePicture: Bitmap,
        routeName: String
    )

    @Query("UPDATE route_data SET route_description = :routeDescription WHERE route_name = :routeName")
    suspend fun addRouteDescription(routeDescription: String, routeName: String)
}