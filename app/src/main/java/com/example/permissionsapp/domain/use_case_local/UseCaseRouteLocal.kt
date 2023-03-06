package com.example.permissionsapp.domain.use_case_local

import android.graphics.Bitmap
import com.example.permissionsapp.data.local.entities.RouteData
import com.example.permissionsapp.data.repository.repository_local.RepositoryRouteLocal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCaseRouteLocal @Inject constructor(
    private val repositoryRouteLocal: RepositoryRouteLocal
) {
    suspend fun insertRoute(routeData: RouteData){
        repositoryRouteLocal.insertRoute(routeData)
    }

    fun getAllRoutes(): Flow<List<RouteData>>{
        return repositoryRouteLocal.getAllRoutes()
    }

    fun getRouteByName(routeName: String): Flow<RouteData> {
        return repositoryRouteLocal.getRouteByName(routeName)
    }

    suspend fun deleteRouteByName (routeName: String){
        repositoryRouteLocal.deleteRouteByName(routeName)
    }

    suspend fun addRoutePicture(routePicture: Bitmap, routeName: String){
        repositoryRouteLocal.addRoutePicture(routePicture, routeName)
    }

    suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routeName: String
    ) {
        repositoryRouteLocal.addRouteData(
            routeDistance,
            routeAverageSpeed,
            routeTime,
            endDate,
            routeName
        )
    }

    suspend fun addRouteDescription(routeDescription: String, routeName: String) {
        repositoryRouteLocal.addRouteDescription(routeDescription, routeName)
    }
}