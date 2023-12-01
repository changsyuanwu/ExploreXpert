package com.example.explorexpert.ui.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.MainActivity
import com.example.explorexpert.databinding.LocationBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LocationBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private var _binding: LocationBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var addedTrip: Boolean = false

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val parentFrag = parentFragment as MapsFragment
        if (addedTrip) {
            Snackbar.make(parentFrag.view as View, "Location added to trip.", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun configureButtons() {
        val mainActivity = requireActivity() as MainActivity
        binding.btnNearbyPlaces.setOnClickListener {
            mainActivity.getMapFragment().getNearbyLocations()
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

    fun setTripAdded() {
        addedTrip = true
    }

    companion object {
        const val TAG: String = "LocationBottomSheetDialogFragment"
    }

}