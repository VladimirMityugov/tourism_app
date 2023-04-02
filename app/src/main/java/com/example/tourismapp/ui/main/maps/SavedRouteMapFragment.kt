package com.example.tourismapp.ui.main.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentSavedRouteMapBinding
import com.example.tourismapp.presentation.services.Polyline
import com.example.tourismapp.presentation.utility.*
import com.example.tourismapp.presentation.utility.clustering.PhotoMarker
import com.example.tourismapp.presentation.utility.clustering.PhotoRenderer
import com.example.tourismapp.presentation.utility.location.MyLocation
import com.example.tourismapp.presentation.utility.permissions.hasLocationPermission
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


private const val TAG = "SAVED_MAP"

class SavedRouteMapFragment : Fragment() {

    private var _binding: FragmentSavedRouteMapBinding? = null
    val binding get() = _binding!!
    private var map: GoogleMap? = null
    private var myLocation = MyLocation(0.0, 0.0)
    private lateinit var clusterManager: ClusterManager<PhotoMarker>
    private var routePath = mutableListOf<Polyline>()
    private val photoMarkers = mutableListOf<PhotoMarker>()

    private val viewModel: MainViewModel by activityViewModels()

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        //Map settings
        Auxiliary.setMapStyle(map = map!!, context = requireContext())
        Auxiliary.setMapSettings(map = map!!)

        //Move Camera to my location
        map!!.setOnMyLocationButtonClickListener {
            onMyLocationClick()
            true
        }
        clusterManager = ClusterManager(requireContext(), map)

        clusterManager.renderer = PhotoRenderer(
            context = requireContext(),
            map = map!!,
            clusterManager
        )

        map!!.setOnCameraIdleListener(clusterManager)

        clusterManager.setOnClusterItemClickListener { item ->
            onMarkerClickListener(item.position)
            true
        }

        clusterManager.setOnClusterClickListener {
            onClusterClick(it)
        }

        clusterManager.setOnClusterInfoWindowClickListener {
            onClusterInfoWindowClick(it)
        }

        clusterManager.setOnClusterItemInfoWindowClickListener {
            onClusterItemInfoWindowClick(it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedRouteMapBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapView = binding.mapView

        if (requireContext().hasLocationPermission()) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
            mapFragment?.getMapAsync(
                callback
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getCurrentLocation(Constants.INTERVAL_FOR_LOCATION_UPDATES)
                    .collectLatest { location ->
                        myLocation = MyLocation(location.latitude, location.longitude)
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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routeName.collectLatest { name ->
                    if (name != null) {
                        viewModel.getRouteInfoByName(name).collectLatest { routeInfo ->
                            routePath = routeInfo.route_path!!
                            Auxiliary.addAllPolylines(routePath = routePath, map = map!!)
                            Auxiliary.zoomToSeeWholeTrack(
                                routePath = routePath,
                                map = map!!,
                                mapView = mapView
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        photoMarkers.clear()
    }

    override fun onPause() {
        super.onPause()
        map?.clear()
    }

    private fun onClusterClick(cluster: Cluster<PhotoMarker?>): Boolean {
        val builder = LatLngBounds.builder()
        for (item in cluster.items) {
            if (item != null) {
                builder.include(item.position)
            }
        }
        val bounds = builder.build()
        try {
            map!!
                .animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun onClusterInfoWindowClick(cluster: Cluster<PhotoMarker?>?) {
        // Does nothing, but you could go to a list of the users.
    }

    private fun onClusterItemClick(item: PhotoMarker?): Boolean {
        // Does nothing, but you could go into the user's profile page, for example.
        return false
    }

    private fun onClusterItemInfoWindowClick(item: PhotoMarker?) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    private fun onMyLocationClick() {
        val latLng = LatLng(myLocation.latitude, myLocation.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Constants.CAMERA_ZOOM_VALUE)
        map!!.animateCamera(cameraUpdate)
    }

    private fun onMarkerClickListener(position: LatLng) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val photos =
                    viewModel.getPhotosByRouteName(viewModel.routeName.value.toString()).first()
                val selectedPhoto =
                    photos.first { it.latitude == position.latitude && it.longitude == position.longitude }
                viewModel.selectItem(selectedPhoto.pic_src)
                findNavController().navigate(R.id.action_savedRouteMapFragment_to_singlePhotoFragment)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}