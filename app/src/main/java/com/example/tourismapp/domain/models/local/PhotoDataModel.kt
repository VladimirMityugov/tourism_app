package com.example.tourismapp.domain.models.local


data class PhotoDataModel(
    val date: String,
    val pic_src: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val routeName: String
)