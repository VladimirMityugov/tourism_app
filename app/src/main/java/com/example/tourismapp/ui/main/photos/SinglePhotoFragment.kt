package com.example.tourismapp.ui.main.photos


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.tourismapp.R
import com.example.tourismapp.databinding.FragmentSinglePhotoBinding
import com.example.tourismapp.presentation.view_models.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SINGLE_PHOTO"

class SinglePhotoFragment : Fragment() {

    companion object {
        fun newInstance() = SinglePhotoFragment()
    }

    private var _binding: FragmentSinglePhotoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSinglePhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photo = binding.photoImage
        val description = binding.description

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedItem.collectLatest { uri ->
                    Glide
                        .with(photo.context)
                        .load(uri)
                        .centerCrop()
                        .into(photo)
                }
            }
        }

        binding.addDescriptionButton.setOnClickListener {
            onAddDescriptionClick()
        }

        binding.addDescriptionTitle.setOnClickListener {
            onAddDescriptionClick()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPhotosByRouteName(viewModel.routeName.value.toString())
                    .collectLatest { photos ->
                        viewModel.selectedItem.collectLatest { uri ->
                            val photoDescription = photos.find { it.pic_src == uri }?.description
                            if (photoDescription != null) {
                                description.visibility = View.VISIBLE
                                description.text = photoDescription
                            } else {
                                description.visibility = View.GONE
                            }
                        }
                    }
            }
        }


        photo.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun onAddDescriptionClick() {
        viewModel.switchPhotoSelected(true)
        val popupWindow = DescriptionFragment()
        popupWindow.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        popupWindow.enterTransition = com.google.android.material.R.id.animateToStart
        popupWindow.exitTransition = com.google.android.material.R.id.animateToEnd
        popupWindow.show(requireActivity().supportFragmentManager, "POP_UP")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


