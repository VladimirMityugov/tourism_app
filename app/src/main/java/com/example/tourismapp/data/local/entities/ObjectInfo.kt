package com.example.tourismapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "objectInfo")
data class ObjectInfo (
    @PrimaryKey
    @ColumnInfo
    val xid: String,
    @ColumnInfo
    val name: String,
    @ColumnInfo
    val country_code: String?,
    @ColumnInfo
    val house_number: String?,
    @ColumnInfo
    val postcode: String?,
    @ColumnInfo
    val road: String?,
    @ColumnInfo
    val description: String?,
    @ColumnInfo
    val image: String?
)
