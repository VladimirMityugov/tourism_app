package com.example.tourismapp.domain.models.remote.places_model

data class PropertiesM(
    val kinds: String,
    val name: String,
    val osm: String,
    val rate: Int,
    val wikidata: String,
    val xid: String
)