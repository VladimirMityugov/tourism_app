package com.example.permissionsapp.domain.use_case_local

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

    suspend fun getAllRoutes(): List<RouteData>{
        return repositoryRouteLocal.getAllRoutes()
    }

    fun getRouteByName(routeName: String): Flow<List<RouteData>> {
        return repositoryRouteLocal.getRouteByName(routeName)
    }

    suspend fun deleteRouteByName (routeName: String){
        repositoryRouteLocal.deleteRouteByName(routeName)
    }

    suspend fun addRouteDescription(routeDescription: String, routeName: String){
        repositoryRouteLocal.addRouteDescription(routeDescription, routeName)
    }

    suspend fun updateRouteEndDate(endDate: String, routeName: String){
        repositoryRouteLocal.updateRouteEndDate(endDate, routeName)
    }
}