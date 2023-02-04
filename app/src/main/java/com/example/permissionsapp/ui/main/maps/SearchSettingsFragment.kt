package com.example.permissionsapp.ui.main.maps


import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.permissionsapp.presentation.MyViewModel
import com.example.tourismApp.databinding.FragmentSearchSettingsBinding

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class SearchSettingsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSearchSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var backButton: AppCompatImageButton
    private lateinit var settingsTitle: AppCompatTextView
    private lateinit var interestingPlacesCheckBox: AppCompatCheckBox
    private lateinit var foodCheckBox: AppCompatCheckBox
    private lateinit var shopsCheckBox: AppCompatCheckBox
    private lateinit var banksCheckBox: AppCompatCheckBox
    private lateinit var transportCheckBox: AppCompatCheckBox
    private lateinit var resetRadius: AppCompatTextView
    private lateinit var slider: Slider
    private lateinit var clearKindsButton: AppCompatTextView
    private lateinit var ratingRadioGroup: RadioGroup
    private lateinit var ratingLow: RadioButton
    private lateinit var ratingMedium: RadioButton
    private lateinit var ratingHigh: RadioButton
    private lateinit var ratingLowH: RadioButton
    private lateinit var ratingMediumH: RadioButton
    private lateinit var ratingHighH: RadioButton
    private lateinit var clearRatingButton: AppCompatTextView


    private val viewModel: MyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton = binding.backButton
        settingsTitle = binding.settingsTitle
        interestingPlacesCheckBox = binding.interestingPlacesCheckbox
        foodCheckBox = binding.foodCheckbox
        shopsCheckBox = binding.shopsCheckbox
        banksCheckBox = binding.banksCheckbox
        transportCheckBox = binding.transportCheckbox
        slider = binding.slider
        resetRadius = binding.anyRadius
        clearKindsButton = binding.clearKinds
        ratingRadioGroup = binding.ratingRadioGroup
        ratingLow = binding.rating1
        ratingMedium = binding.rating2
        ratingHigh = binding.rating3
        ratingLowH = binding.rating1H
        ratingMediumH = binding.rating2H
        ratingHighH = binding.rating3H
        clearRatingButton = binding.clearRating

        clearRatingButton.setOnClickListener {
            ratingRadioGroup.clearCheck()
        }

        interestingPlacesCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            interestingPlacesCheckBox.isActivated = isChecked
        }

        foodCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            foodCheckBox.isActivated = isChecked
        }

        shopsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            shopsCheckBox.isActivated = isChecked
        }

        banksCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            banksCheckBox.isActivated = isChecked
        }

        transportCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            transportCheckBox.isActivated = isChecked
        }

        ratingLow.setOnCheckedChangeListener { buttonView, isChecked ->
            ratingLow.isActivated = isChecked
        }

        ratingMedium.setOnCheckedChangeListener { buttonView, isChecked ->
            ratingMedium.isActivated = isChecked
        }

        ratingHigh.setOnCheckedChangeListener { buttonView, isChecked ->
            ratingHigh.isActivated = isChecked
        }

        ratingLowH.setOnCheckedChangeListener { buttonView, isChecked ->
            ratingLowH.isActivated = isChecked
        }

        ratingMediumH.setOnCheckedChangeListener { buttonView, isChecked ->
            ratingMediumH.isActivated = isChecked
        }

        ratingHighH.setOnCheckedChangeListener { buttonView, isChecked ->
            ratingHighH.isActivated = isChecked
        }

        clearKindsButton.setOnClickListener {
            uncheckAll()
        }

        resetRadius.setOnClickListener {
            resetRadius()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.radius.collectLatest { radius ->
                slider.value = radius / 1000.toFloat()
            }
        }

        slider.addOnChangeListener(Slider.OnChangeListener { slider, _, _ ->
            val value = slider.value
            viewModel.setRadius(value.toInt())
        })


        slider.setLabelFormatter { it.toString() }

        backButton.rotation = 270f

        backButton.setOnClickListener {
            dismiss()
        }
    }

    private fun uncheckAll() {
        interestingPlacesCheckBox.isChecked = false
        foodCheckBox.isChecked = false
        shopsCheckBox.isChecked = false
        banksCheckBox.isChecked = false
        transportCheckBox.isChecked = false
    }

    private fun resetRadius() {
        slider.value = 5.0F
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}