package com.example.permissionsapp.data.repository.repository_local


import com.example.permissionsapp.data.local.dao.RouteDao
import com.example.permissionsapp.data.local.entities.RouteData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RepositoryRouteLocal @Inject constructor(
    private val routeDao: RouteDao
) {

    suspend fun insertRoute(routeData: RouteData){
        routeDao.insertRoute(routeData)
    }

    suspend fun getAllRoutes(): List<RouteData>{
        return routeDao.getAllRoutes()
    }

    fun getRouteByName(routeName: String): Flow<List<RouteData>> {
        return routeDao.getRouteByName(routeName)
    }

    suspend fun deleteRouteByName (routeName: String){
        routeDao.deleteRouteByName(routeName)
    }

    suspend fun addRouteDescription(routeDescription: String, routeName: String){
        routeDao.addRouteDescription(routeDescription, routeName)
    }

    suspend fun updateRouteEndDate(endDate: String, routeName: String){
        routeDao.updateRouteEndDate(endDate, routeName)
    }
}
