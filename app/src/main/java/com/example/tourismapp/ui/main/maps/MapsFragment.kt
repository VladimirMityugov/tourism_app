package com.example.tourismapp.ui.main.maps


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.example.tourismapp.presentation.utility.Constants.ACTION_START
import com.example.tourismapp.presentation.utility.Constants.ACTION_STOP
import com.example.tourismapp.presentation.services.LocationService
import com.example.tourismapp.presentation.services.Polyline
import com.example.tourismapp.presentation.utility.Auxiliary
import com.example.tourismapp.presentation.utility.Constants.ACTION_PAUSE
import com.example.tourismapp.presentation.utility.Constants.CAMERA_ZOOM_VALUE
import com.example.tourismapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.tourismapp.presentation.utility.Constants.POLYLINE_COLOR
import com.example.tourismapp.presentation.utility.Constants.POLYLINE_WIDTH
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_LOCATION_PERMISSIONS
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_NOTIFICATION_PERMISSIONS
import com.example.tourismapp.presentation.utility.location.MyLocation
import com.example.tourismapp.presentation.utility.permissions.*
import com.example.tourismapp.presentation.view_models.PermissionsViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentMapsBinding
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_SERVICE_PERMISSIONS
import com.example.tourismapp.presentation.utility.clustering.PhotoMarker
import com.example.tourismapp.presentation.utility.clustering.PhotoRenderer
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.flow.first

