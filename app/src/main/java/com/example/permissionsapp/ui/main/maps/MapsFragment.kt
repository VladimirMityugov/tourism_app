package com.example.permissionsapp.ui.main.maps

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.presentation.utility.Constants.ACTION_START
import com.example.permissionsapp.presentation.utility.Constants.ACTION_STOP
import com.example.permissionsapp.presentation.services.LocationService
import com.example.permissionsapp.presentation.services.Polyline
import com.example.permissionsapp.presentation.utility.Constants.ACTION_PAUSE
import com.example.permissionsapp.presentation.utility.Constants.CAMERA_ZOOM_VALUE
import com.example.permissionsapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.permissionsapp.presentation.utility.Constants.POLYLINE_COLOR
import com.example.permissionsapp.presentation.utility.Constants.POLYLINE_WIDTH
import com.example.permissionsapp.presentation.utility.MyLocation
import com.example.permissionsapp.presentation.utility.RouteStates
import com.example.permissionsapp.presentation.utility.hasLocationPermission
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.*


private const val TAG = "MAP_FRAGMENT"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@AndroidEntryPoint
class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var userPositionMarker: Marker
    private lateinit var locationAccuracyCircle: Circle
    private lateinit var searchSettingsButton: AppCompatImageButton
    private lateinit var hideShowButton: AppCompatImageButton
    private lateinit var takePhotoButton: AppCompatImageButton
    private lateinit var startRouteButton: AppCompatImageButton
    private lateinit var stopRouteButton: AppCompatImageButton
    private var needAnimateCamera = true
    private var myLocation = MyLocation(0.0, 0.0)
    private val markers = mutableMapOf<String, String>()
    private var locationSourceListener: LocationSource.OnLocationChangedListener? = null
    private var map: GoogleMap? = null

    private var routeIsStarted: Boolean = false
//    private var routePath = listOf<LatLng?>()
//    private var idlePath = listOf<LatLng>()

    private var pathPoints = mutableListOf<Polyline>()


    private val viewModel: MyViewModel by activityViewModels()

    private val onMapReadyCallback = OnMapReadyCallback {
        map = it
        setMapStyle()
        setMapSettings()
//        setLocationSource()
        Log.d(TAG, "MAP IS CALLED")
//        drawAllRoutePolyline()
//        drawAllIdlePolyline()

        addAllPolylines()

        map!!.setOnMarkerClickListener { marker ->
            val xid = markers[marker.id]
            if (xid != null) {
                viewModel.selectObject(xid)
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    viewModel.allObjects.collectLatest { allObjects ->
                        viewModel.getObjectInfoById(xid, allObjects)
                    }
                }
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    viewModel.isAddedToDb.collectLatest { isAdded ->
                        viewModel.placesInfo.collectLatest { placeInfo ->
                            viewModel.objectSelected.collectLatest { objectId ->
                                if (!isAdded) {
                                    if (placeInfo != null && placeInfo.xid == objectId) {
                                        viewModel.saveObjectsToDB(placeInfo)
                                    }
                                }
                            }
                        }
                    }
                }

            }

            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_VALUE)
            map!!.animateCamera(cameraUpdate)
            marker.showInfoWindow()
            true
        }

        map!!.setInfoWindowAdapter(getInfoWindowAdapter())
        map!!.setOnInfoWindowLongClickListener {
            Log.d(TAG, "Dialog is called")
            createObjectInfoDialog()
        }
        map!!.setOnInfoWindowClickListener { marker ->
            marker.hideInfoWindow()
        }

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

        if (requireContext().hasLocationPermission()) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(onMapReadyCallback)
        }

        searchSettingsButton = binding.searchSettingsButton
        hideShowButton = binding.hideShowButton
        takePhotoButton = binding.takePhotoButton
        startRouteButton = binding.startRouteButton
        stopRouteButton = binding.stopRouteButton

        viewModel.getAllRoutes()

        takePhotoButton.setOnClickListener {
            findNavController().navigate(R.id.action_maps_to_photoFragment)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            LocationService.isTracking.collectLatest { isTracking ->
                Log.d(TAG, "IS TRACKING: $isTracking")
                routeIsStarted = isTracking
                if (isTracking) {

                    LocationService.pathPoints.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                        pathPoints = it
                        addLatestPolyline()
                        moveCameraToUser()
                    })

//                    LocationService.routePath.collectLatest {
//                        Log.d(TAG, "PATH IS : $it")
//                        routePath = it
//                        drawRoutePolyline()
////                        drawAllRoutePolyline()
//                        moveCameraToUser()
                    }
                }
