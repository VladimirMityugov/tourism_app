package com.example.permissionsapp.ui.main.profile

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.utility.Constants.AVATAR
import com.example.permissionsapp.presentation.utility.Constants.REQUIRED_PERMISSIONS
import com.example.permissionsapp.presentation.utility.hasReadPermission
import com.example.permissionsapp.presentation.view_models.MainViewModel
import com.example.permissionsapp.ui.main.LoginActivity
import com.example.tourismApp.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val viewModel: MainViewModel by activityViewModels()

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.saveAvatarUriToDataStore(uri.toString())
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.all { true }) {
                photoPickerLauncher
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userAvatar = binding.userAvatar
        val welcomeMessage = binding.welcomeMessage
        val name = binding.userName


        binding.changePhoto.setOnClickListener {
            onChangePhotoClick()
        }

        binding.applyChanges.setOnClickListener {
            val newName = name.text.toString()
            onApplyChangesClick(newName)
        }

        binding.signOutButton.setOnClickListener {
            onSignOutButtonClick()

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.currentUserName.collectLatest { userName ->
                if(userName!=null){
                    name.setText(userName)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.currentUserName.collectLatest { name ->
                viewModel.isFirstLaunch.collectLatest { isFirstLaunch ->
                    if (isFirstLaunch && name != null) {
                        welcomeMessage.text = buildString {
                            append("Welcome, ")
                            append(name)
                        }
                    } else {
                        welcomeMessage.visibility = View.INVISIBLE
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.userAvatarUri.collectLatest { uri ->
                if (uri != null) {
                    Glide
                        .with(userAvatar)
                        .load(uri)
                        .centerCrop()
                        .into(userAvatar)
                }else{
                    Glide
                        .with(userAvatar)
                        .load(AVATAR)
                        .centerCrop()
                        .into(userAvatar)
                }
            }
        }


    }
    private fun onChangePhotoClick() {
        if (requireContext().hasReadPermission()) {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (shouldShowRequestPermissionRationale(REQUIRED_PERMISSIONS[0])) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Read permission dialog")
                .setMessage("To upload image, please accept read external storage permission")
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    dialogLauncher()
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                })
                .create()
                .show()
        } else {
            dialogLauncher()
        }
    }

    private fun dialogLauncher() {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun onSignOutButtonClick() {
        firebaseAuth.signOut()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun onApplyChangesClick(name: String) {
        viewModel.saveNameToDataStore(name)
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}