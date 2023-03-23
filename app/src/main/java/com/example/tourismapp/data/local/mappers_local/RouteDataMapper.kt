package com.example.tourismapp.data.local.mappers_local

import com.example.tourismapp.data.local.entities.RouteData
import com.example.tourismapp.domain.models.local.RouteDataModel

class RouteDataMapper {

    fun toRouteDataModel(routeData: RouteData): RouteDataModel {
        return RouteDataModel(
            route_name = routeData.route_name,
            route_description = routeData.route_description,
            route_distance = routeData.route_distance,
            route_time = routeData.route_time,
            bmp = routeData.bmp,
            route_average_speed = routeData.route_average_speed,
            route_is_finished = routeData.route_is_finished,
            route_path = routeData.route_path,
            start_date = routeData.start_date,
            end_date = routeData.end_date
        )
    }

    fun fromRouteDataModel(routeDataModel: RouteDataModel): RouteData {
        return RouteData(
            route_name = routeDataModel.route_name,
            route_description = routeDataModel.route_description,
            route_distance = routeDataModel.route_distance,
            route_time = routeDataModel.route_time,
            bmp = routeDataModel.bmp,
            route_average_speed = routeDataModel.route_average_speed,
            route_is_finished = routeDataModel.route_is_finished,
            route_path = routeDataModel.route_path,
            start_date = routeDataModel.start_date,
            end_date = routeDataModel.end_date
        )
    }

}