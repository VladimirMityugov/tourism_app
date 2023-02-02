package com.example.permissionsapp.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.data.remote.places_dto.Places
import com.example.permissionsapp.domain.UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "VIEW_MODEL"

@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: UseCase
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

    fun selectItem(item: PhotoData) {
        _selectedItem.value = item
    }

    init {
        this.viewModelScope.launch {
            getPhotoList()
        }
    }

    fun getPhotoList(): Flow<List<PhotoData>> = useCase.getPhotos()


    fun insertPhotos(uri: String, date: String, description: String?) {
        viewModelScope.launch {
            useCase.insertPhotos(
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
            useCase.deletePhoto(uri)
        }
    }

    fun updateCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _currentLatitude.value = latitude
            _currentLongitude.value = longitude
        }
    }

    fun getMuseumsAround(longitude: Double, latitude: Double) {
        viewModelScope.launch {
            kotlin.runCatching {
                useCase.getMuseumsAroundUseCase(longitude, latitude)
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

    fun saveObjectsToDB(places: Places) {
        viewModelScope.launch {
            val placesAround = places.features
            if (placesAround.isNotEmpty() && placesAround.size >= 5) {
                Log.d(TAG, "museums around = $placesAround")
                for (i in 1..5) {
                    val it = placesAround[i]
                    val xid = it.properties.xid
                    Log.d(TAG, "xid of object to save into DB is $xid")
                    val objectInfo = useCase.getMuseumsInfoUseCase(xid)
                    useCase.insertObjectInfo(
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

    private val allObjects = useCase.getObjectInfo()
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(1000),
            initialValue = emptyList()
        )

    fun getObjectInfoById(xid: String) {
        viewModelScope.launch {
            if (allObjects.value.all { it.xid != xid }) {
                val placeInfo = useCase.getMuseumsInfoUseCase(xid)
                _museumInfo.value = ObjectInfo(
                    xid = xid,
                    name = placeInfo.name,
                    country_code = placeInfo.address?.country_code,
                    house_number = placeInfo.address?.house_number,
                    postcode = placeInfo.address?.postcode,
                    road = placeInfo.address?.road
                )
            } else {
                val objectInfo = useCase.getObjectByIdInfo(xid)
                _museumInfo.value = objectInfo
            }
        }
    }
}


