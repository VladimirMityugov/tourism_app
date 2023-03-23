package com.example.tourismapp.ui.main.photos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentRouteBinding
import com.example.tourismapp.domain.models.local.PhotoDataModel
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.example.tourismapp.presentation.adapters.PhotoAdapter
import com.example.tourismapp.presentation.utility.permissions.hasReadPermission
import com.example.tourismapp.presentation.utility.permissions.hasWritePermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.round

private const val TAG = "ROUTE_FRAGMENT"
private const val DATE_FORMAT = "dd-MM-yyyy \n hh-mm"

@AndroidEntryPoint
class RouteFragment : Fragment() {

    companion object {
        fun newInstance() = RouteFragment()
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (!it.values.isEmpty() && it.values.all { true }) {
                Log.d(TAG, "ALL PERMISSIONS ARE GRANTED")
            }
        }

    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val photoAdapter = PhotoAdapter(
        onItemClick = { PhotoData -> onItemClick(PhotoData) },
        onLongClick = { PhotoData -> onLongItemClick(PhotoData) },
        onDeletePhotoClick = { PhotoData -> onDeleteItemClick(PhotoData) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!requireContext().hasReadPermission() && !requireContext().hasWritePermission()) {
            checkPermissions()
        }

        val description = binding.description
        val routeName = binding.routeName
        val routeDistance = binding.routeDistance
        val routeDuration = binding.routeDuration
        val routeAvgSpeed = binding.routeAvgSpeed
        val routePicture = binding.routePicture
        val routeMiniPicture = binding.routeMiniPicture

        val photosRecyclerView = binding.photosRecyclerView
        photosRecyclerView.adapter = photoAdapter

        routePicture.setOnClickListener {
            onRoutePictureClick()
        }

        routeMiniPicture.setOnClickListener {
            onRoutePictureClick()
        }

        binding.addDescriptionButton.setOnClickListener {
            onAddDescriptionClick()
        }

        binding.addDescriptionTitle.setOnClickListener {
            onAddDescriptionClick()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        description.setTextColor(resources.getColor(R.color.myOrange, resources.newTheme()))

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPhotosByRouteName(viewModel.routeName.value.toString())
                    .collectLatest { photos ->
                        if (photos.isNotEmpty()) {
                            photosRecyclerView.visibility = View.VISIBLE
                            routeMiniPicture.visibility = View.VISIBLE
                            routePicture.visibility = View.GONE
                            photoAdapter.submitList(photos)
                        } else {
                            photosRecyclerView.visibility = View.GONE
                            routePicture.visibility = View.VISIBLE
                            routeMiniPicture.visibility = View.GONE
                        }
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routeName.collectLatest { name ->
                    if (name != null) {
                        routeName.text = name
                        viewModel.getRouteInfoByName(name).collectLatest { routeInfo ->
                            if (routeInfo.route_is_finished) {
                                routeAvgSpeed.visibility = View.VISIBLE
                                routeDuration.visibility = View.VISIBLE
                                routeDistance.visibility - View.VISIBLE
                                routeAvgSpeed.text = buildString {
                                    append("Average speed (km/h): ")
                                    append(routeInfo.route_average_speed)
                                }
                                routeDuration.text = buildString {
                                    append("Route duration (minutes): ")
                                    append(round((routeInfo.route_time!! / 1000F / 60) * 100) / 100)
                                }
                                routeDistance.text = buildString {
                                    append("Route distance (meters): ")
                                    append(routeInfo.route_distance!!.toInt())
                                }
                                Glide
                                    .with(routePicture)
                                    .load(routeInfo.bmp)
                                    .into(routePicture)
                                Glide
                                    .with(routeMiniPicture)
                                    .load(routeInfo.bmp)
                                    .into(routeMiniPicture)
                            } else {
                                routeAvgSpeed.visibility = View.INVISIBLE
                                routeDuration.visibility = View.INVISIBLE
                                routeDistance.visibility - View.INVISIBLE
                            }

                            val routeDescription =
                                routeInfo.route_description
                            if (routeDescription != null) {
                                description.visibility = View.VISIBLE
                                description.text = routeDescription
                            } else {
                                description.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onItemClick(item: PhotoDataModel) {
        viewModel.selectItem(item.pic_src)
        findNavController().navigate(R.id.action_routeFragment_to_singlePhotoFragment)
    }

    private fun onDeleteItemClick(photoData: PhotoDataModel) {
        viewModel.deletePhoto(photoData.pic_src)
    }

    private fun onRoutePictureClick() {
        findNavController().navigate(R.id.action_routeFragment_to_savedRouteMapFragment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkPermissions() {
        val allGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            Log.d(TAG, "ALL PERMISSIONS GRANTED")
        } else launcher.launch(REQUEST_PERMISSIONS)
    }

    private fun onLongItemClick(item: PhotoDataModel) {
        viewModel.selectItem(item.pic_src)
        findNavController().navigate(R.id.action_routeFragment_to_collectionGalleryFragment)
    }

    private fun onAddDescriptionClick() {
        viewModel.switchRouteSelected(true)
        val popupWindow = DescriptionFragment()
        popupWindow.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        popupWindow.enterTransition = android.transition.Fade.IN
        popupWindow.exitTransition = android.transition.Fade.OUT
        popupWindow.show(requireActivity().supportFragmentManager, "POP_UP")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}