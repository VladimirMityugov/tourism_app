package com.example.tourismapp.data.local.entities

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tourismapp.presentation.services.Polylines


@Entity(tableName = "route_data")
data class RouteData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo
    val id: Int = 0,
    @ColumnInfo
    val route_name: String,
    @ColumnInfo
    val route_description: String?,
    @ColumnInfo
    val route_distance: Float?,
    @ColumnInfo
    val route_average_speed: Float?,
    @ColumnInfo
    val route_time: Float?,
    @ColumnInfo
    var bmp: Bitmap? = null,
    @ColumnInfo
    val start_date: String,
    @ColumnInfo
    val end_date: String,
    @ColumnInfo
    var route_is_finished: Boolean = false,
    @ColumnInfo
    val route_path: Polylines? = null
)



