package com.example.tourismapp.ui.main.maps


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.tourismapp.databinding.FragmentSearchSettingsBinding
import com.example.tourismapp.presentation.view_models.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


private const val TAG = "SEARCH_SETTINGS"

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



    private val viewModel: MainViewModel by activityViewModels()

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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getPlacesKinds().collectLatest { placeKindList ->
                Log.d(TAG, "Places are : $placeKindList")
                val placesKinds = mutableListOf<String>()
                placeKindList.forEach {
                    placesKinds.add(it.kind)
                }
                viewModel.updatePlacesKindsList(placesKinds)
                viewModel.checkPlacesKinds(placesKinds)
                if(placesKinds.isEmpty())viewModel.hideAllPlaces(true)
                else viewModel.hideAllPlaces(false)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addedToPlacesKinds.collectLatest { checkBoxStatus ->
                interestingPlacesCheckBox.isActivated = checkBoxStatus[INTERESTING_PLACES] == true
                foodCheckBox.isActivated = checkBoxStatus[FOOD] == true
                banksCheckBox.isActivated = checkBoxStatus[BANKS] == true
                transportCheckBox.isActivated = checkBoxStatus[TRANSPORT] == true
                shopsCheckBox.isActivated = checkBoxStatus[SHOPS] == true
            }
        }

        interestingPlacesCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            interestingPlacesCheckBox.isActivated = isChecked
            viewModel.onPlacesKindsClick(INTERESTING_PLACES)
        }

        foodCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            foodCheckBox.isActivated = isChecked
            viewModel.onPlacesKindsClick(FOOD)
        }

        shopsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            shopsCheckBox.isActivated = isChecked
            viewModel.onPlacesKindsClick(SHOPS)
        }

        banksCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            banksCheckBox.isActivated = isChecked
            viewModel.onPlacesKindsClick(BANKS)
        }

        transportCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            transportCheckBox.isActivated = isChecked
            viewModel.onPlacesKindsClick(TRANSPORT)
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
        viewModel.deleteAllPlacesKinds()
    }

    private fun resetRadius() {
        slider.value = 5.0F
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        private const val INTERESTING_PLACES = "interesting_places"
        private const val FOOD = "foods"
        private const val BANKS = "banks"
        private const val SHOPS = "shops"
        private const val TRANSPORT = "transport"
    }
}