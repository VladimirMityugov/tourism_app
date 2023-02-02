package com.example.permissionsapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.inject.Inject


@Entity(tableName = "objectInfo")
data class ObjectInfo @Inject constructor(
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
    val road: String?
)
