package com.example.tourismapp.ui.main.photos

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.Surface.*
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentPhotoBinding
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.example.tourismapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_CAMERA_PERMISSIONS
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_READ_PERMISSIONS
import com.example.tourismapp.presentation.utility.Constants.REQUIRED_WRITE_PERMISSIONS
import com.example.tourismapp.presentation.utility.location.MyLocation
import com.example.tourismapp.presentation.utility.permissions.*
import com.example.tourismapp.presentation.view_models.PermissionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "PHOTO_FRAGMENT"


@AndroidEntryPoint
class PhotoFragment : Fragment() {

    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!

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
                if (it[permission] == true) {
                    permissionsViewModel.dismissDialog(permission)
                }
            }
            val permissionsList = permissionsViewModel.permissionsList.value
            if (permissionsList.isEmpty()) {
                startCamera()
            } else {
                checkPermissionList(permissionsList)
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCurrentLocation(INTERVAL_FOR_LOCATION_UPDATES).collectLatest { location ->
                myLocation = MyLocation(location.latitude, location.longitude)
            }
        }

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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPhotosByRouteName(viewModel.routeName.value.toString())
                    .collectLatest { photos ->
                        if (photos.isNotEmpty()) {
                            galleryButton.visibility = View.VISIBLE
                        } else {
                            galleryButton.visibility = View.GONE
                        }
                    }
            }
        }


    }


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
                    Log.d(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri
                    val date = contentValues.get(MediaStore.MediaColumns.DISPLAY_NAME)

                    Log.d(TAG, "PHOTO IS TAKEN. URI: $uri")
                    //Select this photo in ViewModel
                    viewModel.selectItem(
                        uri = uri.toString()
                    )

                    //Save this photo to DataBase
                    val routeName = viewModel.routeName.value
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

                    //Display this photo in preview
                    Glide
                        .with(this@PhotoFragment)
                        .load(uri)
                        .error(R.drawable.ic_baseline_error_outline_24)
                        .circleCrop()
                        .into(previewImage)

                }
            }
        )
    }

    private fun startCamera() {
        val cameraProvider = ProcessCameraProvider.getInstance(requireContext())

        cameraProvider.addListener({
            val cameraProviderConfiguration = cameraProvider.get()

            val preview = Preview.Builder()
                .setTargetRotation(defineRotation())
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            var cameraSelector: CameraSelector
            val cameraBack =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
            val cameraFront =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

            cameraSelector = cameraBack

            //Front/back camera switcher
            binding.rotateCamera.setOnClickListener {
                if (cameraSelector == cameraBack) {
                    cameraSelector = cameraFront
                    resetCameraPreview(
                        cameraProviderConfiguration = cameraProviderConfiguration,
                        cameraSelector = cameraSelector,
                        preview = preview
                    )
                } else {
                    cameraSelector = cameraBack
                    resetCameraPreview(
                        cameraProviderConfiguration = cameraProviderConfiguration,
                        cameraSelector = cameraSelector,
                        preview = preview
                    )
                }
            }

            try {
                cameraProviderConfiguration.unbindAll()
                cameraProviderConfiguration.bindToLifecycle(
                    this as LifecycleOwner,
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
        if (requireContext().hasCameraPermission()
            && requireContext().hasReadPermission()
            && requireContext().hasWritePermission()
        ) {
            startCamera()
        } else {
            val requiredPermissions = mutableListOf<String>()
            REQUIRED_CAMERA_PERMISSIONS.forEach { requiredPermissions.add(it) }
            REQUIRED_WRITE_PERMISSIONS.forEach { requiredPermissions.add(it) }
            REQUIRED_READ_PERMISSIONS.forEach { requiredPermissions.add(it) }
            val permissions = requiredPermissions.toTypedArray()
            permissionLauncher.launch(permissions)
        }
    }

    private fun checkPermissionList(permissions: List<String>) {
        permissions.forEach { permission ->
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

    private fun defineRotation(): Int {
        val displayManager =
            requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        val targetRotation = when (display.rotation) {
            ROTATION_0 -> ROTATION_270
            ROTATION_90 -> ROTATION_0
            ROTATION_180 -> ROTATION_90
            ROTATION_270 -> ROTATION_180
            else -> ROTATION_0
        }
        return targetRotation
    }

    private fun resetCameraPreview(
        cameraProviderConfiguration: ProcessCameraProvider,
        cameraSelector: CameraSelector,
        preview: Preview
    ) {
        cameraProviderConfiguration.unbindAll()
        cameraProviderConfiguration.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PhotoFragment()
        private const val DATE_FORMAT = "yyyy-MM-dd hh:mm:ss"
    }

}