package com.example.tourismapp.ui.main.photos

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentSavedRouteMapBinding
import com.example.tourismapp.presentation.services.Polyline
import com.example.tourismapp.presentation.utility.Auxiliary
import com.example.tourismapp.presentation.utility.Constants
import com.example.tourismapp.presentation.utility.MyLocation
import com.example.tourismapp.presentation.utility.permissions.hasLocationPermission
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.collectLatest
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
                viewModel.routeName.collectLatest { name ->
                    Log.d(TAG, "ROUTE: $name")
                    if (name != null) {
                        viewModel.getRouteInfoByName(name).collectLatest { routeInfo ->
                            routePath = routeInfo.route_path!!
                            Log.d(TAG, "ROUTE PATH: $routePath")
                            Auxiliary.addAllPolylines(routePath = routePath, map = map!!)
                            Auxiliary.zoomToSeeWholeTrack(routePath = routePath, map = map!!, mapView = mapView)
                        }
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}