private const val TAG = "MAP_FRAGMENT"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@AndroidEntryPoint
class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var userPositionMarker: Marker
    private lateinit var locationAccuracyCircle: Circle

    private lateinit var frame: FrameLayout
    private lateinit var mapView: FragmentContainerView

    private var map: GoogleMap? = null
    private lateinit var clusterManager: ClusterManager<PhotoMarker>
    private var needAnimateCamera = true
    private var myLocation = MyLocation(0.0, 0.0)

    private var routeIsStarted: Boolean = false
    private var routePath = mutableListOf<Polyline>()

    private val markers = mutableMapOf<String, String>()
    private val photoMarkers = mutableListOf<PhotoMarker>()

    private val viewModel: MainViewModel by activityViewModels()
    private val permissionsViewModel: PermissionsViewModel by activityViewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { permission ->
                if (!permission.value) {
                    checkServicePermissions(permission.key)
                }
            }
            if (permissions.values.all { it }) {
                onStartButtonClick()
            }
        }

    private val onMapReadyCallback = OnMapReadyCallback { googleMap ->
        map = googleMap

        Auxiliary.setMapStyle(map = map!!, context = requireContext())
        Auxiliary.setMapSettings(map = map!!)

        addAllPolylines()

        clusterManager = ClusterManager(requireContext(), map)

        clusterManager.renderer = PhotoRenderer(
            context = requireContext(),
            map = map!!,
            clusterManager
        )

        map!!.setOnCameraIdleListener(clusterManager)

        clusterManager.setOnClusterItemClickListener { item ->
            onClusterItemClick(item)
            true
        }

//        map!!.setOnMarkerClickListener { marker ->
//            val xid = markers[marker.id]
//            if (xid != null) {
//                viewModel.selectObject(xid)
//                viewLifecycleOwner.lifecycleScope.launch {
//                    repeatOnLifecycle(Lifecycle.State.STARTED) {
//                        viewModel.allObjects.collectLatest { allObjects ->
//                            viewModel.getObjectInfoById(xid, allObjects)
//                        }
//                        viewModel.isAddedToDb.collectLatest { isAdded ->
//                            viewModel.placesInfo.collectLatest { placeInfo ->
//                                viewModel.objectSelected.collectLatest { objectId ->
//                                    if (!isAdded) {
//                                        if (placeInfo != null && placeInfo.xid == objectId) {
//                                            viewModel.saveObjectsToDB(placeInfo)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
//            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_VALUE)
//            map!!.animateCamera(cameraUpdate)
//            marker.showInfoWindow()
//            true
//        }

//        map!!.setInfoWindowAdapter(getInfoWindowAdapter())
//        map!!.setOnInfoWindowLongClickListener {
//            createObjectInfoDialog()
//        }
//        map!!.setOnInfoWindowClickListener { marker ->
//            marker.hideInfoWindow()
//        }

        //moveCameraToMyLocationByClick
        map!!.setOnMyLocationButtonClickListener {
            val latLng = LatLng(myLocation.latitude, myLocation.longitude)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_VALUE)
            map!!.animateCamera(cameraUpdate)
            true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkLocationPermissions()
        mapView = binding.mapView
        if (requireContext().hasLocationPermission()) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
            mapFragment?.getMapAsync(onMapReadyCallback)
        }

        frame = binding.frameLayout
        val hideShowButton = binding.hideShowButton
        val startRouteButton = binding.startRouteButton
        val stopRouteButton = binding.stopRouteButton
        val searchSettingsButton = binding.searchSettingsButton
        val takePhotoButton = binding.takePhotoButton
        viewModel.getAllRoutes()

        //Buttons
        takePhotoButton.setOnClickListener {
            findNavController().navigate(R.id.action_maps_to_photoFragment)
        }

        startRouteButton.setOnClickListener {
            onStartButtonClick()
        }

        stopRouteButton.setOnClickListener {
            onStopButtonClick()
        }

        searchSettingsButton.setOnClickListener {
            onSearchSettingsClick()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.hideAllPlaces.collectLatest {
                    hideShowButton.isActivated = !it
                }
            }
        }

        //UI handler
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                LocationService.isOnRoute.collectLatest { isOnRoute ->
                    LocationService.isTracking.collectLatest { isTracking ->
                        when {
                            isOnRoute && isTracking -> {
                                startRouteButton.isActivated = true
                                stopRouteButton.isActivated = true
                                takePhotoButton.isActivated = true
                                takePhotoButton.isEnabled = true
                                routeIsStarted = true
                            }
                            isOnRoute && !isTracking -> {
                                startRouteButton.isActivated = false
                                stopRouteButton.isActivated = true
                                takePhotoButton.isActivated = false
                                takePhotoButton.isEnabled = false
                                routeIsStarted = false
                            }
                            !isOnRoute && !isTracking -> {
                                startRouteButton.isActivated = false
                                stopRouteButton.isActivated = false
                                takePhotoButton.isActivated = false
                                takePhotoButton.isEnabled = false
                                routeIsStarted = false
                            }
                        }
                    }
                }
            }
        }


        //mapActivities
        if (requireContext().hasLocationPermission()) {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.getCurrentLocation(INTERVAL_FOR_LOCATION_UPDATES)
                        .collectLatest { location ->

                            //drawRoutePath
                            drawLatestPolyline()
                            myLocation = MyLocation(location.latitude, location.longitude)

                            //setPlacesMarkersOnMapIfRequired
                            viewModel.getPlacesKinds().collectLatest { placeKindList ->
                                val placesKinds = mutableListOf<String>()
                                placeKindList.forEach {
                                    placesKinds.add(it.kind)
                                }
                                viewModel.updatePlacesKindsList(placesKinds)
                                viewModel.hideAllPlaces.collectLatest { hideAll ->
                                    if (!hideAll) {
                                        viewModel.getPlacesAround(
                                            location.longitude,
                                            location.latitude,
                                            placesKinds,
                                            null
                                        )
//                            showPlaces(location)
                                    } else {
                                        hideAllPlaces(location)
                                    }
                                    binding.hideShowButton.setOnClickListener {
                                        if (hideAll) {
                                            viewModel.hideAllPlaces(false)
                                            viewModel.getPlacesAround(
                                                location.longitude,
                                                location.latitude,
                                                placesKinds,
                                                null
                                            )
//                                showPlaces(location)
                                            viewModel.onPlacesKindsClick(INTERESTING_PLACES)
                                        } else {
                                            viewModel.hideAllPlaces(true)
                                            hideAllPlaces(location)
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }



        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val photos =
                    viewModel.getPhotosByRouteName(viewModel.routeName.value.toString()).first()
                if (photos.isNotEmpty()) {
                    photos.forEach { photo ->
                        val photoMarker = PhotoMarker(
                            lat = photo.latitude,
                            lng = photo.longitude,
                            pic_src = photo.pic_src,
                            description = photo.description.toString(),
                            routeName = photo.routeName
                        )
                        photoMarkers.add(photoMarker)
                    }
                    clusterManager.addItems(photoMarkers)
                    clusterManager.cluster()
                }
            }
        }


    }

    private fun checkLocationPermissions() {
        if (!requireContext().hasLocationPermission()) {
            Toast.makeText(
                requireContext(),
                "Please, grant location permission",
                Toast.LENGTH_SHORT
            ).show()
            stopRoute()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun checkServicePermissions(permission: String) {
        providePermissionDialog(
            requireContext(),
            permissionDialogTextProvider = when (permission) {
                android.Manifest.permission.FOREGROUND_SERVICE -> {
                    ForegroundServicePermission()
                }
                android.Manifest.permission.POST_NOTIFICATIONS -> {
                    PostNotificationPermission()
                }
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                    AccessBackgroundLocationPermission()
                }
                else -> {
                    return
                }
            },
            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(
                permission
            ),
            onOkClick = {
                permissionsViewModel.dismissDialog(permission)
                val requiredPermissions = mutableListOf<String>()
                REQUIRED_LOCATION_PERMISSIONS.forEach { requiredPermissions.add(it) }
                REQUIRED_SERVICE_PERMISSIONS.forEach { requiredPermissions.add(it) }
                REQUIRED_NOTIFICATION_PERMISSIONS.forEach { requiredPermissions.add(it) }
                permissionLauncher.launch(requiredPermissions.toTypedArray())
            },
            onDismissClick = { permissionsViewModel.dismissDialog(permission) },
            onGoToAppSettingsCLick = { requireActivity().openAppSettings() }
        )
    }


    //newRouteDialog
    private fun createRouteNameAlertDialog() {

        val dialog = Dialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.route_name_dialog, null)
        val closeButton = dialogView.findViewById<AppCompatImageButton>(R.id.close_button)
        val saveButton = dialogView.findViewById<TextView>(R.id.save_route_name_button)
        val inputField =
            dialogView.findViewById<TextView>(R.id.route_name_field)

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        //Start route, save route info to DB
        saveButton.setOnClickListener {
            if (hasRoutePermission()
            ) {
                onSaveRouteNameClick(inputField, dialog)
            } else {
                dialog.dismiss()
            }
        }
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }


    //locationServiceHandlers
    private fun startRoute() {
        if (hasRoutePermission()) {
            sendCommandToService(ACTION_START)
        } else {
            Toast.makeText(
                requireContext(),
                "Please, grant necessary permissions",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun pauseRoute() {
        sendCommandToService(ACTION_PAUSE)
    }

    private fun stopRoute() {
        sendCommandToService(ACTION_STOP)
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }


    //objectHandlers
//    private fun showPlaces(location: Location) {
//        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//            viewModel.places.collectLatest { places ->
//                viewModel.hideAllPlaces.collectLatest { hideAllPlaces ->
//                    if (!hideAllPlaces) {
////                        map!!.clear()
//                        places?.featuresM?.forEach { feature ->
//                            val placesCoordinates = LatLng(
//                                feature.geometry.coordinates[1],
//                                feature.geometry.coordinates[0]
//                            )
//                            val xid = feature.propertiesM.xid
//                            val marker = map?.addMarker(
//                                MarkerOptions()
//                                    .position(placesCoordinates)
//                                    .icon(
//                                        Auxiliary.bitmapDescriptorFromVector(
//                                            requireContext(),
//                                            R.drawable.dot_icon
//                                        )
//                                    )
//                                    .snippet(feature.propertiesM.name)
//                            )?.id
//                            if (marker != null) {
//                                Log.d(TAG, "Marker id is $marker")
//                                markers[marker] = xid
//                            }
//                        }
//                        drawUserPositionMarker(location)
//                        drawLocationAccuracyCircle(location)
//                    }
//                }
//            }
//        }
//    }

    @SuppressLint("MissingInflatedId")
    private fun createObjectInfoDialog() {

        val dialog = Dialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.object_info_dialog, null)
        val placeImage =
            dialogView.findViewById<AppCompatImageView>(R.id.place_image)
        val closeButton = dialogView.findViewById<AppCompatImageButton>(R.id.close_button)
        val mainInfo = dialogView.findViewById<TextView>(R.id.objectInfo)
        val additionalInfo =
            dialogView.findViewById<TextView>(R.id.objectAdditionalInfo)


        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.objectSelected.collectLatest { objectId ->
                    viewModel.objectInfo.collectLatest { objectInfo ->
                        if (objectInfo != null && objectInfo.xid == objectId) {
                            Log.d(
                                TAG,
                                "ID: ${objectInfo.xid}, ID SELECTED: $objectId, NAME: ${objectInfo.name}"
                            )
                            Glide
                                .with(placeImage.context)
                                .load(objectInfo.image)
                                .error(R.drawable.dot_icon)
                                .fitCenter()
                                .into(placeImage)

//                                markerTitle.text = objectInfo.name
                            mainInfo.text = buildString {
                                append("Address:")
                                append("\n")
                                append(objectInfo.road)
                                append(",")
                                append(objectInfo.house_number)
                                append("\n")
                                append(objectInfo.postcode)
                            }
                            additionalInfo.text = objectInfo.description
                        }
                    }
                }
            }
        }

        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun hideAllPlaces(location: Location) {
//        map!!.clear()
//        drawUserPositionMarker(location)
    }


    //mapSettings
    private fun drawLocationAccuracyCircle(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        locationAccuracyCircle = map!!.addCircle(
            CircleOptions()
                .center(latLng)
                .fillColor(Color.argb(64, 0, 0, 0))
                .strokeColor(Color.argb(64, 0, 0, 0))
                .strokeWidth(0.0f)
                .radius(location.accuracy.toDouble())
        )
    }

    private fun drawUserPositionMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        userPositionMarker = map?.addMarker(
            MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 0.5f)
                .icon(
                    Auxiliary.bitmapDescriptorFromVector(
                        requireContext(),
                        R.drawable.current_location_icon
                    )
                )
        )!!
        cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            latLng, CAMERA_ZOOM_VALUE
        )
        if (needAnimateCamera) {
            map?.moveCamera(cameraUpdate)
            needAnimateCamera = false
        }
    }

    private fun getInfoWindowAdapter(): GoogleMap.InfoWindowAdapter {
        return object : GoogleMap.InfoWindowAdapter {

            @SuppressLint("InflateParams")
            var myInfoWindowView: View =
                LayoutInflater.from(requireContext()).inflate(R.layout.custom_info_window, null)
            val objectDetailsButton =
                myInfoWindowView.findViewById<AppCompatImageButton>(R.id.object_details_button)


            override fun getInfoWindow(marker: Marker): View {
                setInfoWindowText(marker)
                return myInfoWindowView
            }

            override fun getInfoContents(marker: Marker): View {
                setInfoWindowText(marker)
                return myInfoWindowView
            }

            private fun setInfoWindowText(marker: Marker) {
                Log.d(TAG, "Marker id is ${marker.id}")
                Log.d(TAG, "Object id of marker id ${marker.id} is ${markers[marker.id]}")
                val markerTitle = myInfoWindowView.findViewById<TextView>(R.id.markerTitle)

                markerTitle.text = marker.snippet
            }
        }
    }

    //polylinesHandlers
    private fun moveCameraToUser() {
        if (routePath.isNotEmpty() && routePath.last().size > 1) {
            cameraUpdate =
                CameraUpdateFactory.newLatLngZoom(routePath.last().last(), CAMERA_ZOOM_VALUE)
            map?.animateCamera(
                cameraUpdate
            )
        }
    }

    private fun addLatestPolyline() {
        if (routePath.isNotEmpty() && routePath.last().size > 1) {
            val preLastLatLng = routePath.last()[routePath.last().size - 2]
            val lastLatLng = routePath.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun drawLatestPolyline() {
        routePath = LocationService.routePath.value
        addLatestPolyline()
        moveCameraToUser()
    }

    private fun addAllPolylines() {
        routePath = LocationService.routePath.value
        for (polyline in routePath) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map!!.addPolyline(polylineOptions)
        }
    }

    private fun zoomToSeeWholeTrack() {
        routePath = LocationService.routePath.value
        val bounds = LatLngBounds.builder()
        for (polyline in routePath) {
            for (coordinates in polyline) {
                bounds.include(coordinates)
            }
        }
        map!!.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.05F).toInt()
            )
        )
    }

    private fun saveRouteData() {
        Log.d(TAG, "SAVE ROUTE DATA")
        stopRoute()
        val routePath = LocationService.routePath.value
        val routeDistance = Auxiliary.calculateRouteDistance(routePath)
        val routeName = viewModel.routeName.value
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                LocationService.totalTime.collectLatest {
                    val totalTime = it
                    val averageSpeed =
                        Auxiliary.calculateAverageSpeed(totalTime, routeDistance)
                    viewModel.saveRouteData(
                        routeDistance = routeDistance,
                        routeAverageSpeed = averageSpeed,
                        routeTime = it,
                        routePath = routePath,
                        routeName = routeName!!
                    )
                }
            }
        }

        map?.snapshot { bmp ->
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.addRoutePicture(bmp!!, routeName!!)
                    viewModel.finishRoute(routeName)
                    viewModel.selectRouteName(null)
                    routePath.clear()
                    map?.clear()
                }
            }
        }
        Snackbar.make(requireContext(), mapView, "Route data is saved", Snackbar.LENGTH_LONG).show()
    }


    //buttonClicksHandlers
    private fun onSaveRouteNameClick(inputField: TextView, dialog: Dialog) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val routes = viewModel.getAllRoutes().first()
                val name = viewModel.routeName.value
                if (name == null) {
                    val newRouteName = inputField.text
                        .trim()
                        .toString()
                        .lowercase(Locale.ROOT)
                        .replaceFirstChar {
                            it.uppercaseChar()
                        }
                    val routeExists = routes.find { it.route_name == newRouteName }

                    if (newRouteName.isNotEmpty()) {
                        if (routeExists == null) {
                            viewModel.selectRouteName(newRouteName)
                            viewModel.insertRoute(
                                routeName = newRouteName
                            )
                            startRoute()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Route with this name already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Route name could not be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun hasRoutePermission(): Boolean {
        return (requireContext().hasLocationPermission()
                && requireContext().hasNotificationPermission()
                && requireContext().hasServicePermission())
    }


    private fun onStopButtonClick() {
        if (LocationService.isOnRoute.value) {
            zoomToSeeWholeTrack()
            onMapReadyCallback.onMapReady(map!!).also {
                saveRouteData()
            }
        }
    }

    private fun onStartButtonClick() {
        when {
            !routeIsStarted && viewModel.routeName.value == null -> {
                if (hasRoutePermission()
                ) {
                    createRouteNameAlertDialog()
                } else {
                    val requiredPermissions = mutableListOf<String>()
                    REQUIRED_LOCATION_PERMISSIONS.forEach { requiredPermissions.add(it) }
                    REQUIRED_SERVICE_PERMISSIONS.forEach { requiredPermissions.add(it) }
                    REQUIRED_NOTIFICATION_PERMISSIONS.forEach { requiredPermissions.add(it) }
                    permissionLauncher.launch(requiredPermissions.toTypedArray())
                }
            }
            !routeIsStarted && viewModel.routeName.value != null -> startRoute()
            routeIsStarted && viewModel.routeName.value != null -> pauseRoute()
        }
    }

    private fun onSearchSettingsClick() {
        val popupWindow = SearchSettingsFragment()
        popupWindow.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        popupWindow.enterTransition = com.google.android.material.R.id.animateToStart
        popupWindow.exitTransition = com.google.android.material.R.id.animateToEnd
        popupWindow.show(requireActivity().supportFragmentManager, "POP_UP")
    }

    private fun onClusterItemClick(item: PhotoMarker): Boolean {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val photos =
                    viewModel.getPhotosByRouteName(viewModel.routeName.value.toString()).first()
                val selectedPhoto =
                    photos.first { it.latitude == item.position.latitude && it.longitude == item.position.longitude }
                viewModel.selectItem(selectedPhoto.pic_src)
                findNavController().navigate(R.id.action_maps_to_singlePhotoFragment)
            }
        }
        return false
    }


    override fun onStop() {
        super.onStop()
        needAnimateCamera = false
        map!!.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val INTERESTING_PLACES = "interesting_places"
    }

}