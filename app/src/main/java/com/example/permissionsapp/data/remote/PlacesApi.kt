package com.example.permissionsapp.data.remote

import com.example.permissionsapp.data.remote.places_info_dto.PlaceInfo
import com.example.permissionsapp.data.remote.places_dto.Places
import retrofit2.http.*


interface PlacesApi {

    @GET("{lang}/places/radius?")
    suspend fun getPlacesAround(
        @Path("lang")language: String,
        @Query("radius")radius:Int,
        @Query("apikey") apikey: String = KEY,
        @Query("lon") longitude: Double,
        @Query("lat") latitude: Double,
        @Query("kinds")kinds:List<String>?,
        @Query("name")placeName: String?
    ): Places

    @GET("ru/places/xid/{xid}?")
    suspend fun getPlaceInfo(
        @Path("xid") xid: String,
        @Query ("apikey") apikey: String = KEY
    ): PlaceInfo


    companion object {
        const val BASE_URl = "https://api.opentripmap.com/0.1/"
        private const val KEY = "5ae2e3f221c38a28845f05b63caa60f954feaca8c9db81d9c2bbe64b"
    }
}




