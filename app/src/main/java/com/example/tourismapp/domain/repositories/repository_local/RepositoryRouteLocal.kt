package com.example.tourismapp.domain.repositories.repository_local


import android.graphics.Bitmap
import com.example.tourismapp.domain.models.local.RouteDataModel
import com.example.tourismapp.presentation.services.Polylines
import kotlinx.coroutines.flow.Flow


interface RepositoryRouteLocal {

    suspend fun insertRoute(routeDataModel: RouteDataModel)

    fun getAllRoutes(): Flow<List<RouteDataModel>>

    fun getRouteByName(routeName: String): Flow<RouteDataModel>

    suspend fun deleteRouteByName(routeName: String)

    suspend fun addRoutePicture(routePicture: Bitmap, routeName: String)

    suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routePath: Polylines,
        routeName: String
    )

    suspend fun finishRoute(routeStatus: Boolean, routeName: String)

    suspend fun addRouteDescription(routeDescription: String, routeName: String)
}
