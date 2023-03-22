package com.example.tourismapp.data.local.dao

import android.graphics.Bitmap
import androidx.room.*
import com.example.tourismapp.data.local.entities.RouteData
import com.example.tourismapp.presentation.services.Polylines
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

    @Query("UPDATE route_data SET route_distance = :routeDistance, route_average_speed = :routeAverageSpeed, route_time = :routeTime, end_date = :endDate, route_path = :routePath WHERE route_name = :routeName")
    suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routePath: Polylines,
        routeName: String
    )

    @Query("UPDATE route_data SET bmp = :routePicture WHERE route_name = :routeName")
    suspend fun addRoutePicture(
        routePicture: Bitmap,
        routeName: String
    )

    @Query("UPDATE route_data SET route_is_finished = :routeStatus WHERE route_name = :routeName")
    suspend fun finishRoute(
        routeStatus: Boolean,
        routeName: String
    )

    @Query("UPDATE route_data SET route_description = :routeDescription WHERE route_name = :routeName")
    suspend fun addRouteDescription(routeDescription: String, routeName: String)
}