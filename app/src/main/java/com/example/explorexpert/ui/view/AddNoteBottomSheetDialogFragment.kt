package com.example.explorexpert.ui.view

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.AddNoteBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import com.example.explorexpert.ui.viewmodel.TripViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddNoteBottomSheetDialogFragment(
    private val trip: Trip
): BottomSheetDialogFragment() {

    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel
    private lateinit var tripViewModel: TripViewModel

    private var _binding : AddNoteBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tripViewModel = (requireParentFragment() as TripDialogFragment).tripViewModel
        addTripItemViewModel.setTrip(trip)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddNoteBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureButtons()
    }

    private fun configureButtons() {
        binding.btnAddANoteSave.setOnClickListener {
            val title = binding.txtInputNoteTitle.editText?.text.toString()
            val desc = binding.txtInputNoteDescription.editText?.text.toString()
            addTripItemViewModel.addNote(title, desc)
            (requireParentFragment() as TripDialogFragment).refreshTrip()
            (requireParentFragment() as TripDialogFragment).scheduleTripItemsRefresh()
            tripViewModel.fetchSavedItems()
            this.dismiss()
        }
    }

    companion object {
        const val TAG: String = "AddNoteBottomSheetDialogFragment"
    }
}