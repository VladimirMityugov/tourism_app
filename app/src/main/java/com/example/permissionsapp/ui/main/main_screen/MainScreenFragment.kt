package com.example.permissionsapp.ui.main.main_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.permissionsapp.data.local.entities.PhotoData
import com.example.permissionsapp.presentation.MyViewModel
import com.example.permissionsapp.presentation.PhotoAdapter
import com.example.permissionsapp.ui.main.LoginActivity
import com.example.permissionsapp.ui.main.maps.MapsFragment
import com.example.permissionsapp.ui.main.photos.CollectionGalleryFragment
import com.example.permissionsapp.ui.main.photos.PhotoFragment
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentListPhotosBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class MainScreenFragment : Fragment() {

    companion object {
        fun newInstance() = MainScreenFragment()
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

    private var _binding: FragmentListPhotosBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraIconButton: Button
    private lateinit var mapIcon: ImageButton
    private lateinit var welcomeMessage: TextView
    private lateinit var username: TextView
    private lateinit var signOut: AppCompatButton
    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel: MyViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val photoAdapter = PhotoAdapter(onItemClick = { PhotoData -> onItemClick(PhotoData) },
        onLongClick = { PhotoData -> onLongItemClick(PhotoData) })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListPhotosBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraIconButton = binding.cameraIconButton
        binding.photosRecyclerView.adapter = photoAdapter
        mapIcon = binding.mapsButton
        welcomeMessage = binding.welcomeMessage
        username = binding.userTitle
        signOut = binding.signOutButton
        firebaseAuth = FirebaseAuth.getInstance()

        signOut.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }


        if (firebaseAuth.currentUser?.displayName == null) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.currentUserName.collectLatest { currentName ->
                    Log.d(TAG, "Name is $currentName")
                    if(currentName != null){
                        username.isVisible = true
                        username.text = currentName
                    }
                    username.isVisible = false
                }
            }
        } else {
            username.text = firebaseAuth.currentUser?.displayName.toString()
        }


        mapIcon.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentLayout, MapsFragment.newInstance())
                .addToBackStack("Photo_list")
                .commit()
        }

        cameraIconButton.setOnClickListener {
            moveToPhotoFragment()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getPhotoList().collect {
                photoAdapter.submitList(it)
            }
        }

    }

    private fun moveToPhotoFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentLayout, PhotoFragment.newInstance())
            .addToBackStack("photo_list")
            .commit()
    }

    private fun onItemClick(item: PhotoData) {
        viewModel.selectItem(item.pic_src)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentLayout, CollectionGalleryFragment.newInstance())
            .addToBackStack("Photo_list")
            .commit()
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