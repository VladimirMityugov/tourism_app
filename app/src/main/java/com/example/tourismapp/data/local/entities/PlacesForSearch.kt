package com.example.tourismapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "placesForSearch")
data class PlacesForSearch (
    @PrimaryKey
    @ColumnInfo
    val kind: String
        )