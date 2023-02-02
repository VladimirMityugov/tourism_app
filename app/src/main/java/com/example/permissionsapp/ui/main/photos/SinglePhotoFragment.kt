package com.example.permissionsapp.ui.main.photos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.MyViewModel
import com.example.tourismApp.databinding.FragmentSinglePhotoBinding
import kotlinx.coroutines.flow.collectLatest

class SinglePhotoFragment : Fragment() {

    companion object {
        fun newInstance() = SinglePhotoFragment()
    }

    private var _binding: FragmentSinglePhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var photo: ImageView
    private lateinit var backButton: ImageButton

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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.selectedItem.collectLatest {photoData ->
                Glide
                    .with(photo.context)
                    .load(photoData?.pic_src)
                    .into(photo)
            }
        }

        photo.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


