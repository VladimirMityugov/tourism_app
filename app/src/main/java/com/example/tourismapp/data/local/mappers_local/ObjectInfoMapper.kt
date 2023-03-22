package com.example.tourismapp.data.local.mappers_local

import com.example.tourismapp.data.local.entities.ObjectInfo
import com.example.tourismapp.domain.models.local.ObjectInfoModel

class ObjectInfoMapper {

    fun toObjectInfoModel(objectInfo: ObjectInfo): ObjectInfoModel{
        return ObjectInfoModel(
            xid = objectInfo.xid,
            name = objectInfo.name,
            country_code = objectInfo.country_code,
            postcode = objectInfo.postcode,
            house_number = objectInfo.house_number,
            road = objectInfo.road,
            description = objectInfo.description,
            image = objectInfo.image
        )
    }

    fun fromObjectInfoModel(objectInfoModel: ObjectInfoModel): ObjectInfo{
        return ObjectInfo(
            xid = objectInfoModel.xid,
            name = objectInfoModel.name,
            country_code = objectInfoModel.country_code,
            postcode = objectInfoModel.postcode,
            house_number = objectInfoModel.house_number,
            road = objectInfoModel.road,
            description = objectInfoModel.description,
            image = objectInfoModel.image
        )
    }

}