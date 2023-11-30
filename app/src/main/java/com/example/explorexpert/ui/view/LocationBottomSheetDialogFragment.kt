package com.example.explorexpert.ui.view

import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.MainActivity
import com.example.explorexpert.databinding.LocationBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private var _binding: LocationBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LocationBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureButtons()
    }

    private fun configureButtons() {
        val mainActivity = requireActivity() as MainActivity
        binding.btnNearbyPlaces.setOnClickListener {
            this.dismiss()
        }
        binding.btnAdd.setOnClickListener {
            val selectTripBottomSheetDialogFragment = SelectTripBottomSheetDialogFragment()
            selectTripBottomSheetDialogFragment.show(
                childFragmentManager,
                SelectTripBottomSheetDialogFragment.TAG
            )
        }
        binding.btnWeather.setOnClickListener {
            mainActivity.swapToWeatherViaMap()
            this.dismiss()
        }
    }

    companion object {
        const val TAG: String = "LocationBottomSheetDialogFragment"
    }

}