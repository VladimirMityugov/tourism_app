package com.example.permissionsapp.ui.main.photos

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.view_models.MainViewModel
import com.example.permissionsapp.presentation.utility.Constants.INTERVAL_FOR_LOCATION_UPDATES
import com.example.permissionsapp.presentation.utility.MyLocation
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentPhotoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
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
    private val currentDate = SimpleDateFormat(
        DATE_FORMAT,
        Locale.getDefault()
    ).format(System.currentTimeMillis())
    private lateinit var takePhotoButton: ImageButton
    private var imageCapture: ImageCapture? = null
    private lateinit var preview: Preview
    private lateinit var previewImage: ImageView
    private lateinit var galleryButton: ImageButton
    private lateinit var cameraSwitcher: AppCompatImageButton
    private var myLocation = MyLocation(0.0, 0.0)


    private val viewModel: MainViewModel by activityViewModels()

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



        takePhotoButton = binding.takePhotoButton
        previewImage = binding.previewImage
        galleryButton = binding.gallery
        cameraSwitcher = binding.rotateCamera

        previewImage.setOnClickListener {
            findNavController().navigate(R.id.action_photoFragment_to_singlePhotoFragment)
        }

        takePhotoButton.setOnClickListener {
            takePhoto()
        }

        galleryButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.routeName.collectLatest { routeName ->
                    if (routeName != null) {
                        viewModel.getPhotosByRouteName(routeName)
                    }
                }
            }

            findNavController().navigate(R.id.action_photoFragment_to_routeFragment)
        }


    }

    private fun takePhoto() {
        Log.d(TAG, "Take photo")
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


                    val msg = "Photo capture succeeded: $uri"
                    Log.d(TAG, msg)


                    //Display this photo in preview
                    Glide.with(requireContext())
                        .load(uri.toString())
                        .circleCrop()
                        .into(previewImage)

                }
            }
        )
    }

    private fun startCamera() {
        Log.d(TAG, "Start camera")

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getCurrentLocation(INTERVAL_FOR_LOCATION_UPDATES).collectLatest { location ->
                Log.d(TAG, "My location : ${location.latitude}, ${location.longitude}")
                myLocation = MyLocation(location.latitude, location.longitude)
            }
        }
        val cameraProvider = ProcessCameraProvider.getInstance(this.requireContext())
        cameraProvider.addListener({
            val cameraProviderConfiguration = cameraProvider.get()

            preview = Preview.Builder().build()
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
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
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
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED)
        }
        if (isAllGranted) {
            startCamera()
        } else {
            if (shouldShowRequestPermissionRationale(REQUEST_PERMISSIONS[0])) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Camera permission dialog")
                    .setMessage("This app works only with camera. To proceed please accept camera use")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                        dialogLauncher()
                    })
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                        findNavController().navigate(R.id.action_global_mapsFragment)
                        dialog.dismiss()

                    })
                    .create()
                    .show()
            } else {
                dialogLauncher()
            }
        }
    }

    private fun dialogLauncher() {
        launcher.launch(REQUEST_PERMISSIONS)
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