package com.example.tourismapp.domain.models.local

import android.graphics.Bitmap
import com.example.tourismapp.presentation.services.Polylines


data class RouteDataModel(
    val id: Int = 0,
    val route_name: String,
    val route_description: String?,
    val route_distance: Float?,
    val route_average_speed: Float?,
    val route_time: Float?,
    var bmp: Bitmap? = null,
    val start_date: String,
    val end_date: String,
    var route_is_finished: Boolean = false,
    val route_path: Polylines? = null
)



