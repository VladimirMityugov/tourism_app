package com.example.tourismapp.data.remote.places_info_dto

data class PlaceInfo(
    val address: Address?,
    val image: String?,
    val info: Info?,
    val kinds: String?,
    val name: String,
    val osm: String?,
    val otm: String?,
    val rate: String?,
    val xid: String
)