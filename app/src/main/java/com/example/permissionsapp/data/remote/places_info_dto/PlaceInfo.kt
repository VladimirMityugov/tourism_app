package com.example.permissionsapp.data.remote.places_info_dto

data class PlaceInfo(
    val address: Address?,
    val bbox: Bbox?,
    val image: String?,
    val info: Info?,
    val kinds: String?,
    val name: String,
    val osm: String?,
    val otm: String?,
    val point: Point?,
    val preview: Preview?,
    val rate: String?,
    val sources: Sources?,
    val xid: String
)