package com.example.permissionsapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.inject.Inject


@Entity (tableName = "photoData")
data class PhotoData @Inject constructor(
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
    val longitude: Double
        )