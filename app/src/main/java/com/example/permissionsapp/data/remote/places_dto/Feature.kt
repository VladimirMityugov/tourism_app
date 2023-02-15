package com.example.permissionsapp.data.remote.places_dto

import com.example.permissionsapp.data.remote.places_info_dto.Geometry

data class Feature(
    val geometry: Geometry,
    val id: String,
    val properties: Properties,
    val type: String
)