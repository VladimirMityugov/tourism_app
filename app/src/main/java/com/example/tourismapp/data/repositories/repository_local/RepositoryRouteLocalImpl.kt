package com.example.tourismapp.data.repositories.repository_local


import android.graphics.Bitmap
import com.example.tourismapp.data.local.dao.RouteDao
import com.example.tourismapp.data.local.mappers_local.RouteDataMapper
import com.example.tourismapp.domain.models.local.RouteDataModel
import com.example.tourismapp.domain.repositories.repository_local.RepositoryRouteLocal
import com.example.tourismapp.presentation.services.Polylines
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RepositoryRouteLocalImpl @Inject constructor(
    private val routeDao: RouteDao
) : RepositoryRouteLocal {

    override suspend fun insertRoute(routeDataModel: RouteDataModel) {
        val mapper = RouteDataMapper()
        routeDao.insertRoute(mapper.fromRouteDataModel(routeDataModel))
    }

    override fun getAllRoutes(): Flow<List<RouteDataModel>> {
        val mapper = RouteDataMapper()
        return routeDao.getAllRoutes()
            .map { allRoutes -> allRoutes.map { singleRoute -> mapper.toRouteDataModel(singleRoute) } }
    }

    override fun getRouteByName(routeName: String): Flow<RouteDataModel> {
        val mapper = RouteDataMapper()
        return routeDao.getRouteByName(routeName).map { route -> mapper.toRouteDataModel(route) }
    }

    override suspend fun deleteRouteByName(routeName: String) {
        routeDao.deleteRouteByName(routeName)
    }

    override suspend fun addRoutePicture(routePicture: Bitmap, routeName: String) {
        routeDao.addRoutePicture(routePicture, routeName)
    }

    override suspend fun addRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        endDate: String,
        routePath: Polylines,
        routeName: String
    ) {
        routeDao.addRouteData(
            routeDistance = routeDistance,
            routeAverageSpeed = routeAverageSpeed,
            routeTime = routeTime,
            endDate = endDate,
            routePath = routePath,
            routeName = routeName
        )
    }

    override suspend fun finishRoute(routeStatus: Boolean, routeName: String) {
        routeDao.finishRoute(routeStatus, routeName)
    }

    override suspend fun addRouteDescription(routeDescription: String, routeName: String) {
        routeDao.addRouteDescription(routeDescription, routeName)
    }
}
