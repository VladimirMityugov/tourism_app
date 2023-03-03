package com.example.permissionsapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "route_data")
data class RouteData(
    @PrimaryKey
    @ColumnInfo
    val route_name: String,
    @ColumnInfo
    val route_description: String?,
    @ColumnInfo
    val start_date: String,
    @ColumnInfo
    val end_date: String
)



