package com.example.tourismapp.ui.main.maps

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.setPadding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentSavedRouteMapBinding
import com.example.tourismapp.presentation.services.Polyline
import com.example.tourismapp.presentation.utility.Auxiliary
import com.example.tourismapp.presentation.utility.Constants
import com.example.tourismapp.presentation.utility.MyLocation
import com.example.tourismapp.presentation.utility.PhotoMarker
import com.example.tourismapp.presentation.utility.permissions.hasLocationPermission
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
    private lateinit var cameraUpdate: CameraUpdate
    private var routePath = mutableListOf<Polyline>()

    private val viewModel: MainViewModel by activityViewModels()
//    private val clusterManager = ClusterManager<PhotoMarker>(context, map)

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        //Map settings
        Auxiliary.setMapStyle(map = map!!, context = requireContext())
        Auxiliary.setMapSettings(map = map!!)

        //Move Camera to my location
        map!!.setOnMyLocationButtonClickListener {
            val latLng = LatLng(myLocation.latitude, myLocation.longitude)
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Constants.CAMERA_ZOOM_VALUE)
            map!!.animateCamera(cameraUpdate)
            true
        }

        map!!.setOnMarkerClickListener {
            onMarkerClickListener(it.position)
            true
        }
//        map!!.setOnCameraIdleListener(clusterManager)
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
        if (requireContext().hasLocationPermission()) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
            mapFragment?.getMapAsync(callback)
        }

        val mapView = binding.mapView

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
                viewModel.getPhotosByRouteName(viewModel.routeName.value.toString())
                    .collectLatest { photos ->
                        if (photos.isNotEmpty()) {
                            photos.forEach { photo ->
                                val photoMarker = PhotoMarker(
                                    lat = photo.latitude,
                                    lng = photo.longitude,
                                    pic_src = photo.pic_src,
                                    description = photo.description.toString(),
                                    routeName = photo.routeName
                                )
//                                clusterManager.addItem(photoMarker)
                                attachPhotoToRoute(photoMarker)
                            }
                        }
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routeName.collectLatest { name ->
                    Log.d(TAG, "ROUTE: $name")
                    if (name != null) {
                        viewModel.getRouteInfoByName(name).collectLatest { routeInfo ->
                            routePath = routeInfo.route_path!!
                            Log.d(TAG, "ROUTE PATH: $routePath")
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

    private fun attachPhotoToRoute(photoMarker: PhotoMarker) {
        val latLng = photoMarker.position
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(photoMarker.title)
            .snippet((photoMarker.snippet.ifEmpty { "" }).toString())
        Glide.with(this)
            .asBitmap()
            .override(250, 250)
            .load(Uri.parse(photoMarker.pic_src))
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val view = ImageView(requireContext())
                    view.setImageBitmap(resource)
                    view.setPadding(2)
                    view.setBackgroundResource(R.drawable.circle_border)
                    val iconBitmap = Auxiliary.getMarkerBitmapFromView(view)

//                    val markerOptions = MarkerOptions()
//                        .position(cluster.position)
//                        .title("Cluster of ${cluster.size} markers")
//                        .icon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster.size)))
//                    map.addMarker(markerOptions)

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap!!))
                    map!!.addMarker(markerOptions)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
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