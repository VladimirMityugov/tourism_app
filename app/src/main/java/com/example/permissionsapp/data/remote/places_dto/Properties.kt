package com.example.permissionsapp.data.remote.places_dto

data class Properties(
    val kinds: String,
    val name: String,
    val osm: String,
    val rate: Int,
    val wikidata: String,
    val xid: String
)