package com.example.tourismapp.domain.models.local


data class ObjectInfoModel(
    val xid: String,
    val name: String,
    val country_code: String?,
    val house_number: String?,
    val postcode: String?,
    val road: String?,
    val description: String?,
    val image: String?
)
