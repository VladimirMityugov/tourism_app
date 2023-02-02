package com.example.permissionsapp.ui.main.photos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.MyViewModel
import com.example.tourismApp.databinding.FragmentGallerySingleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class GallerySingleFragment : Fragment() {

    private var _binding: FragmentGallerySingleBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryPicture: AppCompatImageView

    private val viewModel: MyViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGallerySingleBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = arguments?.getInt(POSITION)

        galleryPicture = binding.galleryPicture

        galleryPicture.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getPhotoList().collectLatest{photoList ->
                Glide.with(galleryPicture.context)
                    .load(photoList[position!!].pic_src)
                    .into(galleryPicture)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var POSITION = "position"

        @JvmStatic
        fun newInstance(position: Int) = GallerySingleFragment().apply {
            arguments = Bundle().apply {
                putInt(POSITION, position)
            }
        }
    }
}