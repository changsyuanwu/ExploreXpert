package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.CreateTripCopyBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.CreateTripViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateTripCopyBottomSheetDialogFragment(
    private val tripToCopy: Trip
): BottomSheetDialogFragment() {

    @Inject
    lateinit var createTripViewModel: CreateTripViewModel

    private var _binding: CreateTripCopyBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CreateTripCopyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureUI()
        configureButtons()
    }

    private fun configureUI() {
        binding.txtInputTripName.editText?.setText("${tripToCopy.name} copy")
    }

    private fun configureButtons() {
        binding.btnBottomSheetCreateTripCopy.setOnClickListener {
            val tripName = binding.txtInputTripName.editText?.text.toString()

            if (tripName == "") {
                binding.txtInputTripName.error = "Trip name cannot be empty"
                return@setOnClickListener
            }
            else {
                binding.txtInputTripName.error = null
            }

            createTripViewModel.createTripFromCopy(tripName, tripToCopy)

            val parentFrag = requireParentFragment()
            Snackbar.make(
                parentFrag.view as View,
                "Successfully created a copy named $tripName",
                Snackbar.LENGTH_SHORT
            ).show()

            this.dismiss()
        }
    }
}