//            else {
//                    LocationService.idlePath.collectLatest {
//                        Log.d(TAG, "IDLE IS : $it")
////                        idlePath = it
////                        drawIdlePolyline()
//                    }
                }



        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.hideAllPlaces.collectLatest {
                hideShowButton.isActivated = !it
            }
        }

        searchSettingsButton.setOnClickListener {
            onSearchSettingsClick()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.routeName.collectLatest { routeName ->
                viewModel.routeStatus.collectLatest { routeStatus ->
                    if (routeName == null
                        || routeStatus == RouteStates.RouteStopped
                    ) {
                        takePhotoButton.isActivated = false
                        takePhotoButton.isEnabled = false
                    } else {
                        takePhotoButton.isActivated = true
                        takePhotoButton.isEnabled = true
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.routeStatus.collectLatest { routeStatus ->
                when (routeStatus) {
                    RouteStates.RoutePaused -> {
                        startRouteButton.isActivated = false
                        stopRouteButton.isActivated = true
                    }
                    RouteStates.RouteStarted -> {
                        startRouteButton.isActivated = true
                        stopRouteButton.isActivated = true
                    }
                    RouteStates.RouteStopped -> {
                        startRouteButton.isActivated = false
                        stopRouteButton.isActivated = false
                    }
                }
            }
        }

        startRouteButton.setOnClickListener {
            if (viewModel.routeName.value != null) {
                if (!routeIsStarted) {
                    startRoute()
                } else {
                    pauseRoute()
                }
            } else {
                createRouteNameAlertDialog()
            }
        }

        stopRouteButton.setOnClickListener {
            stopRoute()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getCurrentLocation(INTERVAL_FOR_LOCATION_UPDATES).collectLatest { location ->
                Log.d(TAG, "Location is ${location.latitude}, ${location.longitude}")
                myLocation = MyLocation(location.latitude, location.longitude)
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
                            showPlaces(location)
                        } else {
                            hideAllPlaces(location)
                        }
                        hideShowButton.setOnClickListener {
                            if (hideAll) {
                                viewModel.hideAllPlaces(false)
                                viewModel.getPlacesAround(
                                    location.longitude,
                                    location.latitude,
                                    placesKinds,
                                    null
                                )
                                showPlaces(location)
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
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.allRoutes.collectLatest { routes ->
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
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onStop() {
        super.onStop()
        needAnimateCamera = false
    }

    private fun startRoute() {
        viewModel.changeRouteStatus(RouteStates.RouteStarted)
        sendCommandToService(ACTION_START)
    }

    private fun pauseRoute() {
        viewModel.changeRouteStatus(RouteStates.RoutePaused)
        sendCommandToService(ACTION_PAUSE)
    }

    private fun stopRoute() {
        viewModel.changeRouteStatus(RouteStates.RouteStopped)
        sendCommandToService(ACTION_STOP)
        viewModel.selectRouteName(null)
        val currentRouteName = viewModel.routeName.value
        if (currentRouteName != null) {
            viewModel.updateRouteEndDate(currentRouteName)
        }
    }

    private fun showPlaces(location: Location) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.places.collectLatest { places ->
                viewModel.hideAllPlaces.collectLatest { hideAllPlaces ->
                    if (!hideAllPlaces) {
                        map!!.clear()
                        places?.features?.forEach { feature ->
                            val placesCoordinates = LatLng(
                                feature.geometry.coordinates[1],
                                feature.geometry.coordinates[0]
                            )
                            val xid = feature.properties.xid
                            val marker = map?.addMarker(
                                MarkerOptions()
                                    .position(placesCoordinates)
                                    .icon(
                                        bitmapDescriptorFromVector(
                                            requireContext(),
                                            R.drawable.dot_icon
                                        )
                                    )
                                    .snippet(feature.properties.name)
                            )?.id
                            if (marker != null) {
                                Log.d(TAG, "Marker id is $marker")
                                markers[marker] = xid
                            }
                        }
                        drawUserPositionMarker(location)
                        drawLocationAccuracyCircle(location)
                    }
                }
            }
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
                    bitmapDescriptorFromVector(
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

    private fun hideAllPlaces(location: Location) {
        map!!.clear()
        drawUserPositionMarker(location)
    }

    private fun setMapStyle() {
        try {
            val success: Boolean = map!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setMapSettings() {
        map!!.isMyLocationEnabled = true
        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        with(map!!.uiSettings) {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
            isCompassEnabled = true
            isZoomGesturesEnabled = true
            isRotateGesturesEnabled = true
        }
    }

    private fun setLocationSource() {
        map!!.setLocationSource(object : LocationSource {
            override fun activate(locationSource: LocationSource.OnLocationChangedListener) {
                locationSourceListener = locationSource
            }

            override fun deactivate() {
                locationSourceListener = null
            }
        })
    }

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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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

        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun bitmapDescriptorFromVector(
        context: Context,
        vectorResId: Int
    ): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun onSearchSettingsClick() {
        val popupWindow = SearchSettingsFragment()
        popupWindow.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        popupWindow.enterTransition = com.google.android.material.R.id.animateToStart
        popupWindow.exitTransition = com.google.android.material.R.id.animateToEnd
        popupWindow.show(requireActivity().supportFragmentManager, "POP_UP")
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), LocationService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

//    private fun drawRoutePolyline() {
//        if (routePath.size > 2) {
//            val lastLatLng = routePath.last()
//            val preLastLatLng = routePath[routePath.size - 2]
//            if (lastLatLng != null) {
//                val polylineOptions = PolylineOptions()
//                    .color(POLYLINE_COLOR)
//                    .width(POLYLINE_WIDTH)
//                    .add(preLastLatLng)
//                    .add(lastLatLng)
//                map?.addPolyline(polylineOptions)
//                Log.d(TAG, "DRAW ROUTE POLYLINE for route $routePath")
//            }
//        }
//    }
//
//    private fun drawIdlePolyline() {
//        Log.d(TAG, "DRAW IDLE POLYLINE for route $idlePath")
//        if (idlePath.size > 1) {
//            val preLastLatLng = idlePath[idlePath.size - 2]
//            val lastLatLng = idlePath[idlePath.size - 1]
//            val polylineOptions = PolylineOptions()
//                .color(POLYLINE_IDLE_COLOR)
//                .width(POLYLINE_IDLE_WIDTH)
//                .addAll(idlePath)
//            map?.addPolyline(polylineOptions)
//        }
//    }

//    private fun drawAllRoutePolyline() {
//        for (polyline in routePath) {
//            polylineOptions
//                .addAll(polyline)
//            map?.addPolyline(polylineOptions)
//        }
//    }

//    private fun drawAllIdlePolyline() {
//        for (idle in idlePath) {
//            val polylineOptions = PolylineOptions()
//                .color(POLYLINE_COLOR)
//                .width(POLYLINE_WIDTH)
//                .addAll(listOf(idle))
//            map?.addPolyline(polylineOptions)
//        }
//    }
//
//
//    private fun moveCameraToUser() {
//        Log.d(TAG, "MOVE CAMERA for route $routePath")
//        if (routePath.size > 1 && routePath.last() != null) {
//            cameraUpdate =
//                CameraUpdateFactory.newLatLngZoom(routePath.last()!!, CAMERA_ZOOM_VALUE)
//            map?.animateCamera(
//                cameraUpdate
//            )
//        }
//    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addAllPolylines() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    CAMERA_ZOOM_VALUE
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val INTERESTING_PLACES = "interesting_places"
    }

}