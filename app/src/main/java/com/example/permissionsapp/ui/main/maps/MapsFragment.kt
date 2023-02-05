package com.example.permissionsapp.ui.main.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.permissionsapp.data.local.entities.ObjectInfo
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.presentation.utility.DefaultLocationClient
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "MAP_FRAGMENT"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@AndroidEntryPoint
class MapsFragment : Fragment() {

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (!it.values.isEmpty() && it.values.all { true }) {
                Log.d(TAG, "ALL PERMISSIONS ARE GRANTED")
            }
        }

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraUpdate: CameraUpdate
    private lateinit var userPositionMarker: Marker
    private lateinit var locationAccuracyCircle: Circle
    private lateinit var searchSettingsButton: AppCompatImageButton
    private lateinit var hideShowButton: AppCompatImageButton
    private var needAnimateCamera = true
    private var info: ObjectInfo? = null
    private val markers = mutableMapOf<String, String>()
    private var locationSourceListener: LocationSource.OnLocationChangedListener? = null
    private var map: GoogleMap? = null

    private val viewModel: MyViewModel by activityViewModels()

    private val onMapReadyCallback = OnMapReadyCallback {
        map = it
        checkPermissions()
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
            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
            map!!.animateCamera(cameraUpdate)
            marker.showInfoWindow()
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                if (xid != null) {
                    viewModel.getObjectInfoById(xid)
                    info = viewModel.museumInfo.value
                }
            }
            true
        }

        map!!.setInfoWindowAdapter(getInfoWindowAdapter())

        map!!.setOnInfoWindowClickListener { marker ->
            marker.hideInfoWindow()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        fusedClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkPermissions()
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
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(onMapReadyCallback)

        searchSettingsButton = binding.searchSettingsButton
        hideShowButton = binding.hideShowButton

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.hideAllPlaces.collectLatest {
                hideShowButton.isActivated = !it
            }
        }

        searchSettingsButton.setOnClickListener {
            onSearchSettingsClick()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getCurrentLocation(5000).collectLatest { location ->
                viewModel.getPlacesKinds().collectLatest { placeKindList ->
                    Log.d(TAG, "Places for search are : $placeKindList")
                    val placesKinds = mutableListOf<String>()
                    placeKindList.forEach {
                        placesKinds.add(it.kind)
                    }
                    viewModel.updatePlacesKindsList(placesKinds)
                    viewModel.hideAllPlaces.collectLatest { hideAll ->
                        Log.d(TAG, "HIDE status is : $hideAll")
                        if (!hideAll) {
                            Log.d(TAG, "SHOW PLACES")
                            viewModel.getPlacesAround(
                                location.longitude,
                                location.latitude,
                                placesKinds,
                                null
                            )
                            showPlaces(location)
                        } else {
                            Log.d(TAG, "HIDE PLACES")
                            hideAllPlaces(location)
                        }
                        hideShowButton.setOnClickListener {
                            if (hideAll) {
                                viewModel.hideAllPlaces(false)
                                Log.d(TAG, "SHOW PLACES")
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
                                Log.d(TAG, "HIDE PLACES")
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
                        Log.d(TAG, "Places are called")
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
                                            R.drawable.marker_android
                                        )
                                    )
                                    .title(feature.properties.name)
                                    .snippet("Latitude: ${feature.geometry.coordinates[1]}\nLongitude: ${feature.geometry.coordinates[0]}")
                            )?.id
                            if (marker != null) {
                                markers[marker] = xid
                            }
                        }
                        drawUserPositionMarker(location)
                        drawLocationAccuracyCircle(location)
                        Log.d(TAG, "Places are $places")
//                if (places != null) {
//                    viewModel.saveObjectsToDB(places)
//                }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        Log.d(TAG, "check permissions")
        val allGranted = REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            Log.d(TAG, "ALL PERMISSIONS ARE GRANTED")
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

            private fun setInfoWindowText(marker: Marker) {
                val title = marker.title
                val markerTitle = myInfoWindowView.findViewById<TextView>(R.id.markerTitle)
                markerTitle.text = title

                val snippet = marker.snippet
                val objectInfo = myInfoWindowView.findViewById<TextView>(R.id.objectInfo)
                objectInfo.text = snippet

                val additionalInfo =
                    myInfoWindowView.findViewById<TextView>(R.id.objectAdditionalInfo)
                additionalInfo.text = buildString {
                    if (info != null) {
                        append("Address:")
                        append("\n")
                        append(info!!.road)
                        append(",")
                        append(info!!.house_number)
                        append("\n")
                        append(info!!.postcode)
                    }
                }
            }
            override fun getInfoWindow(p0: Marker): View {
                setInfoWindowText(p0)
                return myInfoWindowView
            }

            override fun getInfoContents(p0: Marker): View {
                setInfoWindowText(p0)
                return myInfoWindowView
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
                .title("Its my location")
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

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
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