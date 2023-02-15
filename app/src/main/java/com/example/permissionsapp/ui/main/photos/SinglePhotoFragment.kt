package com.example.permissionsapp.ui.main.photos

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.MyViewModel
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentSinglePhotoBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest

class SinglePhotoFragment : Fragment() {

    companion object {
        fun newInstance() = SinglePhotoFragment()
    }

    private var _binding: FragmentSinglePhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var photo: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var addDescriptionButton: AppCompatImageButton
    private lateinit var addDescriptionTitle: AppCompatTextView
    private lateinit var descriptionLayout: LinearLayout
    private lateinit var description: AppCompatEditText
    private lateinit var saveButton: AppCompatButton

    private val viewModel: MyViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSinglePhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photo = binding.photoImage
        backButton = binding.backButton
        addDescriptionButton = binding.addDescriptionButton
        addDescriptionTitle = binding.addDescriptionTitle
        descriptionLayout = binding.descriptionLayout
        description = binding.description
        saveButton = binding.saveDescriptionButton

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectedItem.collectLatest { uri ->
                Glide
                    .with(photo.context)
                    .load(uri)
                    .centerCrop()
                    .into(photo)
            }
        }

        addDescriptionButton.setOnClickListener {
            descriptionLayout.visibility = View.VISIBLE
        }

        addDescriptionTitle.setOnClickListener {
            descriptionLayout.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener {

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.selectedItem.collectLatest { uri ->
                    if (uri != null) {
                        viewModel.addPhotoDescription(description.text.toString(), uri)
                    }
                }
            }
            descriptionLayout.visibility = View.GONE

            showSnackbar()
        }

        photo.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showSnackbar(){
      Snackbar.make(photo,"Description is saved", Snackbar.LENGTH_SHORT)
          .setTextColor(resources.getColor(R.color.myBlack))
          .setBackgroundTint(resources.getColor(R.color.myLightGrey))
          .setAnchorView(addDescriptionButton)
          .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


