package com.example.tourismapp.presentation.utility.clustering

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class PhotoMarker(
    val lat: Double,
    val lng: Double,
    val pic_src: String,
    val description: String,
    val routeName: String
): ClusterItem{

    override fun getPosition(): LatLng {
       return LatLng(lat, lng)
    }

    override fun getTitle(): String {
        return routeName
    }

    override fun getSnippet(): String {
        val snippet = buildString {
            if(description.length > 10){
                append(description.take(10))
                append("...")
            }else{
                append(description)
            }
        }
        return snippet
    }

    override fun getZIndex(): Float? {
      return null
    }

}
