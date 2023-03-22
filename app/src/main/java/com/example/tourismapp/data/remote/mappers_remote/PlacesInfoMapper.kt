package com.example.tourismapp.data.remote.mappers_remote

import com.example.tourismapp.data.remote.places_info_dto.Address
import com.example.tourismapp.data.remote.places_info_dto.Info
import com.example.tourismapp.data.remote.places_info_dto.PlaceInfo
import com.example.tourismapp.domain.models.remote.places_info_model.AddressM
import com.example.tourismapp.domain.models.remote.places_info_model.InfoM
import com.example.tourismapp.domain.models.remote.places_info_model.PlaceInfoModel

class PlacesInfoMapper {

    fun toPlaceInfoModel(placeInfo: PlaceInfo): PlaceInfoModel {
        return PlaceInfoModel(
            addressM = toAddressM(placeInfo.address),
            image = placeInfo.image,
            infoM = toInfoM(placeInfo.info),
            kinds = placeInfo.kinds,
            name = placeInfo.name,
            osm = placeInfo.osm,
            otm = placeInfo.otm,
            rate = placeInfo.rate,
            xid = placeInfo.xid
        )
    }

    private fun toAddressM(address: Address?): AddressM? {
        return if (address != null) {
            AddressM(
                country = address.country,
                country_code = address.country_code,
                county = address.county,
                house_number = address.house_number,
                postcode = address.postcode,
                road = address.road,
                state = address.state,
                town = address.town
            )
        } else null
    }

    private fun toInfoM(info: Info?): InfoM? {
        return if (info != null) {
            InfoM(
                descr = info.descr,
                image = info.image,
                img_height = info.img_height,
                img_width = info.img_width,
                src = info.src,
                src_id = info.src_id
            )
        } else null
    }

    fun fromPlaceInfoModel(placeInfoModel: PlaceInfoModel): PlaceInfo {
        return PlaceInfo(
            address = fromAddressM(placeInfoModel.addressM),
            image = placeInfoModel.image,
            info = fromInfoM(placeInfoModel.infoM),
            kinds = placeInfoModel.kinds,
            name = placeInfoModel.name,
            osm = placeInfoModel.osm,
            otm = placeInfoModel.otm,
            rate = placeInfoModel.rate,
            xid = placeInfoModel.xid
        )
    }

    private fun fromAddressM(addressM: AddressM?): Address? {
        return if (addressM != null) {
            Address(
                country = addressM.country,
                country_code = addressM.country_code,
                county = addressM.county,
                house_number = addressM.house_number,
                postcode = addressM.postcode,
                road = addressM.road,
                state = addressM.state,
                town = addressM.town
            )
        } else null
    }

    private fun fromInfoM(infoM: InfoM?): Info? {
        return if (infoM != null) {
            Info(
                descr = infoM.descr,
                image = infoM.image,
                img_height = infoM.img_height,
                img_width = infoM.img_width,
                src = infoM.src,
                src_id = infoM.src_id
            )
        } else null
    }


}