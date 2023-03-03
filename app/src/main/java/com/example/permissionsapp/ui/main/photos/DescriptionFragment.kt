package com.example.permissionsapp.ui.main.photos

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.permissionsapp.presentation.MyViewModel
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentDescriptionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import hilt_aggregated_deps._com_example_permissionsapp_presentation_MyViewModel_HiltModules_BindsModule
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "DESCRIPTION_FRAG"

class DescriptionFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDescriptionBinding? = null
    val binding get() = _binding!!
    private lateinit var descriptionInputField: AppCompatEditText
    private lateinit var saveButton: AppCompatButton
    private var isRouteSelected = false

    private val viewModel: MyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDescriptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        descriptionInputField = binding.descriptionInputField
        saveButton = binding.saveDescriptionButton

        val routeName = viewModel.routeName.value.toString()


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isPhoto.collectLatest { isPhoto ->
                viewModel.isRoute.collectLatest { isRoute ->
                    when {
                        isPhoto != null && isPhoto == true -> {
                            isRouteSelected = false
                            Log.d(TAG, "IS PHOTO")
                            setPhotoDescription(routeName)
                        }
                        isRoute != null && isRoute == true -> {
                            isRouteSelected = true
                            Log.d(TAG, "IS ROUTE")
                            setRouteDescription(routeName)
                        }
                    }
                }
            }
        }

        saveButton.setOnClickListener {
            val description = descriptionInputField.text.toString()
            if (isRouteSelected) {
                Log.d(TAG, "IS ROUTE")
                addRouteDescription(description, routeName)
            } else {
                Log.d(TAG, "IS PHOTO")
                addPhotoDescription(description)
            }
            dismiss()
        }
    }


    private fun setRouteDescription(routeName: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getRouteInfoByName(routeName).collectLatest { routeInfo ->
                val routeDescription =
                    routeInfo.find { it.route_name == routeName }?.route_description
                Log.d(TAG, "DESCRIPTION IS : $routeDescription")
                if (routeDescription != null) {
                    descriptionInputField.setText(routeDescription)
                }
            }
        }
    }

    private fun setPhotoDescription(routeName: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectedItem.collectLatest { uri ->
                viewModel.getPhotosByRouteName(routeName).collectLatest { photos ->
                    val photoDescription = photos.find { it.pic_src == uri }?.description
                    Log.d(TAG, "DESCRIPTION IS : $photoDescription")
                    Log.d(TAG, "URI IS : $uri")
                    if (photoDescription != null) {
                        descriptionInputField.setText(photoDescription)
                    }
                }
            }
        }
    }

    private fun addRouteDescription(description: String, routeName: String) {
        if (description.isNotEmpty() && routeName.isNotEmpty()) {
            viewModel.addRouteDescription(description, routeName)
        }
    }

    private fun addPhotoDescription(description: String) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectedItem.collectLatest { uri ->
                if (uri != null && description.isNotEmpty()) {
                    viewModel.addPhotoDescription(description, uri)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "ON DETACH")
        viewModel.switchRouteSelected(false)
        viewModel.switchPhotoSelected(false)
        isRouteSelected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

    }
}