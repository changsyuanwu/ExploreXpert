package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.databinding.CreateTripBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AuthViewModel
import com.example.explorexpert.ui.viewmodel.CreateTripViewModel
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateTripBottomSheetDialogFragment: BottomSheetDialogFragment() {

    @Inject
    lateinit var createTripViewModel: CreateTripViewModel
    private lateinit var planViewModel: PlanViewModel

    private var _binding: CreateTripBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        planViewModel = (requireParentFragment() as PlanFragment).planViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CreateTripBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureButtons()
    }

    private fun configureButtons() {
        binding.btnBottomSheetCreateTrip.setOnClickListener {
            val tripName = binding.txtInputTripName.editText?.text.toString()
            createTripViewModel.createTrip(tripName)
            planViewModel.fetchTrips()
            this.dismiss()
        }
    }

    companion object {
        const val TAG: String = "CreateTripBottomSheetDialogFragment"
    }

}