package com.example.tourismapp.data.remote.mappers_remote

import com.example.tourismapp.data.remote.places_dto.Feature
import com.example.tourismapp.data.remote.places_dto.Places
import com.example.tourismapp.data.remote.places_dto.Properties
import com.example.tourismapp.domain.models.remote.places_model.FeatureM
import com.example.tourismapp.domain.models.remote.places_model.PlacesModel
import com.example.tourismapp.domain.models.remote.places_model.PropertiesM

class PlacesMapper {

    fun toPlacesModel(places: Places): PlacesModel {
        return PlacesModel(
            featuresM = places.features.map { toFeatureM(it) },
            type = places.type
        )
    }

    private fun toFeatureM(feature: Feature): FeatureM {
        return FeatureM(
            id = feature.id,
            propertiesM = toPropertiesM(feature.properties),
            type = feature.type
        )

    }

    private fun toPropertiesM(properties: Properties): PropertiesM {
        return PropertiesM(
            kinds = properties.kinds,
            name = properties.name,
            osm = properties.osm,
            rate = properties.rate,
            wikidata = properties.wikidata,
            xid = properties.xid
        )
    }

    fun fromPlacesModel(placesModel: PlacesModel): Places {
        return Places(
            features = placesModel.featuresM.map { fromFeatureM(it) },
            type = placesModel.type
        )
    }

    private fun fromFeatureM(featureM: FeatureM): Feature {
        return Feature(
            id = featureM.id,
            properties = fromPropertiesM(featureM.propertiesM),
            type = featureM.type
        )

    }

    private fun fromPropertiesM(propertiesM: PropertiesM): Properties {
        return Properties(
            kinds = propertiesM.kinds,
            name = propertiesM.name,
            osm = propertiesM.osm,
            rate = propertiesM.rate,
            wikidata = propertiesM.wikidata,
            xid = propertiesM.xid
        )
    }

}