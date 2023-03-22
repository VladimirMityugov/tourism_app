package com.example.tourismapp.domain.models.remote.places_info_model



data class PlaceInfoModel(
    val addressM: AddressM?,
    val image: String?,
    val infoM: InfoM?,
    val kinds: String?,
    val name: String,
    val osm: String?,
    val otm: String?,
    val rate: String?,
    val xid: String
)