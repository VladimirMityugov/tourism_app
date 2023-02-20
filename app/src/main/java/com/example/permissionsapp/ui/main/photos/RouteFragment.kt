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
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.presentation.PhotoAdapter
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentRouteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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
    private lateinit var photosRecyclerView: RecyclerView

    private val viewModel: MyViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val photoAdapter = PhotoAdapter(
        onItemClick = { PhotoData -> onItemClick(PhotoData) },
        onLongClick = { PhotoData -> onLongItemClick(PhotoData) }
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


        routeName = binding.routeName
        photosRecyclerView = binding.photosRecyclerView

        photosRecyclerView.adapter = photoAdapter


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.photos.collectLatest { photos ->
                photoAdapter.submitList(photos)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.routeName.collectLatest { name ->
                Log.d(TAG, "route name is : $name")
                routeName.text = name
            }
        }

    }

    private fun moveToPhotoFragment() {
//        requireActivity().supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentLayout, PhotoFragment.newInstance())
//            .addToBackStack("photo_list")
//            .commit()
    }

    private fun onItemClick(item: PhotoData) {
        viewModel.selectItem(item.pic_src)
        findNavController().navigate(R.id.action_routeFragment_to_collectionGalleryFragment)
    }

    private fun checkPermissions() {
        Log.d(TAG, "check permissions")
        val allGranted = REQUEST_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) //
        else launcher.launch(REQUEST_PERMISSIONS)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onLongItemClick(item: PhotoData) {
        val imageDate = item.date
        val date = LocalDate.parse(
            imageDate,
            DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.getDefault())
        )
        Log.d(TAG, "Image date: $date")
        viewModel.deletePhoto(item.pic_src)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}