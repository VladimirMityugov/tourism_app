package com.example.permissionsapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import javax.inject.Inject


@Entity (tableName = "placesForSearch")
data class PlacesForSearch @Inject constructor(
    @PrimaryKey
    @ColumnInfo
    val kind: String
        )