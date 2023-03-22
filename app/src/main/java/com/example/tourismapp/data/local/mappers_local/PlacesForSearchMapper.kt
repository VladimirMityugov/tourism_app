package com.example.tourismapp.data.local.mappers_local

import com.example.tourismapp.data.local.entities.PlacesForSearch
import com.example.tourismapp.domain.models.local.PlacesForSearchModel


class PlacesForSearchMapper {

    fun toPlacesForSearchModel(placesForSearch: PlacesForSearch): PlacesForSearchModel {
        return PlacesForSearchModel(
           kind = placesForSearch.kind
        )
    }

    fun fromPlacesForSearchModel(placesForSearchModel: PlacesForSearchModel): PlacesForSearch {
        return PlacesForSearch(
            kind = placesForSearchModel.kind
        )
    }

}