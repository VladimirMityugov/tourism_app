package com.example.tourismapp.domain.use_cases.use_case_local

import android.graphics.Bitmap
import com.example.tourismapp.domain.models.local.RouteDataModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryRouteLocal
import com.example.tourismapp.presentation.services.Polylines
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class UseCaseRouteLocal @Inject constructor(
    private val repositoryRouteLocal: RepositoryRouteLocal
) {
    suspend fun insertRoute(routeDataModel: RouteDataModel) {
        repositoryRouteLocal.insertRoute(routeDataModel)
    }

    fun getAllRoutes(): Flow<List<RouteDataModel>> {
        return repositoryRouteLocal.getAllRoutes()
    }

    fun getRouteByName(routeName: String): Flow<RouteDataModel> {
        return repositoryRouteLocal.getRouteByName(routeName)
    }

    suspend fun deleteRouteByName(routeName: String) {
        repositoryRouteLocal.deleteRouteByName(routeName)
    }

    suspend fun addRoutePicture(routePicture: Bitmap, routeName: String) {
        repositoryRouteLocal.addRoutePicture(routePicture, routeName)
    }

    suspend fun finishRoute(routeStatus: Boolean, routeName: String) {
        repositoryRouteLocal.finishRoute(routeStatus, routeName)
    }

    suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routePath: Polylines,
        routeName: String
    ) {
        repositoryRouteLocal.addRouteData(
            routeDistance = routeDistance,
            routeAverageSpeed = routeAverageSpeed,
            routeTime = routeTime,
            endDate = endDate,
            routePath = routePath,
            routeName = routeName
        )
    }

    suspend fun addRouteDescription(routeDescription: String, routeName: String) {
        repositoryRouteLocal.addRouteDescription(routeDescription, routeName)
    }
}