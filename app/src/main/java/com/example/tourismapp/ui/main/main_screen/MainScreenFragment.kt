package com.example.tourismapp.ui.main.main_screen


import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tourismapp.R
import com.example.tourismapp.data.local.entities.RouteData
import com.example.tourismapp.databinding.FragmentMainScreenBinding
import com.example.tourismapp.domain.models.local.RouteDataModel
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.example.tourismapp.presentation.adapters.RoutesAdapter
import com.example.tourismapp.presentation.services.LocationService
import com.example.tourismapp.presentation.utility.Constants
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_LOCATION_PERMISSIONS
import com.example.tourismapp.presentation.utility.permissions.*
import com.example.tourismapp.presentation.view_models.PermissionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



private const val TAG = "MAIN_SCREEN"

@AndroidEntryPoint
class MainScreenFragment : Fragment() {

    private var _binding: FragmentMainScreenBinding? = null
    private val binding get() = _binding!!

    private val routesAdapter = RoutesAdapter(
        onItemClick = { route -> onRouteClick(route) },
        onDeleteRouteClick = { route -> onRouteDeleteClick(route) }
    )

    private val viewModel: MainViewModel by activityViewModels()
    private val permissionsViewModel: PermissionsViewModel by activityViewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            it.keys.forEach { permission ->
                permissionsViewModel.onPermissionResult(
                    permission, it[permission] == true
                )
                if(it[permission] == true){
                    permissionsViewModel.dismissDialog(permission)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userName = binding.userName
        val userAvatar = binding.userAvatar
        val routesRecyclerView = binding.routesRecyclerView
        val firstRouteButton = binding.firstRouteButton
        val infoField = binding.infoField
        val scrollView = binding.scrollView

        routesRecyclerView.adapter = routesAdapter


        //displayRoutesIfAny
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllRoutes().collectLatest { routes ->
                if (routes.isNotEmpty()) {
                    val finishedRoutes = mutableListOf<RouteDataModel>()
                    routes.forEach {
                        if (it.route_is_finished && !finishedRoutes.contains(it)) {
                            Log.d(TAG, "$it")
                            finishedRoutes.add(it)
                        }
                    }
                    routesRecyclerView.visibility = View.VISIBLE
                    scrollView.visibility = View.VISIBLE
                    infoField.visibility = View.INVISIBLE
                    firstRouteButton.visibility = View.INVISIBLE
                    routesAdapter.submitList(finishedRoutes)

                } else {
                    routesRecyclerView.visibility = View.GONE
                    scrollView.visibility = View.GONE
                    infoField.visibility = View.VISIBLE
                    firstRouteButton.visibility = View.VISIBLE
                }
            }
        }


        //setUserName
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDataStore().collectLatest {
                val newName = it.user_name
                if (newName.isNotEmpty()) {
                    userName.text = newName
                } else {
                    userName.text = ""
                }
            }
        }


        //setUserAvatar
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getDataStore().collectLatest {
                val uri = it.user_avatar_uri
                if (uri.isNotEmpty()) {
                    Glide
                        .with(userAvatar)
                        .load(uri)
                        .centerCrop()
                        .into(userAvatar)
                } else {
                    Glide
                        .with(userAvatar)
                        .load(Constants.AVATAR)
                        .centerCrop()
                        .into(userAvatar)
                }
            }
        }

        lifecycleScope.launch {
            permissionsViewModel.permissionsList.collectLatest {
                Log.d(TAG, "PERMISSIONS LIST: $it")
                it
                    .reversed()
                    .forEach { permission ->
                        providePermissionDialog(
                            requireContext(),
                            permissionDialogTextProvider = when (permission) {
                                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                    AccessCoarseLocationPermission()
                                }
                                Manifest.permission.ACCESS_FINE_LOCATION -> {
                                    AccessFineLocationPermission()
                                }
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                                    AccessBackgroundLocationPermission()
                                }
                                else -> {
                                    return@forEach
                                }
                            },
                            isPermanentlyDeclined = !shouldShowRequestPermissionRationale(permission),
                            onOkClick = {
                                permissionsViewModel.dismissDialog(permission)
                                permissionLauncher.launch(arrayOf(permission))
                            },
                            onDismissClick = { permissionsViewModel.dismissDialog(permission) },
                            onGoToAppSettingsCLick = { requireActivity().openAppSettings() }
                        )
                    }
            }
        }

        firstRouteButton.setOnClickListener {
            if (requireContext().hasLocationPermission()) {
                findNavController().navigate(R.id.action_main_to_maps)
            } else {
                permissionLauncher.launch(REQUIRED_LOCATION_PERMISSIONS)
            }
        }

        firstRouteButton
            .animate()
            .translationY(150F)
            .setStartDelay(1000L)
            .setDuration(500L)
            .withEndAction {
                firstRouteButton
                    .animate()
                    .translationY(0F)
                    .setDuration(800L)
                    .interpolator = BounceInterpolator()
            }
            .start()

    }


    private fun onRouteClick(route: RouteDataModel) {
        if (!LocationService.isOnRoute.value && !LocationService.isTracking.value) {
            viewModel.selectRouteName(route.route_name)
            viewModel.getPhotosByRouteName(route.route_name)
            findNavController().navigate(R.id.action_main_to_routeFragment)
        } else {
            Toast.makeText(
                requireContext(),
                "Please, finish current route first",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun onRouteDeleteClick(route: RouteDataModel) {
        viewModel.deletePhotosByRouteName(route.route_name)
        viewModel.deleteRouteByName(route.route_name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
