package com.example.tourismapp.presentation.view_models

import android.graphics.Bitmap
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tourismapp.data.user_preferences.UserPreferences
import com.example.tourismapp.domain.models.local.ObjectInfoModel
import com.example.tourismapp.domain.models.local.PhotoDataModel
import com.example.tourismapp.domain.models.local.PlacesForSearchModel
import com.example.tourismapp.domain.models.local.RouteDataModel
import com.example.tourismapp.domain.models.remote.places_info_model.PlaceInfoModel
import com.example.tourismapp.domain.models.remote.places_model.PlacesModel
import com.example.tourismapp.domain.use_cases.use_case_local.UseCasePhotoLocal
import com.example.tourismapp.domain.use_cases.use_case_remote.UseCaseRemote
import com.example.tourismapp.domain.use_cases.use_case_local.UseCaseObjectLocal
import com.example.tourismapp.domain.use_cases.use_case_local.UseCasePlacesLocal
import com.example.tourismapp.domain.use_cases.use_case_local.UseCaseRouteLocal
import com.example.tourismapp.presentation.services.Polylines
import com.example.tourismapp.presentation.utility.location.DefaultLocationClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.Route
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


private const val TAG = "VIEW_MODEL"

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCaseRemote: UseCaseRemote,
    private val useCasePhotoLocal: UseCasePhotoLocal,
    private val useCaseObjectLocal: UseCaseObjectLocal,
    private val useCasePlacesLocal: UseCasePlacesLocal,
    private val useCaseRouteLocal: UseCaseRouteLocal,
    private val locationClient: DefaultLocationClient,
    private val dataStore: DataStore<UserPreferences>
) : ViewModel() {

    private val _selectedItem = MutableStateFlow<String?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private val _places = MutableStateFlow<PlacesModel?>(null)
    val places = _places.asStateFlow()

    private val _objectSelected = MutableStateFlow<String?>(null)
    val objectSelected = _objectSelected.asStateFlow()

    private val _placesInfo = MutableStateFlow<PlaceInfoModel?>(null)
    val placesInfo = _placesInfo.asStateFlow()

    private val _objectInfo = MutableStateFlow<ObjectInfoModel?>(null)
    val objectInfo = _objectInfo.asStateFlow()

    private val _isAddedToDb = MutableStateFlow(false)
    val isAddedToDb = _isAddedToDb.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName = _currentUserName.asStateFlow()

    private val _userAvatarUri = MutableStateFlow<String?>(null)
    val userAvatarUri = _userAvatarUri.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch = _isFirstLaunch.asStateFlow()

    private val _radius = MutableStateFlow(5000)
    val radius = _radius.asStateFlow()

    private val _kinds = MutableStateFlow<List<String>>(emptyList())

    private val _addedToPlacesKinds = MutableStateFlow(emptyMap<String, Boolean>())
    val addedToPlacesKinds = _addedToPlacesKinds.asStateFlow()

    private val _hideAllPlaces = MutableStateFlow(true)
    val hideAllPlaces = _hideAllPlaces.asStateFlow()

    private val _routeName = MutableStateFlow<String?>(null)
    val routeName = _routeName.asStateFlow()

    private val _allRoutes = MutableStateFlow<List<RouteDataModel>>(emptyList())
    val allRoutes = _allRoutes.asStateFlow()

    private val _isPhoto = MutableStateFlow<Boolean?>(null)
    val isPhoto = _isPhoto.asStateFlow()

    private val _isRoute = MutableStateFlow<Boolean?>(null)
    val isRoute = _isRoute.asStateFlow()

    fun selectItem(uri: String) {
        _selectedItem.value = uri
    }

    init {
        this.viewModelScope.launch {
            getRoutesList()
        }
//        getUserName()
//        getAvatarUri()
//        getLaunchStatus()
    }


    //Photo
    private fun getRoutesList(): Flow<List<PhotoDataModel>> =
        useCasePhotoLocal.getAllRoutesPhotosFromDb().map { it -> it.distinctBy { it.routeName } }

    fun getPhotosByRouteName(routeName: String): Flow<List<PhotoDataModel>> =
        useCasePhotoLocal.getPhotosByRouteName(routeName)


    fun selectRouteName(routeName: String?) {
        _routeName.value = routeName
    }

    fun insertPhotos(
        uri: String,
        date: String,
        description: String?,
        latitude: Double,
        longitude: Double,
        routeName: String
    ) {
        viewModelScope.launch {
            useCasePhotoLocal.insertPhotosToDb(
                photoDataModel = PhotoDataModel(
                    pic_src = uri,
                    date = date,
                    description = description,
                    latitude = latitude,
                    longitude = longitude,
                    routeName = routeName
                )
            )
        }
    }

    fun addPhotoDescription(description: String?, uri: String) {
        viewModelScope.launch {
            useCasePhotoLocal.addPhotoDescription(descriptionText = description, uri = uri)
        }
    }

    fun deletePhoto(uri: String) {
        viewModelScope.launch {
            useCasePhotoLocal.deletePhotoFromDb(uri)
        }
    }

    fun deletePhotosByRouteName(routeName: String) {
        viewModelScope.launch {
            useCasePhotoLocal.deletePhotosByRouteName(routeName)
        }
    }

    fun switchPhotoSelected(isPhoto: Boolean) {
        _isPhoto.value = isPhoto
    }

    //Routes
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertRoute(routeName: String) {
        viewModelScope.launch {
            useCaseRouteLocal.insertRoute(
                routeDataModel = RouteDataModel(
                    route_name = routeName,
                    route_description = null,
                    route_distance = null,
                    route_average_speed = null,
                    route_time = null,
                    bmp = null,
                    start_date = getCurrentDate(),
                    end_date = getCurrentDate()
                )
            )
        }
    }

    fun getAllRoutes(): Flow<List<RouteDataModel>> = useCaseRouteLocal.getAllRoutes()


    fun selectLastRouteName(allRoutes: List<RouteDataModel>) {
        viewModelScope.launch {
            val ids = mutableListOf<Int>()
            allRoutes.forEach { ids.add(it.id) }
            val maxId = ids.max()
            _routeName.value = allRoutes.find { it.id == maxId }?.route_name
        }
    }

    fun finishRoute(routeName: String) {
        viewModelScope.launch {
            useCaseRouteLocal.finishRoute(true, routeName)
        }
    }

    fun getRouteInfoByName(routeName: String) = useCaseRouteLocal.getRouteByName(routeName)

    fun deleteRouteByName(routeName: String) {
        viewModelScope.launch {
            useCaseRouteLocal.deleteRouteByName(routeName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveRouteData(
        routeDistance: Float,
        routeAverageSpeed: Float,
        routeTime: Long,
        routePath: Polylines,
        routeName: String
    ) {
        viewModelScope.launch {
            useCaseRouteLocal.addRouteData(
                routeDistance = routeDistance,
                routeAverageSpeed = routeAverageSpeed,
                routeTime = routeTime,
                endDate = getCurrentDate(),
                routePath = routePath,
                routeName = routeName
            )
        }
    }

    fun addRoutePicture(routePicture: Bitmap, routeName: String) {
        viewModelScope.launch {
            useCaseRouteLocal.addRoutePicture(routePicture, routeName)
        }
    }

    fun addRouteDescription(routeDescription: String, routeName: String) {
        viewModelScope.launch {
            useCaseRouteLocal.addRouteDescription(routeDescription, routeName)
        }
    }

    fun switchRouteSelected(isRoute: Boolean) {
        _isRoute.value = isRoute
    }

    //Objects
    private fun getPlaceInfo(xid: String) {
        viewModelScope.launch {
            _placesInfo.value = useCaseRemote.getPlaceInfo(
                language = getLanguage(),
                xid = xid
            )
        }
    }

    fun selectObject(xid: String) {
        viewModelScope.launch {
            _objectSelected.value = xid
        }
    }

    val allObjects = useCaseObjectLocal.getAllObjectsFromDb()

    fun getObjectInfoById(xid: String, allObjects: List<ObjectInfoModel>) {
        viewModelScope.launch {
            if (allObjects.all { it.xid != xid }) {
                getPlaceInfo(xid)
                _isAddedToDb.value = false
                Log.d(TAG, "Object with id $xid is not in DB")
            } else {
                _objectInfo.value = useCaseObjectLocal.getObjectByIdInfoFromDb(xid)
                _isAddedToDb.value = true
                Log.d(TAG, "Object with id $xid is in DB")
            }
        }
    }

    fun saveObjectsToDB(placeInfoModel: PlaceInfoModel) {
        viewModelScope.launch {
            useCaseObjectLocal.insertObjectInfoToDb(
                objectInfoModel = ObjectInfoModel(
                    xid = placeInfoModel.xid,
                    name = placeInfoModel.name,
                    country_code = placeInfoModel.addressM?.country_code,
                    house_number = placeInfoModel.addressM?.house_number,
                    postcode = placeInfoModel.addressM?.postcode,
                    road = placeInfoModel.addressM?.road,
                    description = placeInfoModel.infoM?.descr,
                    image = placeInfoModel.image
                )
            )
            Log.d(TAG, "Object with id ${placeInfoModel.xid} has been saved to DB")
            _objectInfo.value = useCaseObjectLocal.getObjectByIdInfoFromDb(placeInfoModel.xid)
        }
    }

    //Places
    fun getPlacesKinds(): Flow<List<PlacesForSearchModel>> =
        useCasePlacesLocal.getPlacesKindsFromDb()

    private fun insertPlacesKindsToDb(placeKind: String) {
        viewModelScope.launch {
            useCasePlacesLocal.insertPlacesKindsToDb(
                placesForSearchModel = PlacesForSearchModel(
                    kind = placeKind
                )
            )
            Log.d(TAG, "Place $placeKind has been added to DB")
        }
    }

    private fun deletePlaceKindFromDb(placeKind: String) {
        viewModelScope.launch {
            useCasePlacesLocal.deletePlaceKindFromDb(placeKind = placeKind)
            Log.d(TAG, "Place $placeKind has been deleted from DB")
        }
    }

    fun deleteAllPlacesKinds() {
        viewModelScope.launch {
            useCasePlacesLocal.deleteAllPlacesKinds()
        }
    }

    fun updatePlacesKindsList(placesKindsList: List<String>) {
        viewModelScope.launch {
            _kinds.value = emptyList()
            _kinds.value = placesKindsList
        }
    }

    fun checkPlacesKinds(
        placesKindsList: List<String>
    ) {
        viewModelScope.launch {
            val places = listOf(
                INTERESTING_PLACES,
                FOOD,
                BANKS,
                SHOPS,
                TRANSPORT
            )
            _addedToPlacesKinds.value = emptyMap()
            val initialStatus = _addedToPlacesKinds.value.entries
            val status = mutableMapOf<String, Boolean>()
            initialStatus.forEach { status[it.key] = it.value }
            places.forEach { kind ->
                status[kind] = !placesKindsList.all { it != kind }
            }
            _addedToPlacesKinds.value = status
            Log.d(TAG, "Updated list: ${_addedToPlacesKinds.value}")
        }
    }

    fun onPlacesKindsClick(placeKind: String) {
        viewModelScope.launch {
            if (_addedToPlacesKinds.value[placeKind] == false) {
                val initialStatus = _addedToPlacesKinds.value.entries
                val status = mutableMapOf<String, Boolean>()
                initialStatus.forEach { status[it.key] = it.value }
                status[placeKind] = true
                _addedToPlacesKinds.value = status
                insertPlacesKindsToDb(placeKind)
            } else {
                val initialStatus = _addedToPlacesKinds.value.entries
                val status = mutableMapOf<String, Boolean>()
                initialStatus.forEach { status[it.key] = it.value }
                status[placeKind] = false
                _addedToPlacesKinds.value = status
                deletePlaceKindFromDb(placeKind)
            }
        }
    }

    fun setRadius(value: Int) {
        _radius.value = value * 1000
    }

    fun hideAllPlaces(status: Boolean) {
        viewModelScope.launch {
            _hideAllPlaces.value = status
            Log.d(TAG, "HIDE ALL PLACES is ${_hideAllPlaces.value}")
        }
    }


    //MapsActivity
    fun getPlacesAround(
        longitude: Double,
        latitude: Double,
        kinds: List<String>?,
        placeName: String?
    ) {
        viewModelScope.launch {
            kotlin.runCatching {
                Log.d(
                    TAG,
                    "Language: ${getLanguage()}, radius: ${_radius.value}, Longitude: $longitude, latitude: $latitude," +
                            "kinds: ${_kinds.value}"
                )
                useCaseRemote.getPlacesAround(
                    language = getLanguage(),
                    radius = _radius.value,
                    longitude = longitude,
                    latitude = latitude,
                    kinds = kinds,
                    placeName = placeName
                )
            }.fold(
                onSuccess = { places ->
                    if (places.featuresM.isNotEmpty()) _places.value = places
                    else _places.value = null
                },
                onFailure = {
                    Log.d(TAG, "${it.message}")
                    _places.value = null
                }
            )
        }
    }

    fun getCurrentLocation(interval: Long): Flow<Location> =
        locationClient.getLocationUpdates(interval)


    //Auxiliary
    private fun getLanguage(): String {
        return if (Locale.getDefault() == Locale("ru", "RU")) RU
        else EN
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): String {
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(System.currentTimeMillis())
    }

    fun saveAvatarUriToDataStore(uri: String) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    user_avatar_uri = uri
                )
            }
        }
    }

    fun saveNameToDataStore(name: String) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy(
                    user_name = name
                )
            }
        }
    }

    suspend fun getDataStore() = dataStore.data


    companion object {
        private const val RU = "ru"
        private const val EN = "en"
        private const val INTERESTING_PLACES = "interesting_places"
        private const val FOOD = "foods"
        private const val BANKS = "banks"
        private const val SHOPS = "shops"
        private const val TRANSPORT = "transport"
        private const val DATE_FORMAT = "dd.MM.yy"
    }
}


