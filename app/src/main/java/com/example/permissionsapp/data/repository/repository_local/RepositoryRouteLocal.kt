package com.example.permissionsapp.data.repository.repository_local


import android.graphics.Bitmap
import com.example.permissionsapp.data.local.dao.RouteDao
import com.example.permissionsapp.data.local.entities.RouteData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryRouteLocal @Inject constructor(
    private val routeDao: RouteDao
) {

    suspend fun insertRoute(routeData: RouteData) {
        routeDao.insertRoute(routeData)
    }

    fun getAllRoutes(): Flow<List<RouteData>> {
        return routeDao.getAllRoutes()
    }

    fun getRouteByName(routeName: String): Flow<RouteData> {
        return routeDao.getRouteByName(routeName)
    }

    suspend fun deleteRouteByName(routeName: String) {
        routeDao.deleteRouteByName(routeName)
    }

    suspend fun addRoutePicture(routePicture: Bitmap, routeName: String){
        routeDao.addRoutePicture(routePicture, routeName)
    }

    suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routeName: String
    ) {
        routeDao.addRouteData(
            routeDistance,
            routeAverageSpeed,
            routeTime,
            endDate,
            routeName
        )
    }

    suspend fun addRouteDescription(routeDescription: String, routeName: String) {
        routeDao.addRouteDescription(routeDescription, routeName)
    }
}
