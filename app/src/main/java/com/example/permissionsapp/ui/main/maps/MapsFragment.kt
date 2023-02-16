package com.example.permissionsapp.ui.main.maps

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
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
import com.example.permissionsapp.presentation.utility.MyLocation
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentMapsBinding
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.*

private const val TAG = "MAP_FRAGMENT"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@AndroidEntryPoint
class MapsFragment : Fragment() {

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (!it.values.isEmpty() && it.values.all { true }) {
                Log.d(TAG, "ALL PERMISSIONS ARE GRANTED. 0")
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(onMapReadyCallback)
            }
        }

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var userPositionMarker: Marker
    private lateinit var locationAccuracyCircle: Circle
    private lateinit var searchSettingsButton: AppCompatImageButton
    private lateinit var hideShowButton: AppCompatImageButton
    private lateinit var takePhotoButton: AppCompatButton
    private lateinit var routeButton: AppCompatImageButton
    private var routeIsStarted: Boolean = false

    private var needAnimateCamera = true
    private var myLocation = MyLocation(0.0, 0.0)
    private val markers = mutableMapOf<String, String>()
    private var locationSourceListener: LocationSource.OnLocationChangedListener? = null
    private var map: GoogleMap? = null

    private val viewModel: MyViewModel by activityViewModels()

    private val onMapReadyCallback = OnMapReadyCallback {
        map = it
        setMapStyle()
        setMapSettings()
        setLocationSource()

//        map!!.setOnCameraMoveListener{
//            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//                val location = DefaultLocationClient(requireContext(), fusedClient).getLocationUpdates(5000)
//                Log.d(TAG, "Location is : $location ")
//            }
//        }

        map!!.setOnMarkerClickListener { marker ->
            val xid = markers[marker.id]
            Log.d(TAG, "Object xid is : $xid")
            if (xid != null) {
                viewModel.selectObject(xid)
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    viewModel.allObjects.collectLatest { allObjects ->
                        Log.d(TAG, "All objects: $allObjects")
                        viewModel.getObjectInfoById(xid, allObjects)
                    }
                }

                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                    viewModel.isAddedToDb.collectLatest { isAdded ->
                        viewModel.placesInfo.collectLatest { placeInfo ->
                            viewModel.objectSelected.collectLatest { objectId ->
                                if (!isAdded) {
                                    Log.d(TAG, "Object with id $objectId is not in DB")
                                    if (placeInfo != null && placeInfo.xid == objectId) {
                                        Log.d(
                                            TAG,
                                            "Object with name ${placeInfo.name} and xid ${placeInfo.xid} has been saved to DB"
                                        )
                                        viewModel.saveObjectsToDB(placeInfo)
                                    }
                                } else {
                                    Log.d(TAG, "Object with id $objectId is already in DB")
                                }
                            }
                        }
                    }
                }


                Log.d(TAG, "Object is selected. Info is requested")
            }
            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
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
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
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
        checkPermissions()
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
//        mapFragment?.getMapAsync(onMapReadyCallback)

        searchSettingsButton = binding.searchSettingsButton
        hideShowButton = binding.hideShowButton
        takePhotoButton = binding.takePhotoButton
        routeButton = binding.startRouteButton

        takePhotoButton.setOnClickListener {
            findNavController().navigate(R.id.action_maps_to_photoFragment)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.hideAllPlaces.collectLatest {
                hideShowButton.isActivated = !it
            }
        }

        searchSettingsButton.setOnClickListener {
            onSearchSettingsClick()
        }

        routeButton.isActivated = false

        routeButton.setOnClickListener {
            if (!routeIsStarted) {
                Log.d(TAG, "Route is not started")
                routeButton.isActivated = false
                routeIsStarted = true
            } else {
                Log.d(TAG, "Route is started")
                routeButton.isActivated = true
                routeIsStarted = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getCurrentLocation(5000).collectLatest { location ->
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

    override fun onStop() {
        super.onStop()
        needAnimateCamera = false
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

    private fun checkPermissions() {
        val allGranted = REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(onMapReadyCallback)
            Log.d(TAG, "ALL PERMISSIONS ARE GRANTED. 1")
//            setMapSettings()
//            setLocationSource()
        } else {
            if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])) {
                createAlertDialog()
            } else {
                launcher.launch(REQUIRED_PERMISSIONS)
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
                .title("It`s my location")
        )!!
        cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            latLng, 15f
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

    private fun createAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location permission dialog")
            .setMessage("To provide you better experience, please accept location permission")
            .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                launcher.launch(REQUIRED_PERMISSIONS)
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            })
            .create()
            .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MapsFragment()
        private const val INTERESTING_PLACES = "interesting_places"
        private val REQUIRED_PERMISSIONS: Array<String> = buildList {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }.toTypedArray()
    }

}