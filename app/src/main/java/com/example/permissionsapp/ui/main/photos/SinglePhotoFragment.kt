package com.example.permissionsapp.ui.main.photos


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.permissionsapp.presentation.MyViewModel
import com.example.tourismApp.R
import com.example.tourismApp.databinding.FragmentSinglePhotoBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "SINGLE_PHOTO"

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
    private lateinit var description: AppCompatTextView

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
        description = binding.description


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
            onAddDescriptionClick()
        }

        addDescriptionTitle.setOnClickListener {
            onAddDescriptionClick()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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


        photo.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        backButton.setOnClickListener {
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


