package com.example.permissionsapp.presentation

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.remote.places_dto.Places
import com.example.permissionsapp.domain.UseCaseLocal
import com.example.permissionsapp.domain.UseCaseRemote
import com.example.permissionsapp.presentation.utility.DefaultLocationClient
import com.example.permissionsapp.presentation.utility.PlacesRating
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


private const val TAG = "VIEW_MODEL"

@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCaseRemote: UseCaseRemote,
    private val useCaseLocal: UseCaseLocal,
    private val locationClient: DefaultLocationClient
) : ViewModel() {

    private val _selectedItem = MutableStateFlow<PhotoData?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _places = MutableStateFlow<Places?>(null)
    val places = _places.asStateFlow()

    private val _currentLatitude = MutableStateFlow<Double?>(null)
    val currentLatitude = _currentLatitude.asStateFlow()

    private val _currentLongitude = MutableStateFlow<Double?>(null)
    val currentLongitude = _currentLongitude.asStateFlow()

    private val _museumInfo = MutableStateFlow<ObjectInfo?>(null)
    val museumInfo = _museumInfo.asStateFlow()

    private val _photos = MutableStateFlow<List<PhotoData>>(emptyList())
    val photos = _photos.asStateFlow()

    private val _isEmailEntered = MutableStateFlow(false)
    val isEmailEntered = _isEmailEntered.asStateFlow()

    private val _isPasswordEntered = MutableStateFlow(false)
    val isPasswordEntered = _isPasswordEntered.asStateFlow()

    private val _isNewEmailEntered = MutableStateFlow(false)
    val isNewEmailEntered = _isNewEmailEntered.asStateFlow()

    private val _isNewPasswordEntered = MutableStateFlow(false)
    val isNewPasswordEntered = _isNewPasswordEntered.asStateFlow()

    private val _isNameEntered = MutableStateFlow(false)
    val isNameEntered = _isNameEntered.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName = _currentUserName.asStateFlow()

    private val _placesRating = MutableStateFlow<PlacesRating?>(null)

    private val _radius = MutableStateFlow(5000)
    val radius = _radius.asStateFlow()

    private val _kinds = MutableStateFlow<List<String>>(emptyList())

    fun selectItem(item: PhotoData) {
        _selectedItem.value = item
    }

    init {
        this.viewModelScope.launch {
            getPhotoList()
        }
    }

    fun getPhotoList(): Flow<List<PhotoData>> = useCaseLocal.getPhotos()


    fun insertPhotos(uri: String, date: String, description: String?) {
        viewModelScope.launch {
            useCaseLocal.insertPhotos(
                photoData = PhotoData(
                    pic_src = uri,
                    date = date,
                    description = description
                )
            )
        }
    }

    fun deletePhoto(uri: String) {
        viewModelScope.launch {
            useCaseLocal.deletePhoto(uri)
        }
    }

    fun getCurrentLocation(interval: Long):Flow<Location> = locationClient.getLocationUpdates(interval)


    fun updateCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _currentLatitude.value = latitude
            _currentLongitude.value = longitude
        }
    }

    fun setRadius(value:Int){
        _radius.value = value*1000
    }

    fun getPlacesAround(
        longitude: Double,
        latitude: Double,
        placeName: String?
    ) {
        viewModelScope.launch {
            kotlin.runCatching {
                useCaseRemote.getPlacesAround(
                    language = getLanguage(),
                    radius = _radius.value,
                    longitude = longitude,
                    latitude = latitude,
                    kinds = _kinds.value,
                    rating = when (_placesRating.value) {
                        PlacesRating.LOW -> LOW
                        PlacesRating.HIGH -> HIGH
                        PlacesRating.HIGH_H -> HIGH_H
                        PlacesRating.LOW_H -> LOW_H
                        PlacesRating.MEDIUM -> MEDIUM
                        PlacesRating.MEDIUM_H -> MEDIUM_H
                        else -> null
                    },
                    placeName = placeName
                )
            }.fold(
                onSuccess = { places ->
                    if (places.features.isNotEmpty()) _places.value = places
                    else _places.value = null
                },
                onFailure = {
                    Log.d(TAG, "${it.message}")
                    _places.value = null
                }
            )
        }
    }

    fun setRating(placesRating: PlacesRating){
        _placesRating.value = placesRating
    }

    private fun getLanguage(): String {
        return if (Locale.getDefault() == Locale("ru", "RU")) RU
        else EN
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            _currentUserName.value = name
        }
    }

    fun switchEmailStatus(status: Boolean) {
        viewModelScope.launch {
            _isEmailEntered.value = status
        }
    }

    fun switchPasswordStatus(status: Boolean) {
        viewModelScope.launch {
            _isPasswordEntered.value = status
        }
    }

    fun switchRegistrationEmailStatus(status: Boolean) {
        viewModelScope.launch {
            _isNewEmailEntered.value = status
        }
    }

    fun switchRegistrationPasswordStatus(status: Boolean) {
        viewModelScope.launch {
            _isNewPasswordEntered.value = status
        }
    }

    fun switchNameStatus(status: Boolean) {
        viewModelScope.launch {
            _isNameEntered.value = status
        }
    }

    fun saveObjectsToDB(places: Places) {
        viewModelScope.launch {
            val placesAround = places.features
            if (placesAround.isNotEmpty() && placesAround.size >= 5) {
                Log.d(TAG, "museums around = $placesAround")
                for (i in 1..5) {
                    val it = placesAround[i]
                    val xid = it.properties.xid
                    Log.d(TAG, "xid of object to save into DB is $xid")
                    val objectInfo = useCaseRemote.getPlaceInfo(xid)
                    useCaseLocal.insertObjectInfo(
                        objectInfo = ObjectInfo(
                            xid = objectInfo.xid,
                            name = objectInfo.name,
                            country_code = objectInfo.address?.country_code,
                            house_number = objectInfo.address?.house_number,
                            postcode = objectInfo.address?.postcode,
                            road = objectInfo.address?.road
                        )
                    )
                    Log.d(
                        TAG,
                        "Data xid = ${objectInfo.xid}, name = ${objectInfo.name} for $xid has been saved to DB"
                    )
                }
            }
        }
    }

    private val allObjects = useCaseLocal.getObjectInfo()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(1000),
            initialValue = emptyList()
        )

    fun getObjectInfoById(xid: String) {
        viewModelScope.launch {
            if (allObjects.value.all { it.xid != xid }) {
                val placeInfo = useCaseRemote.getPlaceInfo(xid)
                _museumInfo.value = ObjectInfo(
                    xid = xid,
                    name = placeInfo.name,
                    country_code = placeInfo.address?.country_code,
                    house_number = placeInfo.address?.house_number,
                    postcode = placeInfo.address?.postcode,
                    road = placeInfo.address?.road
                )
            } else {
                val objectInfo = useCaseLocal.getObjectByIdInfo(xid)
                _museumInfo.value = objectInfo
            }
        }
    }

    companion object {
        private const val RU = "ru"
        private const val EN = "en"
        private const val LOW = "1"
        private const val MEDIUM = "2"
        private const val HIGH = "3"
        private const val LOW_H = "1h"
        private const val MEDIUM_H = "2h"
        private const val HIGH_H = "3h"
    }
}


