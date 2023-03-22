package com.example.tourismapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "photoData")
data class PhotoData (
    @ColumnInfo
    val date: String,
    @PrimaryKey
    @ColumnInfo
    val pic_src: String,
    @ColumnInfo
    val description: String?,
    @ColumnInfo
    val latitude: Double,
    @ColumnInfo
    val longitude: Double,
    @ColumnInfo
    val routeName: String
        )