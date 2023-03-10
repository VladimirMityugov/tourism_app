package com.example.permissionsapp.ui.main.photos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.permissionsapp.presentation.view_models.MainViewModel
import com.example.permissionsapp.presentation.adapters.FragmentGalleryCollectionAdapter
import com.example.tourismApp.databinding.FragmentGalleryCollectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


private const val TAG = "COLLECTION"

@AndroidEntryPoint
class CollectionGalleryFragment : Fragment() {

    private var _binding: FragmentGalleryCollectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var galleryCollectionAdapter: FragmentGalleryCollectionAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var routeName: AppCompatTextView
    private lateinit var closeButton: AppCompatImageButton


    private val viewModel: MainViewModel by activityViewModels()


    private val pagerCallBack = object : ViewPager2.OnPageChangeCallback() {

        var currentPage = 0
        var isSettled = false

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                isSettled = false
            }
            if (state == ViewPager2.SCROLL_STATE_SETTLING) {
                isSettled = true
            }
            if (state == ViewPager2.SCROLL_STATE_IDLE && !isSettled) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentPage = position + 1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryCollectionBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        viewPager = binding.pager

        routeName = binding.routeName
        closeButton = binding.closeButton
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.routeName.collectLatest { name ->
                routeName.text = name
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.getPhotosByRouteName(viewModel.routeName.value.toString()).collectLatest { photoList ->
                    viewModel.selectedItem.collectLatest { uri ->
                        galleryCollectionAdapter =
                            FragmentGalleryCollectionAdapter(
                                this@CollectionGalleryFragment,
                                photoList.size
                            )
                        viewPager.adapter = galleryCollectionAdapter

                        val photoToDisplay = photoList.find { it.pic_src == uri }
                        val pageToDisplay = photoList.indexOf(photoToDisplay)
                        viewPager.setCurrentItem(pageToDisplay, false)
                    }
                }
        }

        closeButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        viewPager.registerOnPageChangeCallback(pagerCallBack)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CollectionGalleryFragment()
    }
}