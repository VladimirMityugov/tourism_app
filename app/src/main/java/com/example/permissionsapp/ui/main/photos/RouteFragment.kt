package com.example.permissionsapp.ui.main.photos

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
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.presentation.view_models.MainViewModel
import com.example.permissionsapp.presentation.PhotoAdapter
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentRouteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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

    private lateinit var routeName: AppCompatTextView
    private lateinit var routeDistance: AppCompatTextView
    private lateinit var routeDuration: AppCompatTextView
    private lateinit var routeAvgSpeed: AppCompatTextView
    private lateinit var photosRecyclerView: RecyclerView
    private lateinit var backButton: AppCompatImageButton
    private lateinit var description: AppCompatTextView
    private lateinit var addDescriptionButton: AppCompatImageButton
    private lateinit var addDescriptionTitle: AppCompatTextView

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

        checkPermissions()

        backButton = binding.backButton
        description = binding.description
        routeName = binding.routeName
        routeDistance = binding.routeDistance
        routeDuration = binding.routeDuration
        routeAvgSpeed = binding.routeAvgSpeed
        addDescriptionButton = binding.addDescriptionButton
        addDescriptionTitle = binding.addDescriptionTitle
        photosRecyclerView = binding.photosRecyclerView

        photosRecyclerView.adapter = photoAdapter

        addDescriptionButton.setOnClickListener {
            onAddDescriptionClick()
        }

        addDescriptionTitle.setOnClickListener {
            onAddDescriptionClick()
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        description.setTextColor(resources.getColor(R.color.myOrange, resources.newTheme()))

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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
                                append(round((routeInfo.route_time!! / 1000F / 60)*100) / 100)
                            }
                            routeDistance.text = buildString {
                                append("Route distance (meters): ")
                                append(routeInfo.route_distance!!.toInt())
                            }
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

    private fun onItemClick(item: PhotoData) {
        viewModel.selectItem(item.pic_src)
        findNavController().navigate(R.id.action_routeFragment_to_singlePhotoFragment)
    }

    private fun onDeleteItemClick(photoData: PhotoData) {
        viewModel.deletePhoto(photoData.pic_src)
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
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.getPhotosByRouteName(viewModel.routeName.value.toString())
                    .collectLatest { photos ->
                        photoAdapter.submitList(photos)
                    }
            }
        } else launcher.launch(REQUEST_PERMISSIONS)
    }

    private fun onLongItemClick(item: PhotoData) {
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