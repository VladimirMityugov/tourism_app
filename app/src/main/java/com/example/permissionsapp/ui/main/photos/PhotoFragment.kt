package com.example.permissionsapp.ui.main.photos

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.utility.Constants
import com.example.permissionsapp.presentation.view_models.MainViewModel
import com.example.permissionsapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_CAMERA_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_READ_PERMISSIONS
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_WRITE_PERMISSIONS
import com.example.permissionsapp.presentation.utility.MyLocation
import com.example.permissionsapp.presentation.utility.permissions.*
import com.example.permissionsapp.presentation.view_models.PermissionsViewModel
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentPhotoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "PHOTO_FRAGMENT"


@AndroidEntryPoint
class PhotoFragment : Fragment() {


    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.all { true }) {
                startCamera()
            } else {
                findNavController().navigate(R.id.action_global_mapsFragment)
            }
        }

    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var outputDirectory: File

    private val currentDate = SimpleDateFormat(
        DATE_FORMAT,
        Locale.getDefault()
    ).format(System.currentTimeMillis())


    private var imageCapture: ImageCapture? = null

    private lateinit var previewImage: ImageView

    private var myLocation = MyLocation(0.0, 0.0)

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
        _binding = FragmentPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        val takePhotoButton = binding.takePhotoButton
        val galleryButton = binding.gallery
        previewImage = binding.previewImage
//        outputDirectory = getOutputDirectory()

        //go to single photo by click on preview image
        previewImage.setOnClickListener {
            findNavController().navigate(R.id.action_photoFragment_to_singlePhotoFragment)
        }

        //take a photo
        takePhotoButton.setOnClickListener {
            onTakePhotoClick()
        }

        //go to route screen with all photos
        galleryButton.setOnClickListener {
            onGalleryButtonClick()
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
                                Manifest.permission.READ_EXTERNAL_STORAGE -> {
                                    ReadExternalStoragePermission()
                                }
                                Manifest.permission.READ_MEDIA_AUDIO -> {
                                    ReadMediaPermission()
                                }
                                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                                    WriteExternalStoragePermission()
                                }
                                Manifest.permission.CAMERA -> {
                                    CameraPermission()
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


    }

//    private fun getOutputDirectory(): File {
//
//    }

    private fun onGalleryButtonClick() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.routeName.collectLatest { routeName ->
                    if (routeName != null) {
                        viewModel.getPhotosByRouteName(routeName)
                    }
                }
            }
        }
        findNavController().navigate(R.id.action_photoFragment_to_routeFragment)
    }

    private fun onTakePhotoClick() {
        val imageCapture = imageCapture ?: return

//        val photoFile = File(
//            outputDirectory,
//        )

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, currentDate)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @SuppressLint("RestrictedApi")
                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri
                    val date = contentValues.get(MediaStore.MediaColumns.DISPLAY_NAME)

                    //Select this photo in ViewModel
                    viewModel.selectItem(
                        uri = uri.toString()
                    )

                    //Save this photo to DataBase
                    viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        viewModel.routeName.collectLatest { routeName ->
                            if (routeName != null) {
                                viewModel.insertPhotos(
                                    uri.toString(),
                                    date as String,
                                    null,
                                    myLocation.latitude,
                                    myLocation.longitude,
                                    routeName
                                )
                            }
                        }
                    }

                    //Display this photo in preview
                    Glide.with(previewImage)
                        .load(uri)
                        .error(R.drawable.ic_baseline_error_outline_24)
                        .circleCrop()
                        .into(previewImage)

                }
            }
        )
    }

    private fun startCamera() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCurrentLocation(INTERVAL_FOR_LOCATION_UPDATES).collectLatest { location ->
                Log.d(TAG, "My location : ${location.latitude}, ${location.longitude}")
                myLocation = MyLocation(location.latitude, location.longitude)
            }
        }
        val cameraProvider = ProcessCameraProvider.getInstance(this.requireContext())

        cameraProvider.addListener({
            val cameraProviderConfiguration = cameraProvider.get()

            val preview = Preview.Builder().build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            var cameraSelector: CameraSelector
            val cameraBack =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val cameraFront =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            cameraSelector = cameraBack

            //Front/back camera switcher
            val cameraSwitcher = binding.rotateCamera
            cameraSwitcher.setOnClickListener {
                if (cameraSelector == cameraBack) {
                    cameraSelector = cameraFront
                    cameraProviderConfiguration.unbindAll()
                    cameraProviderConfiguration.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } else {
                    cameraSelector = cameraBack
                    cameraProviderConfiguration.unbindAll()
                    cameraProviderConfiguration.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }
            }
            imageCapture = ImageCapture.Builder()
                .build()

            cameraProviderConfiguration.unbindAll()
            try {
                cameraProviderConfiguration.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun checkPermissions() {

        if (requireContext().hasCameraPermission() && requireContext().hasReadPermission() && requireContext().hasWritePermission()) {
            startCamera()
        } else {
            permissionLauncher.launch(REQUIRED_READ_PERMISSIONS)
            permissionLauncher.launch(REQUIRED_WRITE_PERMISSIONS)
            permissionLauncher.launch(REQUIRED_CAMERA_PERMISSIONS)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PhotoFragment()
        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
        private const val DATE_FORMAT = "yyyy-MM-dd hh:mm:ss"
    }

}