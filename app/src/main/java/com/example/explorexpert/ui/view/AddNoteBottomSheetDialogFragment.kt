package com.example.explorexpert.ui.view

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.databinding.AddNoteBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddNoteBottomSheetDialogFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel

    private var _binding : AddNoteBottomSheetBinding? = null
    private val binding get() = _binding!!

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

        binding.txtInputNoteDescription.editText?.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

        configureButtons()
    }

    private fun configureButtons() {
        binding.btnAddANoteSave.setOnClickListener {
//            val tripName = binding.txtInputTripName.editText?.text.toString()
//            createTripViewModel.createTrip(tripName)
//            planViewModel.fetchTrips()
            this.dismiss()
        }
    }

    companion object {
        const val TAG: String = "AddNoteBottomSheetDialogFragment"
    }
}