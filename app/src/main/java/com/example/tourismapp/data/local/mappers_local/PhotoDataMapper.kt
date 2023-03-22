package com.example.tourismapp.data.local.mappers_local

import com.example.tourismapp.data.local.entities.PhotoData
import com.example.tourismapp.domain.models.local.PhotoDataModel

class PhotoDataMapper {

    fun toPhotoDataModel(photoData: PhotoData): PhotoDataModel {
        return PhotoDataModel(
            date = photoData.date,
            pic_src = photoData.pic_src,
            description = photoData.description,
            latitude = photoData.latitude,
            longitude = photoData.longitude,
            routeName = photoData.routeName
        )
    }

    fun fromPhotoDataModel(photoDataModel: PhotoDataModel): PhotoData {
        return PhotoData(
            date = photoDataModel.date,
            pic_src = photoDataModel.pic_src,
            description = photoDataModel.description,
            latitude = photoDataModel.latitude,
            longitude = photoDataModel.longitude,
            routeName = photoDataModel.routeName
        )
    }

}