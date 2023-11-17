package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.explorexpert.R
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.AddNoteBottomSheetBinding
import com.example.explorexpert.databinding.AddPlaceDialogBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.example.explorexpert.ui.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddPlaceDialogFragment(
    private val trip: Trip
) : DialogFragment() {

    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel
    private lateinit var tripViewModel: TripViewModel

    private var _binding : AddPlaceDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogSlideUpStyle)

        tripViewModel = (requireParentFragment() as TripDialogFragment).tripViewModel
        addTripItemViewModel.setTrip(trip)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddPlaceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.show()
    }

    companion object {
        const val TAG: String = "AddPlaceDialogFragment"
    }
}